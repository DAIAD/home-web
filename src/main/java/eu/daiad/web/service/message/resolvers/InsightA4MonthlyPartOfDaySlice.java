package eu.daiad.web.service.message.resolvers;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.message.Recommendation.ParameterizedTemplate;
import eu.daiad.web.service.message.AbstractRecommendationResolver;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang.mutable.MutableDouble;
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
import eu.daiad.web.model.EnumPartOfDay;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.EnumTimeUnit;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.EnumMessageLevel;
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
public class InsightA4MonthlyPartOfDaySlice extends AbstractRecommendationResolver
{  
    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for monthly volume consumption */
        private static final String MIN_VALUE = "5E+1"; 

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double totalValue;

        @NotNull
        private EnumMap<EnumPartOfDay, Double> parts = new EnumMap<>(EnumPartOfDay.class);

        public Parameters()
        {
            super();
        }

        public Parameters(
            DateTime refDate, EnumDeviceType deviceType, double totalValue)
        {
            super(refDate, deviceType);
            this.totalValue = totalValue;
        }

        @JsonProperty("totalValue")
        public void setTotalValue(double y)
        {
            this.totalValue = y;
        }

        @JsonProperty("totalValue")
        public Double getTotalValue()
        {
            return totalValue;
        }

        @JsonProperty("parts")
        public void setParts(Map<EnumPartOfDay, Double> partialValues)
        {
            setPartsFromNumbers(partialValues);
        }
        
        private <N extends Number> void setPartsFromNumbers(Map<EnumPartOfDay, N> partialValues)
        {
            for (EnumPartOfDay p: partialValues.keySet()) {
                Double y = partialValues.get(p).doubleValue();
                if (y != null)
                    this.parts.put(p, y);
            }
        }

        @JsonProperty("parts")
        public Map<EnumPartOfDay, Double> getParts()
        {            
            return parts;
        }

        @Size(max = 0)
        @JsonIgnore
        public Set<EnumPartOfDay> getMissingParts()
        {
            return EnumSet.complementOf(EnumSet.copyOf(parts.keySet()));
        }
        
        @NotNull
        @DecimalMax("1E-2")
        @JsonIgnore
        public Double getError()
        {
            double s = 0.0;
            for (EnumPartOfDay p: EnumPartOfDay.values()) {
                double y = parts.get(p).doubleValue();
                s += y;
            }
            return Math.abs(s - totalValue);
        }

        public Double getPart(EnumPartOfDay partOfDay)
        {
            Number n = parts.get(partOfDay);
            return (n == null)? null : n.doubleValue();    
        }

        public Parameters withPart(EnumPartOfDay partOfDay, double value)
        {
            this.parts.put(partOfDay, value);
            return this;
        }

        public <N extends Number> Parameters withParts(Map<EnumPartOfDay, N> vals)
        {
            setPartsFromNumbers(vals);
            return this;
        }

        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;

            double y1 = parts.get(EnumPartOfDay.MORNING).doubleValue(); 
            double y2 = parts.get(EnumPartOfDay.AFTERNOON).doubleValue();
            double y3 = parts.get(EnumPartOfDay.NIGHT).doubleValue();

            if (y1 < y2) {
                t = (y2 < y3)? 
                    EnumRecommendationTemplate.INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT:
                    EnumRecommendationTemplate.INSIGHT_A4_CONSUMPTION_MAINLY_IN_AFTERNOON;
            } else {
                // y2 <= y1
                t = (y1 < y3)?
                    EnumRecommendationTemplate.INSIGHT_A4_CONSUMPTION_MAINLY_IN_NIGHT:
                    EnumRecommendationTemplate.INSIGHT_A4_CONSUMPTION_MAINLY_IN_MORNING;
            }

            return t;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();

            parameters.put("value", totalValue);
            parameters.put("consumption", totalValue); 

