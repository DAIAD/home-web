package eu.daiad.web.service.message.resolvers;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.annotate.message.MessageGenerator;
import eu.daiad.web.model.EnumStatistic;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert.ParameterizedTemplate;
import eu.daiad.web.model.message.EnumAlertTemplate;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.message.SimpleMessageResolutionStatus;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumDataField;
import eu.daiad.web.model.query.EnumMeasurementField;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.Point;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractAlertResolver;

@MessageGenerator(period = "P1M", dayOfMonth = 1, maxPerMonth = 1)
@Component
@Scope("prototype")
public class AlertWaterSaver extends AbstractAlertResolver
{
    private static final Set<EnumDeviceType> supportedDevices = EnumSet.of(EnumDeviceType.METER);

    private static final double CHANGE_PERCENTAGE_HIGH = 25.0;

    private static final double CHANGE_PERCENTAGE_THRESHOLD = 10.0; // < CHANGE_PERCENTAGE_HIGH

    public static class Parameters extends Message.AbstractParameters
    implements ParameterizedTemplate
    {
        @NotNull
        @DecimalMin("1E+0")
        private Double value;

        @NotNull
        @DecimalMin("1E+0")
        private Double previousValue;

        @NotNull
        @DecimalMin("1E+1")
        private Double averageValue;

        public Parameters()
        {}

        protected Parameters(
            DateTime refDate, EnumDeviceType deviceType,
            double value, double previousValue, double averageValue)
        {
            super(refDate, deviceType);
            this.value = value;
            this.previousValue = previousValue;
            this.averageValue = averageValue;
        }

        @JsonProperty("value")
        public Double getValue()
        {
            return value;
        }

        @JsonProperty("value")
        public void setValue(double value)
        {
            this.value = value;
        }

        @JsonProperty("averageValue")
        public Double getAverageValue()
        {
            return averageValue;
        }

        @JsonProperty("averageValue")
        public void setAverageValue(double averageValue)
        {
            this.averageValue = averageValue;
        }

        @JsonProperty("previousValue")
        public Double getPreviousValue()
        {
            return previousValue;
        }

        @JsonProperty("previousValue")
        public void setPreviousValue(double previousValue)
        {
            this.previousValue = previousValue;
        }

        @JsonIgnore
        @NotNull
        @DecimalMin("5.0")
        public Double getPercentChange()
        {
            return (value != null && previousValue != null)?
                ( 100.0 * (previousValue - value) / previousValue) : null;
        }

        @Override
        public ParameterizedTemplate withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }

        @Override
        @JsonIgnore
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();

            parameters.put("value", value);
            parameters.put("consumption", value);

            parameters.put("previous_value", previousValue);
            parameters.put("previous_consumption", previousValue);

            parameters.put("average_value", averageValue);
            parameters.put("average_consumption", averageValue);

            parameters.put("percent_change", Integer.valueOf(getPercentChange().intValue()));

            return parameters;
        }

        @Override
        @JsonIgnore
        public EnumAlertTemplate getTemplate()
        {
            return EnumAlertTemplate.GOOD_JOB_MONTHLY;
        }
    }

    @Autowired
    IDataService dataService;

    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        Assert.state(deviceType == EnumDeviceType.METER);

        final Period period = Period.months(1);
        final EnumTimeAggregation granularity = EnumTimeAggregation.MONTH;
        final EnumMeasurementField measurementField = EnumMeasurementField.METER_VOLUME;

        DateTime end = refDate.withDayOfMonth(1)
            .withTimeAtStartOfDay();

        Double monthlyAverage = statisticsService.getNumber(
                end, period, measurementField, EnumStatistic.AVERAGE_PER_USER)
            .getValue();
        if (monthlyAverage == null)
            return Collections.emptyList();

        DataQuery query = null;
        DataQueryResponse queryResponse = null;
        SeriesFacade series = null;
        Interval interval = null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .meter()
            .sum();

        DateTime t1 = end.minus(period);

        query = queryBuilder.absolute(t1, end, granularity)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(EnumDeviceType.METER);
        interval = query.getTime().asInterval();
        Double c0 = (series != null)?
            series.get(EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval)):
            null;
        if (c0 == null)
            return Collections.emptyList();

        query = queryBuilder.absolute(t1.minus(period), t1, granularity)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(EnumDeviceType.METER);
        interval = query.getTime().asInterval();
        Double c1 = (series != null)?
            series.get(EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval)):
            null;
        if (c1 == null)
            return Collections.emptyList();

        Double percentChange = 100 * ((c1 - c0) / c1);
        boolean fire = (
            (percentChange > CHANGE_PERCENTAGE_HIGH) ||
            (percentChange > CHANGE_PERCENTAGE_THRESHOLD && c0 < monthlyAverage));
        if (fire) {
            ParameterizedTemplate parameterizedTemplate =
                new Parameters(refDate, EnumDeviceType.METER, c0, c1, monthlyAverage);
            MessageResolutionStatus<ParameterizedTemplate> result =
                new SimpleMessageResolutionStatus<>(parameterizedTemplate);
            return Collections.singletonList(result);
        }
        return Collections.emptyList();
    }

    @Override
    public Set<EnumDeviceType> getSupportedDevices()
    {
        return Collections.unmodifiableSet(supportedDevices);
    }
}
