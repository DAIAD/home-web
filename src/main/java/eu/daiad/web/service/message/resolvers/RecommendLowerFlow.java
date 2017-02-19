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
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.annotate.message.MessageGenerator;
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
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractRecommendationResolver;

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
        final int N = 3; // number of months to examine
        
        Double averageFlow = stats.getValue(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, EnumDataField.FLOW);
        Double averageConsumption = stats.getValue(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, EnumDataField.VOLUME);
        if (averageFlow == null || averageConsumption == null)
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
            return null;
        
        double userAverageFlow = series.get(EnumDataField.FLOW, EnumMetric.AVERAGE);
        double userAverageConsumption = series.get(EnumDataField.VOLUME, EnumMetric.SUM) / N;
        boolean fire = 
            userAverageFlow > averageFlow * FLOW_HIGH_RATIO &&
            userAverageConsumption > averageConsumption * VOLUME_HIGH_RATIO;
        if (fire) {
            // Get a rough estimate for annual water savings if adopts average behavior
            final int numMonthsPerYear = 12;
            Double annualSavings = numMonthsPerYear * (userAverageConsumption - averageConsumption);
            
            ParameterizedTemplate parameterizedTemplate = new Parameters(refDate, deviceType)
                .withAverageConsumption(averageConsumption)
                .withAverageFlow(averageFlow)
                .withUserAverageFlow(userAverageFlow)
                .withUserAverageConsumption(userAverageConsumption)
                .withAnnualSavings(annualSavings.intValue());
            
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
