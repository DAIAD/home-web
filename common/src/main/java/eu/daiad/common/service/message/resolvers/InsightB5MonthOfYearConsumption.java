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
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.common.annotate.message.MessageGenerator;
import eu.daiad.common.model.EnumTimeAggregation;
import eu.daiad.common.model.EnumTimeUnit;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.message.EnumRecommendationTemplate;
import eu.daiad.common.model.message.Message;
import eu.daiad.common.model.message.MessageResolutionStatus;
import eu.daiad.common.model.message.Recommendation.ParameterizedTemplate;
import eu.daiad.common.model.message.SimpleMessageResolutionStatus;
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


@MessageGenerator(period = "P1M", dayOfMonth = 2, maxPerMonth = 1)
@Component
@Scope("prototype")
public class InsightB5MonthOfYearConsumption extends AbstractRecommendationResolver
{
    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        /** A minimum value for monthly volume consumption */
        private static final String MIN_VALUE = "2E+1";

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
            boolean incr = (previousValue < currentValue);
            return (deviceType == EnumDeviceType.AMPHIRO)?
                (incr?
                    EnumRecommendationTemplate.INSIGHT_B5_SHOWER_MONTHLY_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_B5_SHOWER_MONTHLY_CONSUMPTION_DECR):
                (incr?
                    EnumRecommendationTemplate.INSIGHT_B5_METER_MONTHLY_CONSUMPTION_INCR:
                    EnumRecommendationTemplate.INSIGHT_B5_METER_MONTHLY_CONSUMPTION_DECR);
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
        Interval interval;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(tz)
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Compute for target month

        query = queryBuilder
            .sliding(targetDate, +1, EnumTimeUnit.MONTH, EnumTimeAggregation.MONTH)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        interval = query.getTime().asInterval();
        Double targetValue = (series != null)?
            series.get(EnumDataField.VOLUME, EnumMetric.SUM, betweenTime(interval)):
            null;
        if (targetValue == null || targetValue < monthlyThreshold)
            return Collections.emptyList(); // nothing to compare to

        // Compute for same month a year ago

        query = queryBuilder
            .sliding(targetDate.minusYears(1), +1, EnumTimeUnit.MONTH, EnumTimeAggregation.MONTH)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        interval = query.getTime().asInterval();
        Double previousValue = (series != null)?
            series.get(EnumDataField.VOLUME, EnumMetric.SUM, betweenTime(interval)):
            null;
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
            new SimpleMessageResolutionStatus<>(parameterizedTemplate);

        return Collections.singletonList(result);
    }

}
