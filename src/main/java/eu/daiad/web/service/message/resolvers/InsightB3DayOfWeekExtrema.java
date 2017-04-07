package eu.daiad.web.service.message.resolvers;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.FluentIterable;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
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
import eu.daiad.web.model.query.Point;
import eu.daiad.web.model.query.EnumMeasurementDataSource;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractRecommendationResolver;

import static eu.daiad.web.model.query.Point.betweenTime;


@MessageGenerator(period = "P2W", dayOfWeek = EnumDayOfWeek.MONDAY, maxPerWeek = 1)
@Component
@Scope("prototype")
public class InsightB3DayOfWeekExtrema extends AbstractRecommendationResolver
{
    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {        
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E+0"; 

        /** The average daily consumption for the particular day-of-week */
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;

        /** The average daily consumption for all week days */
        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double averageValue;

        /** The day of week for this consumption peak (high or low) */
        @NotNull
        private EnumDayOfWeek dayOfWeek; 

        public Parameters()
        {
            super();
        }

        public Parameters(
            DateTime refDate, EnumDeviceType deviceType, 
            double currentValue, double averageValue, EnumDayOfWeek dayOfWeek)
        {
            super(refDate, deviceType);
            this.averageValue = averageValue;
            this.currentValue = currentValue;
            this.dayOfWeek = dayOfWeek;
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

        @JsonProperty("dayOfWeek")
        public EnumDayOfWeek getDayOfWeek()
        {
            return dayOfWeek;
        }

        @JsonProperty("dayOfWeek")
        public void setDayOfWeek(EnumDayOfWeek day)
        {
            this.dayOfWeek = day;
        }

        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            boolean isMin = (currentValue < averageValue);
            return (deviceType == EnumDeviceType.AMPHIRO)?
                (isMin?
                    EnumRecommendationTemplate.INSIGHT_B3_SHOWER_DAYOFWEEK_CONSUMPTION_LOW:
                    EnumRecommendationTemplate.INSIGHT_B3_SHOWER_DAYOFWEEK_CONSUMPTION_PEAK):
                (isMin?
                    EnumRecommendationTemplate.INSIGHT_B3_METER_DAYOFWEEK_CONSUMPTION_LOW:
                    EnumRecommendationTemplate.INSIGHT_B3_METER_DAYOFWEEK_CONSUMPTION_PEAK);
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

            int dow = dayOfWeek.toInteger();
            parameters.put("day", refDate.withDayOfWeek(dow).toDate());
            parameters.put("day_of_week", dayOfWeek);

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
        final double F = 0.6; // a threshold ratio of non-nulls for collected values
        final DateTime targetDate = EnumTimeUnit.WEEK.startOf(refDate.minusWeeks(1));
        final DateTimeZone tz = refDate.getZone();
        final int N = 9; // number of weeks to examine
        final double dailyThreshold = config.getVolumeThreshold(deviceType, EnumTimeUnit.DAY);
        
        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(tz)
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();
        
        // Initialize sums for each day of week, and sum for all days

        Map<EnumDayOfWeek, Sum> sumPerDay = new EnumMap<>(EnumDayOfWeek.class);
        for (EnumDayOfWeek day: EnumDayOfWeek.values())
            sumPerDay.put(day, new Sum());
        Sum sum = new Sum();
        
        // Fetch data for N past weeks

        DateTime start = targetDate.plusWeeks(1);
        for (int i = 0; i < N; i++) {
            DateTime end = start;
            start = start.minusWeeks(1);
            // Execute query for current week
            query = queryBuilder
                .absolute(start, end, EnumTimeAggregation.DAY)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            if (series == null)
                continue;
            FluentIterable<Point> points = FluentIterable
                .of(series.iterPoints(EnumDataField.VOLUME, EnumMetric.SUM))
                .filter(betweenTime(start, end));
            // Update partial sums for each day of week
            for (Point p: points) {
                DateTime t = p.getTimestamp().toDateTime(tz);
                double value = p.getValue();
                EnumDayOfWeek day = EnumDayOfWeek.valueOf(t.getDayOfWeek());
                sumPerDay.get(day).increment(value);
                sum.increment(value);
            }
        }
        
        // Do we have sufficient data for each day?

        boolean sufficient = true;
        for (EnumDayOfWeek day: EnumDayOfWeek.values())
            if (sumPerDay.get(day).getN() < N * F) {
                sufficient = false;
                break;
            }
        if (!sufficient)
            return Collections.emptyList();
        
        // Compute average daily consumption for each day-of-week; Find peak days

        double minOfDay = Double.POSITIVE_INFINITY, maxOfDay = Double.NEGATIVE_INFINITY;
        EnumDayOfWeek dayMin = null, dayMax = null;
        for (EnumDayOfWeek day: EnumDayOfWeek.values()) {
            Sum sy = sumPerDay.get(day);
            double y = sy.getResult() / sy.getN();
            if (y < minOfDay) {
                minOfDay = y;
                dayMin = day;
            }
            if (y > maxOfDay) {
                maxOfDay = y;
                dayMax = day;
            }
        }

        if (maxOfDay < dailyThreshold)
            return Collections.emptyList(); // not reliable; overall consumption is too low
     
        // Compute average daily consumption for all days

        double avg = sum.getResult() / sum.getN();

        // Produce 2 insights, one for each peak (min, max)

        debug(
            "%s/%s: Computed consumption for %d weeks to %s: " +
                "min=%.2f dayMin=%s - max=%.2f dayMax=%s - average=%.2f",
             accountKey, deviceType, N, targetDate.plusWeeks(1).toString("dd/MM/YYYY"),
             minOfDay, dayMin, maxOfDay, dayMax, avg);

        ParameterizedTemplate p1 = 
            new Parameters(refDate, deviceType, minOfDay, avg, dayMin);
        ParameterizedTemplate p2 =
            new Parameters(refDate, deviceType, maxOfDay, avg, dayMax);
        
        return Arrays.<MessageResolutionStatus<ParameterizedTemplate>>asList(
            new SimpleMessageResolutionStatus<>(p1),
            new SimpleMessageResolutionStatus<>(p2)
        );
    }
}
