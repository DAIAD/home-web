package eu.daiad.common.service.message.resolvers;

import static eu.daiad.common.model.query.Point.betweenTime;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
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


@MessageGenerator(period = "P1W", dayOfWeek = EnumDayOfWeek.MONDAY, maxPerWeek = 1)
@Component
@Scope("prototype")
public class InsightB2WeeklyConsumption extends AbstractRecommendationResolver
{
    public static final double CHANGE_PERCENTAGE_THRESHOLD = 60.0;

    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for weekly volume consumption */
        private static final String MIN_VALUE = "1E+1";

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double currentValue;

        @NotNull
        @DecimalMin(MIN_VALUE)
        private Double previousValue;

        public Parameters()
        {
            super();
        }

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
            boolean incr = (previousValue < currentValue);
            return (deviceType == EnumDeviceType.AMPHIRO)?
                (incr?
                    EnumRecommendationTemplate.INSIGHT_B2_SHOWER_WEEKLY_PREV_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_B2_SHOWER_WEEKLY_PREV_CONSUMPTION_DECR):
                (incr?
                    EnumRecommendationTemplate.INSIGHT_B2_METER_WEEKLY_PREV_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_B2_METER_WEEKLY_PREV_CONSUMPTION_DECR);
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
        final DateTime targetDate = refDate.minusWeeks(1) // target previous week
            .withDayOfWeek(DateTimeConstants.MONDAY)
            .withTimeAtStartOfDay();

        final double threshold = config.getVolumeThreshold(deviceType, EnumTimeUnit.WEEK);

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;
        Interval interval;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Compute for target period

        query = queryBuilder
            .sliding(targetDate, +1, EnumTimeUnit.WEEK, EnumTimeAggregation.WEEK)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        interval = query.getTime().asInterval();
        Double targetValue = (series != null)?
            series.get(EnumDataField.VOLUME, EnumMetric.SUM, betweenTime(interval)):
            null;
        if (targetValue == null || targetValue < threshold)
            return Collections.emptyList(); // nothing to compare to

        // Compute for previous period

        query = queryBuilder
            .sliding(targetDate.minusWeeks(1), +1, EnumTimeUnit.WEEK, EnumTimeAggregation.WEEK)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        interval = query.getTime().asInterval();
        Double previousValue = (series != null)?
            series.get(EnumDataField.VOLUME, EnumMetric.SUM, betweenTime(interval)):
            null;
        if (previousValue == null || previousValue < threshold)
            return Collections.emptyList(); // nothing to compare to

        // Seems we have sufficient data

        double percentChange = 100.0 * (targetValue - previousValue) / previousValue;
        double score = Math.abs(percentChange) / (2 * CHANGE_PERCENTAGE_THRESHOLD);

        debug(
            "%s/%s: Computed consumption for previous P1W of %s: " +
                "%.2f previous=%.2f change=%.2f%% score=%.2f",
            accountKey, deviceType, targetDate.toString("dd/MM/YYYY"),
            targetValue, previousValue, percentChange, score);

        ParameterizedTemplate parameterizedTemplate =
            new Parameters(refDate, deviceType, targetValue, previousValue);
        MessageResolutionStatus<ParameterizedTemplate> result =
            new ScoringMessageResolutionStatus<>(score, parameterizedTemplate);
        return Collections.singletonList(result);
    }

}
