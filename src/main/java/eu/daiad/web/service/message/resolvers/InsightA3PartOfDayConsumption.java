package eu.daiad.web.service.message.resolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.annotate.message.MessageGenerator;
import eu.daiad.web.model.EnumDayOfWeek;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.EnumTimeUnit;
import eu.daiad.web.model.EnumPartOfDay;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.EnumRecommendationTemplate;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.message.Recommendation.ParameterizedTemplate;
import eu.daiad.web.model.message.ScoringMessageResolutionStatus;
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

@MessageGenerator(period = "P1D")
@Component
@Scope("prototype")
public class InsightA3PartOfDayConsumption extends AbstractRecommendationResolver
{
    public static final double CHANGE_PERCENTAGE_THRESHOLD = 45.0;
    
    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E-1"; 

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double averageValue;

        @NotNull
        private EnumPartOfDay partOfDay;

        public Parameters()
        {}

        public Parameters(
            DateTime refDate, EnumDeviceType deviceType,
            EnumPartOfDay partOfDay, double currentValue, double averageValue)
        {
            super(refDate, deviceType);
            this.averageValue = averageValue;
            this.currentValue = currentValue;
            this.partOfDay = partOfDay;
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

        @JsonProperty("averageValue")
        public void setAverageValue(double y)
        {
            this.averageValue = y;
        }

        @JsonProperty("averageValue")
        public Double getAverageValue()
        {
            return averageValue;
        }

        @JsonProperty("partOfDay")
        public EnumPartOfDay getPartOfDay()
        {
            return partOfDay;
        }

        @JsonProperty("partOfDay")
        public void setPartOfDay(EnumPartOfDay partOfDay)
        {
            this.partOfDay = partOfDay;
        }

        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;
            switch (partOfDay) {
            case MORNING:
                t = (averageValue <= currentValue)?
                    EnumRecommendationTemplate.INSIGHT_A3_MORNING_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_A3_MORNING_CONSUMPTION_DECR;
                break;
            case AFTERNOON:
                t = (averageValue <= currentValue)?
                    EnumRecommendationTemplate.INSIGHT_A3_AFTERNOON_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_A3_AFTERNOON_CONSUMPTION_DECR;
                break;
            case NIGHT:
                t = (averageValue <= currentValue)?
                    EnumRecommendationTemplate.INSIGHT_A3_NIGHT_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_A3_NIGHT_CONSUMPTION_DECR;
                break;
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

            parameters.put("average_value", averageValue);
            parameters.put("average_consumption", averageValue);

            Double percentChange = 100.0 * Math.abs(((currentValue - averageValue) / averageValue));
            parameters.put("percent_change", Integer.valueOf(percentChange.intValue()));

            parameters.put("part_of_day", partOfDay);

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
        final int N = 30;       // number of past days to examine
        final double F = 0.6;   // a threshold ratio of non-nulls for collected values
        final double dailyThreshold = config.getVolumeThreshold(deviceType, EnumTimeUnit.DAY);

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;
        
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();
        
        // For each part of day, compare consumption to past days
        
        List<MessageResolutionStatus<ParameterizedTemplate>> results = new ArrayList<>();
        
        for (EnumPartOfDay partOfDay: EnumPartOfDay.values()) {
            final double threshold = dailyThreshold * partOfDay.asFractionOfDay();
            final Interval r = partOfDay.toInterval(refDate);
            
            // Compute for part-of-day for target day

            query = queryBuilder
                .absolute(r.getStart(), r.getEnd(), EnumTimeAggregation.ALL)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            Double targetValue = (series != null)? 
                series.get(EnumDataField.VOLUME, EnumMetric.SUM) : null;
            if (targetValue == null || targetValue < threshold)
                continue; // skip part-of-day; nothing to compare to
         
            // Compute for part-of-day for past N days

            // Note: not so efficient to query for each partOfDay; 
            // query at HOUR granularity for the whole day and keep separate sums
            DateTime start = refDate;
            SummaryStatistics summary = new SummaryStatistics();
            for (int i = 0; i < N; i++) {
                start = start.minusDays(1);
                Interval r1 = partOfDay.toInterval(start);
                query = queryBuilder
                    .absolute(r1.getStart(), r1.getEnd(), EnumTimeAggregation.ALL)
                    .build();
                queryResponse = dataService.execute(query);
                series = queryResponse.getFacade(deviceType);
                Double val = (series != null)? 
                    series.get(EnumDataField.VOLUME, EnumMetric.SUM) : null;
                if (val != null)
                    summary.addValue(val);
            }
            if (summary.getN() < N * F)
                continue; // skip part-of-day; too few values
            
            // Seems we have sufficient data for the past days

            double averageValue = summary.getMean();
            if (averageValue < threshold)
                continue; // skip; not reliable, consumption is too low

            double percentChange = 100.0 * (targetValue - averageValue) / averageValue;
            double score = Math.abs(percentChange) / (2 * CHANGE_PERCENTAGE_THRESHOLD);

            debug(
                "%s/%s: Computed consumption at %s of period P%dD to %s: %.2f μ=%.2f score=%.2f",
                 accountKey, deviceType, partOfDay, N, refDate.toString("dd/MM/YYYY"),
                 targetValue, averageValue, score);
            
            ParameterizedTemplate parameterizedTemplate = 
                new Parameters(refDate, deviceType, partOfDay, targetValue, averageValue);
            results.add(
                new ScoringMessageResolutionStatus<>(score, parameterizedTemplate));
        }
        
        return results;
    }

}
