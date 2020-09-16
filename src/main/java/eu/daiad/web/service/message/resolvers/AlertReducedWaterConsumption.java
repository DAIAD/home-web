package eu.daiad.web.service.message.resolvers;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.annotate.message.MessageGenerator;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.model.EnumDayOfWeek;
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
import eu.daiad.web.model.query.EnumMeasurementDataSource;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.Point;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractAlertResolver;


@MessageGenerator(period = "P1W", dayOfWeek = EnumDayOfWeek.MONDAY, maxPerWeek = 1)
@Component
@Scope("prototype")
public class AlertReducedWaterConsumption extends AbstractAlertResolver
{
    public static final double CHANGE_PERCENTAGE_THRESHOLD = 20.0;

    public static final double CHANGE_PERCENTAGE_CEIL = 60.0;

    @Autowired
    IDataService dataService;

    @Autowired
    IUserRepository userRepository;

    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        @NotNull
        @DecimalMin("1E+0")
        private Double value;

        @NotNull
        @DecimalMin("1E+0")
        private Double initialValue;

        public Parameters()
        {}

        protected Parameters(
            DateTime refDate, EnumDeviceType deviceType, double value, double initialValue)
        {
            super(refDate, deviceType);
            this.value = value;
            this.initialValue = initialValue;
        }

        @JsonProperty("value")
        public Double getValue()
        {
            return value;
        }

        @JsonProperty("value")
        public void setValue(Double value)
        {
            this.value = value;
        }

        @JsonProperty("initialValue")
        public Double getInitialValue()
        {
            return initialValue;
        }

        @JsonProperty("initialValue")
        public void setInitialValue(Double initialValue)
        {
            this.initialValue = initialValue;
        }

        @DecimalMin("10.0")
        @JsonIgnore
        public Double getPercentChange()
        {
            return (value == null || initialValue == null)?
                null : (100.0 * ((initialValue - value) / initialValue));
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

            parameters.put("initial_value", initialValue);
            parameters.put("initial_consumption", initialValue);

            parameters.put("percent_change", Integer.valueOf(getPercentChange().intValue()));

            return parameters;
        }

        @Override
        @JsonIgnore
        public EnumAlertTemplate getTemplate()
        {
            return (deviceType == EnumDeviceType.AMPHIRO)?
                EnumAlertTemplate.REDUCED_WATER_USE_SHOWER:
                EnumAlertTemplate.REDUCED_WATER_USE_METER;
        }
    }

    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        final int N = 14; // number of days to examine

        DataQuery query = null;
        DataQueryResponse queryResponse = null;
        SeriesFacade series = null;
        Interval interval = null;

        AccountEntity account = userRepository.getAccountByKey(accountKey);
        Assert.state(account != null, "[Assertion failed] - Account not found");

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DateTime registerDate = account.getCreatedOn().withTimeAtStartOfDay();
        query = queryBuilder
            .sliding(registerDate, +N,  EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        interval = query.getTime().asInterval();
        Double c0 = (series != null)?
            series.aggregate(
                EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval), new Sum()):
            null;
        if (c0 == null)
            return Collections.emptyList();

        query = queryBuilder
            .sliding(refDate.withTimeAtStartOfDay(), -N,  EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        interval = query.getTime().asInterval();
        Double c1 = (series != null)?
            series.aggregate(
                EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval), new Sum()):
            null;
        if (c1 == null)
            return Collections.emptyList();

        Double percentChange = 100 * ((c0 - c1) / c0);
        if (percentChange > CHANGE_PERCENTAGE_THRESHOLD && percentChange < CHANGE_PERCENTAGE_CEIL) {
            ParameterizedTemplate parameterizedTemplate = new Parameters(refDate, deviceType, c1, c0);
            MessageResolutionStatus<ParameterizedTemplate> result =
                new SimpleMessageResolutionStatus<>(parameterizedTemplate);
            return Collections.singletonList(result);
        }
        return Collections.emptyList();
    }

}
