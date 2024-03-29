package eu.daiad.common.service.message.resolvers;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
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

import eu.daiad.common.annotate.message.MessageGenerator;
import eu.daiad.common.model.EnumStatistic;
import eu.daiad.common.model.EnumTimeAggregation;
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
import eu.daiad.common.model.query.EnumMeasurementField;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.Point;
import eu.daiad.common.model.query.SeriesFacade;
import eu.daiad.common.service.ICurrencyRateService;
import eu.daiad.common.service.IDataService;
import eu.daiad.common.service.message.AbstractRecommendationResolver;

@MessageGenerator(period = "P1M", dayOfMonth = 2, maxPerMonth = 1)
@Component
@Scope("prototype")
public class RecommendLowerFlow extends AbstractRecommendationResolver
{
    public static final double FLOW_HIGH_RATIO = 1.10;

    public static final double VOLUME_HIGH_RATIO = 1.15;

    private static final Set<EnumDeviceType> supportedDevices = EnumSet.of(EnumDeviceType.AMPHIRO);

    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        @NotNull
        @DecimalMin("1E-2")
        private Double userAverageFlow;

        @NotNull
        @DecimalMin("1E+1")
        private Double userAverageConsumption;

        @NotNull
        @DecimalMin("1E-2")
        private Double averageFlow;

        @NotNull
        @DecimalMin("1E+1")
        private Double averageConsumption;

        /**
         * An estimate for annual water savings if average behavior adopted
         */
        @NotNull
        @DecimalMin("1E+2")
        private Integer annualSavings;

        public Parameters()
        {}

        public Parameters(DateTime refDate, EnumDeviceType deviceType)
        {
            super(refDate, deviceType);
        }

        @JsonProperty("userAverageFlow")
        public Double getUserAverageFlow()
        {
            return userAverageFlow;
        }

        @JsonProperty("userAverageFlow")
        public void setUserAverageFlow(double userAverageFlow)
        {
            this.userAverageFlow = userAverageFlow;
        }

        public Parameters withUserAverageFlow(double userAverageFlow)
        {
            this.userAverageFlow = userAverageFlow;
            return this;
        }

        @JsonProperty("averageFlow")
        public Double getAverageFlow()
        {
            return averageFlow;
        }

        @JsonProperty("averageFlow")
        public void setAverageFlow(double averageFlow)
        {
            this.averageFlow = averageFlow;
        }

        public Parameters withAverageFlow(double averageFlow)
        {
            this.averageFlow = averageFlow;
            return this;
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
            parameters.put("user_average_flow", userAverageFlow);

            parameters.put("average_consumption", averageConsumption);
            parameters.put("average_flow", averageFlow);

            parameters.put("annual_savings", annualSavings);

            parameters.put("percent_above_flow", Double.valueOf(
                100.0 * (userAverageFlow - averageFlow) / averageFlow));

            parameters.put("percent_above_consumption", Double.valueOf(
                100.0 * (userAverageConsumption - averageConsumption) / averageConsumption));

            return parameters;
        }

        @Override
        @JsonIgnore
        public EnumRecommendationTemplate getTemplate()
        {
            return EnumRecommendationTemplate.LOWER_FLOW;
        }
    }

    @Autowired
    IDataService dataService;

    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        Assert.state(deviceType == EnumDeviceType.AMPHIRO, "[Assertion failed] - Device type must be AMPHIRO");

        final int N = 3; // number of months to examine
        final Period period = Period.months(N);
        final EnumTimeAggregation granularity = EnumTimeAggregation.MONTH;

        DateTime end = refDate.withDayOfMonth(1)
            .withTimeAtStartOfDay();

        Double averageFlow = statisticsService.getNumber(
                end, period, EnumMeasurementField.AMPHIRO_FLOW, EnumStatistic.AVERAGE_PER_SESSION)
            .getValue();

        Double averageConsumption = statisticsService.getNumber(
                end, period, EnumMeasurementField.AMPHIRO_VOLUME, EnumStatistic.AVERAGE_PER_USER)
            .getValue();

        if (averageFlow == null || averageConsumption == null)
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
            return null;

        Interval interval = query.getTime().asInterval();
        Double userConsumption = series.aggregate(
            EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval), new Sum());
        Double userAverageFlow = series.aggregate(
            EnumDataField.FLOW, EnumMetric.AVERAGE, Point.betweenTime(interval), new Mean());
        boolean fire =
            userConsumption != null &&
            userAverageFlow != null &&
            userAverageFlow > averageFlow * FLOW_HIGH_RATIO &&
            userConsumption > averageConsumption * VOLUME_HIGH_RATIO;
        if (fire) {
            // Get a rough estimate for annual water savings if adopts average behavior
            final int numMonthsPerYear = 12;
            final double numPeriodsPerYear = Double.valueOf(numMonthsPerYear) / period.getMonths();
            Double annualSavings = numPeriodsPerYear * (userConsumption - averageConsumption);

            ParameterizedTemplate parameterizedTemplate = new Parameters(refDate, deviceType)
                .withAverageConsumption(averageConsumption)
                .withAverageFlow(averageFlow)
                .withUserAverageFlow(userAverageFlow)
                .withUserAverageConsumption(userConsumption)
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
