package eu.daiad.web.service.message.resolvers;

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

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.annotate.message.MessageGenerator;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.model.EnumStatistic;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.EnumTimeUnit;
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
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.IEnergyCalculator;
import eu.daiad.web.service.IPriceDataService;
import eu.daiad.web.service.message.AbstractRecommendationResolver;

@MessageGenerator(period = "P1M", dayOfMonth = 2, maxPerMonth = 1)
@Component
@Scope("prototype")
public class RecommendLowerTemperature extends AbstractRecommendationResolver
{
    public static final double TEMPERATURE_HIGH_RATIO = 1.1; 
        
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
            
            parameters.put("percent_above_temperature", Double.valueOf(
                100.0 * (userAverageTemperature - averageTemperature) / averageTemperature));
            
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
        final int N = 3; // number of months to examine
        
        Double averageTemperature = stats.getValue(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, EnumDataField.TEMPERATURE);
        if (averageTemperature == null)
            return Collections.emptyList();
        
        DateTime start = refDate.minusMonths(N)
            .withDayOfMonth(1)
            .withTimeAtStartOfDay();
        
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .sliding(start, N, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .amphiro()
            .sum()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return Collections.emptyList();
        
        double userAverageConsumption = series.get(EnumDataField.VOLUME, EnumMetric.SUM) / N;
        double userAverageTemperature = series.get(EnumDataField.TEMPERATURE, EnumMetric.AVERAGE);
        if (userAverageTemperature > averageTemperature * TEMPERATURE_HIGH_RATIO) {
            // Get a rough estimate for annual money savings if temperature is 1 degree lower
            final int numMonthsPerYear = 12;
            final double pricePerKwh = priceData.getPricePerKwh(utility.getCountry());
            double annualSavings = numMonthsPerYear * pricePerKwh *
                energyCalculator.computeEnergyToRiseTemperature(1.0, userAverageConsumption);
            
            ParameterizedTemplate parameterizedTemplate = new Parameters(refDate, deviceType)
                .withUserAverageTemperature(userAverageTemperature)
                .withAverageTemperature(averageTemperature)
                .withAnnualSavings(annualSavings);
            
            MessageResolutionStatus<ParameterizedTemplate> result = 
                new SimpleMessageResolutionStatus<>(true, parameterizedTemplate);
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
