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
import eu.daiad.web.model.query.EnumMeasurementField;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.Point;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractRecommendationResolver;

@MessageGenerator(period = "P1M", dayOfMonth = 5, maxPerMonth = 1)
@Component
@Scope("prototype")
public class RecommendShampooChange extends AbstractRecommendationResolver
{
    public static final double VOLUME_HIGH_RATIO = 1.10;

    private static final Set<EnumDeviceType> supportedDevices = EnumSet.of(EnumDeviceType.AMPHIRO);

    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        @NotNull
        @DecimalMin("1E+1")
        private Double userAverageConsumption;

        @NotNull
        @DecimalMin("1E+1")
        private Double averageConsumption;

        public Parameters()
        {}

        public Parameters(DateTime refDate, EnumDeviceType deviceType)
        {
            super(refDate, deviceType);
        }

        @JsonProperty("userAverageConsumption")
        public Double getUserAverageConsumption()
        {
            return userAverageConsumption;
        }

        @JsonProperty("userAverageConsumption")
        public void setUserAverageConsumption(double userAverageConsumption)
        {
            this.userAverageConsumption = userAverageConsumption;
        }

        public Parameters withUserAverageConsumption(double userAverageConsumption)
        {
            this.userAverageConsumption = userAverageConsumption;
            return this;
        }

        @JsonProperty("averageConsumption")
        public Double getAverageConsumption()
        {
            return averageConsumption;
        }

        @JsonProperty("averageConsumption")
        public void setAverageConsumption(double averageConsumption)
        {
            this.averageConsumption = averageConsumption;
        }

        public Parameters withAverageConsumption(double averageConsumption)
        {
            this.averageConsumption = averageConsumption;
            return this;
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

            parameters.put("user_average_consumption", userAverageConsumption);

            parameters.put("average_consumption", averageConsumption);

            parameters.put("percent_above_consumption", Double.valueOf(
                100.0 * (userAverageConsumption - averageConsumption) / averageConsumption));

            return parameters;
        }

        @Override
        @JsonIgnore
        public EnumRecommendationTemplate getTemplate()
        {
            return EnumRecommendationTemplate.CHANGE_SHAMPOO;
        }
    }

    @Autowired
    IDataService dataService;

    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        Assert.state(deviceType == EnumDeviceType.AMPHIRO);

        final int N = 3; // number of months to examine
        final Period period = Period.months(N);
        final EnumTimeAggregation granularity = EnumTimeAggregation.MONTH;
        final DateTime end = refDate.withDayOfMonth(1).withTimeAtStartOfDay();

        Double averageConsumption = statisticsService.getNumber(
                end, period, EnumMeasurementField.AMPHIRO_VOLUME, EnumStatistic.AVERAGE_PER_USER)
            .getValue();
        if (averageConsumption == null)
            return Collections.emptyList();

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .absolute(end.minus(period), end, granularity)
            .amphiro()
            .sum()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return Collections.emptyList();

        Interval interval = query.getTime().asInterval();
        Double userConsumption = series.aggregate(
            EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval), new Sum());
        if (userConsumption != null && userConsumption > averageConsumption * VOLUME_HIGH_RATIO) {
            ParameterizedTemplate parameterizedTemplate = new Parameters(refDate, deviceType)
                .withAverageConsumption(averageConsumption)
                .withUserAverageConsumption(userConsumption);
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
