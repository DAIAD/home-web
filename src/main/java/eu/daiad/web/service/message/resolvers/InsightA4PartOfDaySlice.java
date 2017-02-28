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
import eu.daiad.web.model.EnumPartOfDay;
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

@MessageGenerator(period = "P1D")
@Component
@Scope("prototype")
public class InsightA4PartOfDaySlice extends AbstractRecommendationResolver
{  
    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E-1"; 

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
            for (Map.Entry<EnumPartOfDay, Double> e: partialValues.entrySet()) {
                EnumPartOfDay p = e.getKey();
                Double y = e.getValue();
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
        @DecimalMax("1E-3")
        @JsonIgnore
        public Double getError()
        {
            double s = 0.0;
            for (EnumPartOfDay p: EnumPartOfDay.values()) {
                Double y = parts.get(p);
                if (y == null)
                    return null;
                s += y;
            }
            return Math.abs(s - totalValue);
        }

        public Double getPart(EnumPartOfDay partOfDay)
        {
            return parts.get(partOfDay);
        }

        public Parameters withPart(EnumPartOfDay partOfDay, double value)
        {
            this.parts.put(partOfDay, value);
            return this;
        }

        public Parameters withParts(Map<EnumPartOfDay, Double> vals)
        {
            setParts(vals);
            return this;
        }

        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            EnumRecommendationTemplate t = null;

            Double y1 = parts.get(EnumPartOfDay.MORNING); 
            Double y2 = parts.get(EnumPartOfDay.AFTERNOON);
            Double y3 = parts.get(EnumPartOfDay.NIGHT);

            if (y1 == null || y2 == null || y3 == null)
                return null;

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

            Double y1 = parts.get(EnumPartOfDay.MORNING); 
            Double y2 = parts.get(EnumPartOfDay.AFTERNOON);
            Double y3 = parts.get(EnumPartOfDay.NIGHT);

            if (y1 == null || y2 == null || y3 == null)
                return parameters;

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
        
        // Compute for every part-of-day for target day

        EnumMap<EnumPartOfDay, Double> parts = new EnumMap<>(EnumPartOfDay.class);
        double sumOfParts = 0;
        boolean missingPart = false;
        for (EnumPartOfDay partOfDay: EnumPartOfDay.values()) {
            Interval r = partOfDay.toInterval(refDate);
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
            sumOfParts += y;
            parts.put(partOfDay, y);
        }

        if (missingPart || sumOfParts < dailyThreshold)
            return Collections.emptyList(); // not reliable; overall consumption is too low

        // We have sufficient data for all parts of target day

        debug(
            "%s/%s: Consumption for %s is %.2flt: " +
                "morning=%.2f%% afternoon=%.2f%% night=%.2f%%",
             accountKey, deviceType, refDate.toString("dd/MM/YYYY"), sumOfParts,
             100 * parts.get(EnumPartOfDay.MORNING) / sumOfParts,
             100 * parts.get(EnumPartOfDay.AFTERNOON) / sumOfParts,
             100 * parts.get(EnumPartOfDay.NIGHT) / sumOfParts);
        
        ParameterizedTemplate parameterizedTemplate = new Parameters(
                refDate, deviceType, sumOfParts)
            .withParts(parts);
        MessageResolutionStatus<ParameterizedTemplate> result = 
            new SimpleMessageResolutionStatus<>(true, parameterizedTemplate);
        return Collections.singletonList(result);
        
    }

}
