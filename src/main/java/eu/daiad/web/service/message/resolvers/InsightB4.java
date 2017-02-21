package eu.daiad.web.service.message.resolvers;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.FluentIterable;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.joda.time.DateTime;
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
import eu.daiad.web.model.query.Point;
import eu.daiad.web.model.query.EnumMeasurementDataSource;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractRecommendationResolver;

@MessageGenerator(period = "P1W", dayOfWeek = EnumDayOfWeek.MONDAY, maxPerWeek = 1)
@Component
@Scope("prototype")
public class InsightB4 extends AbstractRecommendationResolver
{
    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E-1"; 

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double weekdayValue;        

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double weekendValue;

        public Parameters()
        {
            super();
        }

        public Parameters(
            DateTime refDate, EnumDeviceType deviceType, double weekdayValue, double weekendValue)
        {
            super(refDate, deviceType);
            this.weekdayValue = weekdayValue;
            this.weekendValue = weekendValue;
        }

        @JsonProperty("weekdayValue")
        public void setWeekdayValue(double y)
        {
            this.weekdayValue = y;
        }

        @JsonProperty("weekdayValue")
        public Double getWeekdayValue()
        {
            return weekdayValue;
        }

        @JsonProperty("weekendValue")
        public void setWeekendValue(double y)
        {
            this.weekendValue = y;
        }

        @JsonProperty("weekendValue")
        public Double getWeekendValue()
        {
            return weekendValue;
        }

        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            return (weekdayValue < weekendValue)?
                EnumRecommendationTemplate.INSIGHT_B4_MORE_ON_WEEKEND:
                EnumRecommendationTemplate.INSIGHT_B4_LESS_ON_WEEKEND;    
        }

        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();

            parameters.put("weekday_consumption", weekdayValue);

            parameters.put("weekend_consumption", weekendValue);

            Double percentChange = 100.0 * Math.abs((weekendValue - weekdayValue) / weekdayValue);
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
        final double F = 0.6; // a threshold ratio of non-nulls for collected values
        final DateTime targetDate = EnumTimeUnit.WEEK.startOf(refDate.minusWeeks(1));
        final DateTimeZone tz = refDate.getZone();
        final int N = 8; // number of weeks to examine
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
        
        // Initialize sums for working/weekend days

        Map<EnumDayOfWeek.Type, Sum> sumPerType = new EnumMap<>(EnumDayOfWeek.Type.class);
        sumPerType.put(EnumDayOfWeek.Type.WEEKDAY, new Sum());
        sumPerType.put(EnumDayOfWeek.Type.WEEKEND, new Sum());
        
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
                .filter(Point.betweenTime(start, end));
            // Update partial sums for each type (working/weekend day)
            for (Point p: points) {
                DateTime t = p.getTimestamp().toDateTime(tz);
                double value = p.getValue();
                EnumDayOfWeek day = EnumDayOfWeek.valueOf(t.getDayOfWeek());
                sumPerType.get(day.getType()).increment(value);
            }
        }
        
        // Do we have sufficient data?

        Sum weekdaySum = sumPerType.get(EnumDayOfWeek.Type.WEEKDAY);
        Sum weekendSum = sumPerType.get(EnumDayOfWeek.Type.WEEKEND);

        final int N1 = (int) (N * F);
        if (weekdaySum.getN() < 5 * N1 || weekendSum.getN() < 2 * N1)
            return Collections.emptyList();
        
        // Compute average for each type (working/weekend) day

        double weekdayAverage = weekdaySum.getResult() / weekdaySum.getN();
        double weekendAverage = weekendSum.getResult() / weekendSum.getN();
        if (weekdayAverage < dailyThreshold && weekendAverage < dailyThreshold)
            return Collections.emptyList(); // not reliable; both parts have too low consumption

        debug(
            "%s/%s: Computed consumption for %d weeks to %s: " +
                "weekday-average=%.2f weekend-average=%.2f",
             accountKey, deviceType, N, targetDate.plusWeeks(1).toString("dd/MM/YYYY"),
             weekdayAverage, weekendAverage);
        
        ParameterizedTemplate parameterizedTemplate =
            new Parameters(refDate, deviceType, weekdayAverage, weekendAverage);
        MessageResolutionStatus<ParameterizedTemplate> result =
            new SimpleMessageResolutionStatus<>(true, parameterizedTemplate);
        
        return Collections.singletonList(result);
    }

}
