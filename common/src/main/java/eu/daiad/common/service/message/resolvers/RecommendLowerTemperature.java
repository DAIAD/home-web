package eu.daiad.common.service.message.resolvers;

import java.math.BigDecimal;
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
import eu.daiad.common.model.query.EnumMeasurementField;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.Point;
import eu.daiad.common.model.query.SeriesFacade;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.service.ICurrencyRateService;
import eu.daiad.common.service.IDataService;
import eu.daiad.common.service.IEnergyCalculator;
import eu.daiad.common.service.IPriceDataService;
import eu.daiad.common.service.message.AbstractRecommendationResolver;

@MessageGenerator(period = "P1M", dayOfMonth = 2, maxPerMonth = 1)
@Component
@Scope("prototype")
public class RecommendLowerTemperature extends AbstractRecommendationResolver
{
    public static final double TEMPERATURE_HIGH_RATIO = 1.12;

    public static final double VOLUME_THRESHOLD_RATIO = 2.50;

    private static final Set<EnumDeviceType> supportedDevices = EnumSet.of(EnumDeviceType.AMPHIRO);

    public static class Parameters extends Message.AbstractParameters
    implements ParameterizedTemplate
    {
        @NotNull
        @DecimalMin("1E+1")
        private Double userAverageTemperature;

        @NotNull
        @DecimalMin("1E+1")
        private Double averageTemperature;

        /**
         * An estimate for annual money savings if temperature is 1 degree lower
         */
        @NotNull
        @DecimalMin("1E+1")
        private BigDecimal annualSavings;

        public Parameters()
        {}

        public Parameters(DateTime refDate, EnumDeviceType deviceType)
        {
            super(refDate, deviceType);
        }

        @JsonProperty("userAverageTemperature")
        public Double getUserAverageTemperature()
        {
            return userAverageTemperature;
        }

        @JsonProperty("userAverageTemperature")
        public void setUserAverageTemperature(double userAverageTemperature)
        {
            this.userAverageTemperature = userAverageTemperature;
        }

        public Parameters withUserAverageTemperature(double userAverageTemperature)
        {
            this.userAverageTemperature = userAverageTemperature;
            return this;
        }

        @JsonProperty("averageTemperature")
        public Double getAverageTemperature()
        {
            return averageTemperature;
        }

        @JsonProperty("averageTemperature")
        public void setAverageTemperature(double averageTemperature)
        {
            this.averageTemperature = averageTemperature;
        }

        public Parameters withAverageTemperature(double averageTemperature)
        {
            this.averageTemperature = averageTemperature;
            return this;
        }

        @JsonProperty("annualSavings")
        public BigDecimal getAnnualSavings()
        {
            return annualSavings;
        }

        @JsonProperty("annualSavings")
        public void setAnnualSavings(BigDecimal annualSavings)
        {
            this.annualSavings = annualSavings;
        }

        public Parameters withAnnualSavings(BigDecimal annualSavings)
        {
            this.annualSavings = annualSavings;
            return this;
        }

        public Parameters withAnnualSavings(double annualSavings)
        {
            return withAnnualSavings(
                BigDecimal.valueOf(Math.round(annualSavings * 1E+2), +2));
        }

        @JsonIgnore
        @DecimalMin("5.0")
        public Double getPercentAboveTemperature()
        {
            return 100.0 * (userAverageTemperature - averageTemperature) / averageTemperature;
        }

        @Override
        public Parameters withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            BigDecimal rate = currencyRate.getRate(Locale.getDefault(), target);
            annualSavings = annualSavings.multiply(rate);
            return this;
        }

        @Override
        @JsonIgnore
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();

            parameters.put("user_average_temperature", userAverageTemperature);

            parameters.put("average_temperature", averageTemperature);

            parameters.put("annual_savings_1", annualSavings);
            parameters.put("annual_savings_2", annualSavings.multiply(BigDecimal.valueOf(2L)));

            parameters.put("percent_above_temperature", getPercentAboveTemperature());

            return parameters;
        }

        @Override
        @JsonIgnore
        public EnumRecommendationTemplate getTemplate()
        {
            return EnumRecommendationTemplate.LOWER_TEMPERATURE;
        }
    }

    @Autowired
    IDataService dataService;

    @Autowired
    IUserRepository userRepository;

    @Autowired
    IPriceDataService priceData;

    @Autowired
    IEnergyCalculator energyCalculator;

    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        Assert.state(deviceType == EnumDeviceType.AMPHIRO, "[Assertion failed] - Device type must be AMPHIRO");

        final int N = 3; // number of months to examine
        final Period period = Period.months(N);
        final EnumTimeAggregation granularity = EnumTimeAggregation.MONTH;
        final DateTime end = refDate.withDayOfMonth(1).withTimeAtStartOfDay();

        Double averageTemperature = statisticsService.getNumber(
                end, period, EnumMeasurementField.AMPHIRO_TEMPERATURE, EnumStatistic.AVERAGE_PER_SESSION)
            .getValue();

        if (averageTemperature == null)
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

        double thresholdConsumption = period.getMonths() *
            config.getVolumeThreshold(EnumDeviceType.AMPHIRO, EnumTimeUnit.MONTH);

        Interval interval = query.getTime().asInterval();
        Double userConsumption = series.aggregate(
            EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval), new Sum());
        Double userAverageTemperature = series.aggregate(
            EnumDataField.TEMPERATURE, EnumMetric.AVERAGE, Point.betweenTime(interval), new Mean());

        boolean fire =
            userConsumption != null &&
            userAverageTemperature != null &&
            userAverageTemperature > TEMPERATURE_HIGH_RATIO * averageTemperature &&
            userConsumption > VOLUME_THRESHOLD_RATIO * thresholdConsumption;
        if (fire) {
            // Get a rough estimate for annual money savings if temperature is 1 degree lower
            final int numMonthsPerYear = 12;
            final double numPeriodsPerYear = Double.valueOf(numMonthsPerYear) / period.getMonths();
            final double pricePerKwh = priceData.getPricePerKwh(utility.getCountry());
            double annualSavings = numPeriodsPerYear * pricePerKwh *
                energyCalculator.computeEnergyToRiseTemperature(1.0, userConsumption);

            ParameterizedTemplate parameterizedTemplate = new Parameters(refDate, deviceType)
                .withUserAverageTemperature(userAverageTemperature)
                .withAverageTemperature(averageTemperature)
                .withAnnualSavings(annualSavings);

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