            double y1 = parts.get(EnumPartOfDay.MORNING).doubleValue(); 
            double y2 = parts.get(EnumPartOfDay.AFTERNOON).doubleValue();
            double y3 = parts.get(EnumPartOfDay.NIGHT).doubleValue();
            
            Double p1 = 100.0 * (y1 / totalValue);
            Double p2 = 100.0 * (y2 / totalValue);
            Double p3 = 100.0 * (y3 / totalValue);
            
            parameters.put("morning_consumption", y1);
            parameters.put("morning_percentage", Integer.valueOf(p1.intValue()));

            parameters.put("afternoon_consumption", y2);
            parameters.put("afternoon_percentage", Integer.valueOf(p2.intValue()));

            parameters.put("night_consumption", y3);
            parameters.put("night_percentage", Integer.valueOf(p3.intValue()));

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
        final double dailyThreshold = 0.8 * config.getVolumeThreshold(deviceType, EnumTimeUnit.DAY);
        final int N  = 18; // a threshold for number of days used
        
        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();
        
        DateTime end = refDate.withDayOfMonth(1).withTimeAtStartOfDay();
        DateTime start = end.minusMonths(1);
       
        // Initialize partial sums for each part-of-day
        
        EnumMap<EnumPartOfDay, MutableDouble> sumPerPart = new EnumMap<>(EnumPartOfDay.class);
        for (EnumPartOfDay partOfDay: EnumPartOfDay.values())
            sumPerPart.put(partOfDay, new MutableDouble(.0));
        
        // Compute for each day
        
        double consumption = .0;
        int n = 0;
        for (DateTime target = start; target.isBefore(end); target = target.plusDays(1)) {
            // Compute for every part-of-day of target day
            double dailyConsumption = .0;
            boolean missingPart = false;
            EnumMap<EnumPartOfDay, Double> parts = new EnumMap<>(EnumPartOfDay.class);
            for (EnumPartOfDay partOfDay: EnumPartOfDay.values()) {
                Interval r = partOfDay.toInterval(target);
                query = queryBuilder
                    .absolute(r.getStart(), r.getEnd(), EnumTimeAggregation.ALL)
                    .build();
                queryResponse = dataService.execute(query);
                series = queryResponse.getFacade(deviceType);
                Double y = (series != null)? 
                    series.get(EnumDataField.VOLUME, EnumMetric.SUM) : null;
                if (y == null) {
                    missingPart = true;
                    break;
                }
                dailyConsumption += y;
                parts.put(partOfDay, y);
            }
            if (missingPart || dailyConsumption < dailyThreshold)
                continue; // skip; not reliable or non-significant consumption
            // We have sufficient data for target day: update partial sums
            for (EnumPartOfDay partOfDay: EnumPartOfDay.values())
                sumPerPart.get(partOfDay).add(parts.get(partOfDay));
            n++;
            consumption += dailyConsumption;
        }
        
        if (n < N)
            return Collections.emptyList(); // too few days with significant consumption
        
        // We have sufficient data
        
        debug(
            "%s/%s: Consumption for P1M to %s: %.2f: " +
                "morning=%.2f%% afternoon=%.2f%% night=%.2f%%",
             accountKey, deviceType, end.toString("dd/MM/YYYY"), consumption,
             100 * sumPerPart.get(EnumPartOfDay.MORNING).doubleValue() / consumption,
             100 * sumPerPart.get(EnumPartOfDay.AFTERNOON).doubleValue() / consumption,
             100 * sumPerPart.get(EnumPartOfDay.NIGHT).doubleValue() / consumption);
        
        ParameterizedTemplate parameterizedTemplate = new Parameters(
                refDate, deviceType, consumption)
            .withParts(sumPerPart);
        
        MessageResolutionStatus<ParameterizedTemplate> result = 
            new SimpleMessageResolutionStatus<>(parameterizedTemplate);
        return Collections.singletonList(result);
    }

}
