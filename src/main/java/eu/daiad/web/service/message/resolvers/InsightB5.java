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
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.annotate.message.MessageGenerator;
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

@MessageGenerator(period = "P1M", dayOfMonth = 2, maxPerMonth = 1)
@Component
@Scope("prototype")
public class InsightB5 extends AbstractRecommendationResolver
{
    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for monthly volume consumption */
        private static final String MIN_VALUE = "1E+1"; 

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double previousValue;

        public Parameters()
        {}

        public Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double previousValue)
        {
            super(refDate, deviceType);
            this.currentValue = currentValue;
            this.previousValue = previousValue;
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

        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            return (previousValue < currentValue)?
                EnumRecommendationTemplate.INSIGHT_B5_MONTHLY_CONSUMPTION_INCR:
                EnumRecommendationTemplate.INSIGHT_B5_MONTHLY_CONSUMPTION_DECR;    
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
        final DateTime targetDate = EnumTimeUnit.MONTH.startOf(refDate.minusMonths(1));
        final DateTimeZone tz = refDate.getZone();
        final double monthlyThreshold = config.getVolumeThreshold(deviceType, EnumTimeUnit.MONTH);

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(tz)
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();
        
        // Compute for target month

        query = queryBuilder
            .sliding(targetDate, +1, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double targetValue = (series != null)? 
            series.get(EnumDataField.VOLUME, EnumMetric.SUM) : null;
        if (targetValue == null || targetValue < monthlyThreshold)
            return Collections.emptyList(); // nothing to compare to
        
        // Compute for same month a year ago

        query = queryBuilder
            .sliding(targetDate.minusYears(1), +1, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double previousValue = (series != null)? 
            series.get(EnumDataField.VOLUME, EnumMetric.SUM) : null;
        if (previousValue == null || previousValue < monthlyThreshold)
            return Collections.emptyList(); // nothing to compare to
        
        debug(
            "%s/%s: Computed consumption for %s compared to %s (a year ago): %.2f previous=%.2f",
             accountKey, deviceType,
             targetDate.toString("MM/YYYY"), targetDate.minusYears(1).toString("MM/YYYY"),
             targetValue, previousValue);
        
        ParameterizedTemplate parameterizedTemplate =
            new Parameters(refDate, deviceType, targetValue, previousValue);
        MessageResolutionStatus<ParameterizedTemplate> result =
            new SimpleMessageResolutionStatus<ParameterizedTemplate>(true, parameterizedTemplate);
        
        return Collections.singletonList(result);
    }

}
