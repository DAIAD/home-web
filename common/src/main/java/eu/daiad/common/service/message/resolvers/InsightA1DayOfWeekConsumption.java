package eu.daiad.common.service.message.resolvers;

import static eu.daiad.common.model.query.Point.betweenTime;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.common.annotate.message.MessageGenerator;
import eu.daiad.common.model.EnumDayOfWeek;
import eu.daiad.common.model.EnumTimeAggregation;
import eu.daiad.common.model.EnumTimeUnit;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.message.EnumRecommendationTemplate;
import eu.daiad.common.model.message.Message;
import eu.daiad.common.model.message.MessageResolutionStatus;
import eu.daiad.common.model.message.Recommendation.ParameterizedTemplate;
import eu.daiad.common.model.message.ScoringMessageResolutionStatus;
import eu.daiad.common.model.query.DataQuery;
import eu.daiad.common.model.query.DataQueryBuilder;
import eu.daiad.common.model.query.DataQueryResponse;
import eu.daiad.common.model.query.EnumDataField;
import eu.daiad.common.model.query.EnumMeasurementDataSource;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.SeriesFacade;
import eu.daiad.common.service.ICurrencyRateService;
import eu.daiad.common.service.IDataService;
import eu.daiad.common.service.message.AbstractRecommendationResolver;

@MessageGenerator(period = "P1D")
@Component
@Scope("prototype")
public class InsightA1DayOfWeekConsumption extends AbstractRecommendationResolver
{
    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for daily volume consumption */
        private static final String MIN_VALUE = "1E+0";

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double averageValue;

        public Parameters()
        {}

        public Parameters(
            DateTime refDate, EnumDeviceType deviceType, double currentValue, double averageValue)
        {
            super(refDate, deviceType);
            this.averageValue = averageValue;
            this.currentValue = currentValue;
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

        @JsonIgnore
        public EnumDayOfWeek getDayOfWeek()
        {
            return EnumDayOfWeek.valueOf(refDate.getDayOfWeek());
        }

        @JsonIgnore
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            boolean incr = (averageValue <= currentValue);
            if (deviceType == EnumDeviceType.AMPHIRO)
                return incr?
                    EnumRecommendationTemplate.INSIGHT_A1_SHOWER_DAYOFWEEK_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_A1_SHOWER_DAYOFWEEK_CONSUMPTION_DECR;
            else
                return incr?
                    EnumRecommendationTemplate.INSIGHT_A1_METER_DAYOFWEEK_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_A1_METER_DAYOFWEEK_CONSUMPTION_DECR;
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

            parameters.put("day", refDate.toDate());
            parameters.put("day_of_week", getDayOfWeek());

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
        final double K = 1.50;  // a threshold (z-score) of significant change
        final int N = 15;       // number of past weeks to examine
        final double F = 0.5;   // a threshold ratio of non-nulls for collected values
        final double dailyThreshold = config.getVolumeThreshold(deviceType, EnumTimeUnit.DAY);

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;
        Interval interval = null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Compute for target day

        DateTime start = refDate.withTimeAtStartOfDay();

        query = queryBuilder
            .sliding(start, +1, EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        interval = query.getTime().asInterval();
        Double targetValue = (series != null)?
            series.get(EnumDataField.VOLUME, EnumMetric.SUM, betweenTime(interval)):
            null;
        if (targetValue == null || targetValue < dailyThreshold)
            return Collections.emptyList(); // nothing to compare to

        // Compute for past N weeks for a given day-of-week

        SummaryStatistics summary = new SummaryStatistics();
        for (int i = 0; i < N; i++) {
            start = start.minusWeeks(1);
            query = queryBuilder
                .sliding(start, +1, EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            interval = query.getTime().asInterval();
            Double val = (series != null)?
                series.get(EnumDataField.VOLUME, EnumMetric.SUM, betweenTime(interval)):
                null;
            if (val != null)
                summary.addValue(val);
        }
        if (summary.getN() < N * F)
            return Collections.emptyList(); // too few values

        // Seems we have sufficient data for the past weeks

        double averageValue = summary.getMean();
        if (averageValue < dailyThreshold)
            return Collections.emptyList(); // not reliable; consumption is too low

        double sd = Math.sqrt(summary.getPopulationVariance());
        double normValue = (sd > 0)? ((targetValue - averageValue) / sd) : Double.POSITIVE_INFINITY;
        double score = (sd > 0)? (Math.abs(normValue) / (2 * K)) : Double.POSITIVE_INFINITY;

        debug(
            "%s/%s: Computed consumption for %s of period P%dW to %s: " +
                "%.2f μ=%.2f σ=%.2f x*=%.2f score=%.2f",
             accountKey, deviceType, refDate.toString("EEEE"), N, refDate.toString("dd/MM/YYYY"),
             targetValue, averageValue, sd, normValue, score);

        ParameterizedTemplate parameterizedTemplate =
            new Parameters(refDate, deviceType, targetValue, averageValue);
        MessageResolutionStatus<ParameterizedTemplate> result =
            new ScoringMessageResolutionStatus<>(score, parameterizedTemplate);
        return Collections.singletonList(result);
    }
}
