package eu.daiad.common.service.message.resolvers;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.common.annotate.message.MessageGenerator;
import eu.daiad.common.model.EnumDayOfWeek;
import eu.daiad.common.model.EnumStatistic;
import eu.daiad.common.model.EnumTimeAggregation;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.message.Alert.ParameterizedTemplate;
import eu.daiad.common.model.message.EnumAlertTemplate;
import eu.daiad.common.model.message.Message;
import eu.daiad.common.model.message.MessageResolutionStatus;
import eu.daiad.common.model.message.SimpleMessageResolutionStatus;
import eu.daiad.common.model.query.DataQuery;
import eu.daiad.common.model.query.DataQueryBuilder;
import eu.daiad.common.model.query.DataQueryResponse;
import eu.daiad.common.model.query.EnumDataField;
import eu.daiad.common.model.query.EnumMeasurementDataSource;
import eu.daiad.common.model.query.EnumMeasurementField;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.Point;
import eu.daiad.common.model.query.SeriesFacade;
import eu.daiad.common.service.ICurrencyRateService;
import eu.daiad.common.service.IDataService;
import eu.daiad.common.service.message.AbstractAlertResolver;

@MessageGenerator(period = "P1W", dayOfWeek = EnumDayOfWeek.MONDAY, maxPerWeek = 1)
@Component
@Scope("prototype")
public class AlertExcessiveWaterConsumption extends AbstractAlertResolver
{
    public static final double HIGH_CONSUMPTION_RATIO = 2.0; // in terms of average consumption

    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        @NotNull
        @DecimalMin("1E+0")
        private Double value;

        @NotNull
        @DecimalMin("1E+0")
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

        @NotNull
        @DecimalMin("1")
        @JsonIgnore
        public Double getPercentAbove()
        {
            if (value == null || averageValue == null)
                return null;
            return 100.0 * ((value - averageValue) / averageValue);
        }


        @Override
        public Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
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

            parameters.put("percent_above", getPercentAbove());

            parameters.put("annual_savings", annualSavings);

            return parameters;
        }

        @Override
        @JsonIgnore
        public EnumAlertTemplate getTemplate()
        {
            return (deviceType == EnumDeviceType.AMPHIRO)?
                EnumAlertTemplate.TOO_MUCH_WATER_SHOWER: EnumAlertTemplate.TOO_MUCH_WATER_METER;
        }
    }

    @Autowired
    IDataService dataService;

    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        final Period period = Period.weeks(1);
        final EnumTimeAggregation granularity = EnumTimeAggregation.WEEK;
        final EnumMeasurementField measurementField =
            EnumMeasurementField.valueOf(deviceType, EnumDataField.VOLUME);

        DateTime end = refDate.withDayOfWeek(DateTimeConstants.MONDAY)
            .withTimeAtStartOfDay();

        Double averageConsumption = statisticsService.getNumber(
                end, period, measurementField, EnumStatistic.AVERAGE_PER_USER)
            .getValue();
        if (averageConsumption == null)
            return Collections.emptyList();

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .absolute(end.minus(period), end, granularity)
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(deviceType);

        Interval interval = query.getTime().asInterval();
        Double userConsumption = (series != null)?
            series.aggregate(
                EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval), new Sum()):
            null;

        if (userConsumption != null && userConsumption > HIGH_CONSUMPTION_RATIO * averageConsumption) {
            // Get a rough estimate for annual savings if average behavior is adopted
            final int numWeeksPerYear = 52; // not exactly, it's 52 or 53
            Double annualSavings = (userConsumption - averageConsumption) * numWeeksPerYear;
            ParameterizedTemplate parameterizedTemplate = new Parameters(
                    refDate, deviceType, userConsumption, averageConsumption)
                .withAnnualSavings(annualSavings);
            MessageResolutionStatus<ParameterizedTemplate> result =
                new SimpleMessageResolutionStatus<>(parameterizedTemplate);
            return Collections.singletonList(result);
        }

        return Collections.emptyList();
    }

}
