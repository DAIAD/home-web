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

import org.apache.commons.math3.stat.descriptive.summary.Sum;
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
import eu.daiad.web.model.EnumTimeUnit;
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
public class AlertWaterEfficiencyLeader extends AbstractAlertResolver
{
    private static final Set<EnumDeviceType> supportedDevices = EnumSet.of(EnumDeviceType.METER);

    public static class Parameters extends Message.AbstractParameters
    implements ParameterizedTemplate
    {
        @NotNull
        @DecimalMin("1E+0")
        private Double value;

        @NotNull
        @DecimalMin("1E+1")
        private Double averageValue;

        @NotNull
        @DecimalMin("1E+2")
        private Double annualSavings;

        public Parameters()
        {}

        protected Parameters(
            DateTime refDate, EnumDeviceType deviceType, double value, double averageValue)
        {
            super(refDate, deviceType);
            this.value = value;
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

        @JsonProperty("annualSavings")
        public Double getAnnualSavings()
        {
            return annualSavings;
        }

        public Parameters withAnnualSavings(double annualSavings)
        {
            this.annualSavings = annualSavings;
            return this;
        }

        @JsonProperty("annualSavings")
        public void setAnnualSavings(double annualSavings)
        {
            this.annualSavings = annualSavings;
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

            parameters.put("average_value", averageValue);
            parameters.put("average_consumption", averageValue);

            parameters.put("annual_savings", Integer.valueOf(annualSavings.intValue()));

            return parameters;
        }

        @Override
        @JsonIgnore
        public EnumAlertTemplate getTemplate()
        {
            return EnumAlertTemplate.WATER_EFFICIENCY_LEADER;
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
        final DateTime end = refDate.withDayOfMonth(1).withTimeAtStartOfDay();

        Double monthlyAverage = statisticsService.getNumber(
                end, period, measurementField, EnumStatistic.AVERAGE_PER_USER)
            .getValue();
        Double monthly10pThreshold = statisticsService.getNumber(
                end, period, measurementField, EnumStatistic.PERCENTILE_10P_OF_USERS)
            .getValue();

        if (monthlyAverage == null || monthly10pThreshold == null)
            return Collections.emptyList();

        double monthlyThreshold = config.getVolumeThreshold(EnumDeviceType.METER, EnumTimeUnit.MONTH);

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .absolute(end.minus(period), end, granularity)
            .user("user", accountKey)
            .meter()
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.METER);

        Interval interval = query.getTime().asInterval();
        Double consumption = (series != null)?
            series.aggregate(
                EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval), new Sum()):
            null;
        if (consumption == null || consumption < monthlyThreshold)
            return Collections.emptyList();

        if (consumption < Math.min(monthly10pThreshold, monthlyAverage)) {
            final int monthsPerYear = 12;
            ParameterizedTemplate parameterizedTemplate = new Parameters(
                    refDate, deviceType, consumption, monthlyAverage)
                .withAnnualSavings(monthsPerYear * (monthlyAverage - consumption));
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
