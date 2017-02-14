package eu.daiad.web.service.message.resolvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.annotate.message.MessageGenerator;
import eu.daiad.web.model.ConsumptionStats.EnumStatistic;
import eu.daiad.web.model.EnumDayOfWeek;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.EnumTimeUnit;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.EnumRecommendationTemplate;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.message.Recommendation.ParameterizedTemplate;
import eu.daiad.web.model.message.SimpleMessageResolutionStatus;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumDataField;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.EnumMeasurementDataSource;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractRecommendationResolver;

@MessageGenerator(period = "P1W", dayOfWeek = EnumDayOfWeek.MONDAY, maxPerWeek = 1, maxPerMonth = 2) 
@Component
@Scope("prototype")
public class InsightB2 extends AbstractRecommendationResolver
{
    public static final double CHANGE_PERCENTAGE_THRESHOLD = 40.0;
    
    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for weekly volume consumption */
        private static final String MIN_VALUE = "1E+0"; 

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double previousValue;

        @NotNull
        private EnumTimeUnit timeUnit;

        public Parameters()
        {
            super();
        }

        public Parameters(
            DateTime refDate, EnumDeviceType deviceType, 
            EnumTimeUnit timeUnit, double currentValue, double previousValue)
        {
            super(refDate, deviceType);
            this.currentValue = currentValue;
            this.previousValue = previousValue;
            this.timeUnit = timeUnit;
        }

        @JsonProperty("currentValue")
        public void setCurrentValue(double y)
        {
            this.currentValue = y;
        }

        @JsonProperty("currentValue")
        public Double getCurrentValue()
        {
            return currentValue;
        }

        @JsonProperty("previousValue")
        public void setPreviousValue(double y)
        {
            this.previousValue = y;
        }

        @JsonProperty("previousValue")
        public Double getPreviousValue()
        {
            return previousValue;
        }

        @JsonProperty("timeUnit")
        public EnumTimeUnit getTimeUnit()
        {
            return timeUnit;
        }

        @JsonProperty("timeUnit")
        public void setTimeUnit(EnumTimeUnit timeUnit)
        {
            this.timeUnit = timeUnit;
        }

        @JsonIgnore
        @AssertTrue(message = "This insight (B.2) is only relevant to weekly/monthly consumption")
        public boolean hasProperTimeUnit()
        {
            return (timeUnit == EnumTimeUnit.WEEK || timeUnit == EnumTimeUnit.MONTH);
        }

        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;
            boolean increase = (previousValue < currentValue);

            switch (timeUnit) {
            case WEEK:
                t = increase?
                    EnumRecommendationTemplate.INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_B2_WEEKLY_PREV_CONSUMPTION_DECR;
                break;
            case MONTH:
                t = increase?
                    EnumRecommendationTemplate.INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_B2_MONTHLY_PREV_CONSUMPTION_DECR;
                break;
            default:
                // no-op
            }
            return t;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();

            parameters.put("value", currentValue);
            parameters.put("consumption", currentValue);     

            parameters.put("previous_value", previousValue);
            parameters.put("previous_consumption", previousValue);

            Double percentChange = 100.0 * Math.abs(((currentValue - previousValue) / previousValue));
            parameters.put("percent_change", Integer.valueOf(percentChange.intValue()));

            parameters.put("time_unit", timeUnit.name());

            return parameters;
        }

        @Override
        public Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }
    }
    
    @Autowired
    IDataService dataService;
    
    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();
        
        // Examine in weekly/monthly basis
        
        List<MessageResolutionStatus<ParameterizedTemplate>> results = new ArrayList<>();
        
        for (EnumTimeUnit timeUnit: Arrays.asList(EnumTimeUnit.WEEK, EnumTimeUnit.MONTH)) {
            final Period period = timeUnit.toPeriod();
            final DateTime targetDate = // start at most recent period-sized past interval 
                timeUnit.startOf(refDate.minus(period));
            final double threshold = config.getVolumeThreshold(deviceType, timeUnit);
            
            // Compute for target period

            query = queryBuilder
                .sliding(targetDate, +1, timeUnit, EnumTimeAggregation.ALL)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            Double targetValue = (series != null)?
                series.get(EnumDataField.VOLUME, EnumMetric.SUM) : null;
            if (targetValue == null || targetValue < threshold)
                continue; // skip; nothing to compare to
            
            // Compute for previous period

            query = queryBuilder
                .sliding(targetDate.minus(period), +1, timeUnit, EnumTimeAggregation.ALL)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            Double previousValue = (series != null)? 
                series.get(EnumDataField.VOLUME, EnumMetric.SUM) : null;
            if (previousValue == null || previousValue < threshold)
                continue; // skip; nothing to compare to
            
            // Seems we have sufficient data

            double percentChange = 100.0 * (targetValue - previousValue) / previousValue;
            double score = Math.abs(percentChange) / (2 * CHANGE_PERCENTAGE_THRESHOLD);

            debug(
                "Insight B2 for account %s/%s: Consumption for previous period %s of %s:%n  " +
                    "value=%.2f previous=%.2f change=%.2f%% score=%.2f",
                 accountKey, deviceType, period, targetDate.toString("dd/MM/YYYY"),
                 targetValue, previousValue, percentChange, score);

            ParameterizedTemplate parameterizedTemplate = 
                new Parameters(refDate, deviceType, timeUnit, targetValue, previousValue);
            results.add(
                new SimpleMessageResolutionStatus<>(score, parameterizedTemplate));
        }
        
        return results;
    }

}
