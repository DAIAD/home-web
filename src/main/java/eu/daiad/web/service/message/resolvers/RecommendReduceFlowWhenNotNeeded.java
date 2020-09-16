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

@MessageGenerator(period = "P1M", dayOfMonth = 10, maxPerMonth = 1)
@Component
@Scope("prototype")
public class RecommendReduceFlowWhenNotNeeded extends AbstractRecommendationResolver
{
    private static final Set<EnumDeviceType> supportedDevices = EnumSet.of(EnumDeviceType.AMPHIRO);

    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        @NotNull
        @DecimalMin("1E+0")
        private Double userAverageConsumptionPerSession;

        @NotNull
        @DecimalMin("1E+0")
        private Double averageConsumptionPerSession;

        @NotNull
        @DecimalMin("1E+2")
        private Integer annualSavings;

        public Parameters()
        {}

        public Parameters(DateTime refDate, EnumDeviceType deviceType)
        {
            super(refDate, deviceType);
        }

        @Override
        public Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }

        @JsonProperty("userAverageConsumptionPerSession")
        public Double getUserAverageConsumptionPerSession()
        {
            return userAverageConsumptionPerSession;
        }


        @JsonProperty("userAverageConsumptionPerSession")
        public void setUserAverageConsumptionPerSession(double userAverageConsumptionPerSession)
        {
            this.userAverageConsumptionPerSession = userAverageConsumptionPerSession;
        }

        public Parameters withUserAverageConsumptionPerSession(double userAverageConsumptionPerSession)
        {
            this.userAverageConsumptionPerSession = userAverageConsumptionPerSession;
            return this;
        }

        @JsonProperty("averageConsumptionPerSession")
        public Double getAverageConsumptionPerSession()
        {
            return averageConsumptionPerSession;
        }

        @JsonProperty("averageConsumptionPerSession")
        public void setAverageConsumptionPerSession(double averageConsumptionPerSession)
        {
            this.averageConsumptionPerSession = averageConsumptionPerSession;
        }

        public Parameters withAverageConsumptionPerSession(double averageConsumptionPerSession)
        {
            this.averageConsumptionPerSession = averageConsumptionPerSession;
            return this;
        }

        @JsonProperty("annualSavings")
        public Integer getAnnualSavings()
        {
            return annualSavings;
        }

        @JsonProperty("annualSavings")
        public void setAnnualSavings(int annualSavings)
        {
            this.annualSavings = annualSavings;
        }

        public Parameters withAnnualSavings(int annualSavings)
        {
            this.annualSavings = annualSavings;
            return this;
        }

        @Override
        @JsonIgnore
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();

            parameters.put("user_average_consumption_per_session", userAverageConsumptionPerSession);

            parameters.put("average_consumption_per_pession", averageConsumptionPerSession);

            parameters.put("annual_savings", annualSavings);

            return parameters;
        }

        @Override
        @JsonIgnore
        public EnumRecommendationTemplate getTemplate()
        {
            return EnumRecommendationTemplate.REDUCE_FLOW_WHEN_NOT_NEEDED;
        }
    }

    @Autowired
    IDataService dataService;

    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        Assert.state(deviceType == EnumDeviceType.AMPHIRO, "[Assertion failed] - The device type must be AMPHIRO");

        final int N = 3; // number of months to examine
        final Period period = Period.months(N);
        final EnumTimeAggregation granularity = EnumTimeAggregation.MONTH;
        final DateTime end = refDate.withDayOfMonth(1).withTimeAtStartOfDay();

        Double averagePerSessionConsumption = statisticsService.getNumber(
                end, period, EnumMeasurementField.AMPHIRO_VOLUME, EnumStatistic.AVERAGE_PER_SESSION)
            .getValue();

        if (averagePerSessionConsumption == null)
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
        Double userNumberOfSessions = series.aggregate(
            EnumDataField.VOLUME, EnumMetric.COUNT, Point.betweenTime(interval), new Sum());
        if (userConsumption == null || userNumberOfSessions == null)
            return Collections.emptyList();

        double userAveragePerSessionConsumption = userConsumption / userNumberOfSessions;
        if (userAveragePerSessionConsumption > averagePerSessionConsumption) {
            // Todo - Calculate the number of sessions per year when available
            int numberOfSessionsPerYear = 100;
            // Estimate annual savings
            Double annualSavings = numberOfSessionsPerYear *
                (userAveragePerSessionConsumption - averagePerSessionConsumption);
            ParameterizedTemplate parameterizedTemplate = new Parameters(refDate, deviceType)
                .withAverageConsumptionPerSession(averagePerSessionConsumption)
                .withUserAverageConsumptionPerSession(userAveragePerSessionConsumption)
                .withAnnualSavings(annualSavings.intValue());

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
