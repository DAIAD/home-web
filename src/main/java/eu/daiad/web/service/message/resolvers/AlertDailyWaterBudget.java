package eu.daiad.web.service.message.resolvers;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.annotate.message.MessageGenerator;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.EnumTimeUnit;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.Alert.ParameterizedTemplate;
import eu.daiad.web.model.message.Alert.SimpleParameterizedTemplate;
import eu.daiad.web.model.message.EnumAlertTemplate;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.message.SimpleMessageResolutionStatus;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMeasurementDataSource;
import eu.daiad.web.model.query.EnumDataField;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractAlertResolver;

@MessageGenerator(period = "P1D")
@Component
@Scope("prototype")
public class AlertDailyWaterBudget extends AbstractAlertResolver
{
    public static final int BUDGET_NEAR_PERCENTAGE_THRESHOLD = 80; // must be <= 100
    
    public static final int BUDGET_OVER_PERCENTAGE_THRESHOLD = 110; // must be > 100
    
    @Autowired
    IDataService dataService;
        
    private static abstract class BasicParameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {        
        @NotNull
        @DecimalMin("1E-3")
        private Double value;
        
        @NotNull
        @DecimalMin("1E+0")
        private Double budget;
     
        @NotNull
        @DecimalMin("0")
        @DecimalMax("200")
        private Integer percentThreshold;
        
        public BasicParameters()
        {}
        
        protected BasicParameters(
            DateTime refDate, EnumDeviceType deviceType, double value, double budget, int percentThreshold)
        {
            super(refDate, deviceType);
            this.value = value;
            this.budget = budget;
            this.percentThreshold = percentThreshold;
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
        
        @JsonProperty("budget")
        public Double getBudget()
        {
            return budget;
        }

        @JsonProperty("budget")
        public void setBudget(double budget)
        {
            this.budget = budget;
        }

        @JsonProperty("percentThreshold")
        public int getPercentThreshold()
        {
            return percentThreshold;
        }
                
        @JsonProperty("percentThreshold")
        public void setPercentThreshold(int percentThreshold)
        {
            this.percentThreshold = percentThreshold;
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
            
            parameters.put("budget", budget);
            parameters.put("remaining", Double.valueOf((budget > value)? (budget - value) : .0));
            
            parameters.put("percent_threshold", percentThreshold);
            
            Double percentUsed = 100.0 * (value / budget);
            parameters.put("percent_consumed", Integer.valueOf(percentUsed.intValue()));
            
            return parameters;
        }
    }
    
    private static class NearBudgetParameters extends BasicParameters
    {  
        public NearBudgetParameters()
        {
            super();
        }

        public NearBudgetParameters(
            DateTime refDate, EnumDeviceType deviceType, double value, double budget, int percentThreshold)
        {
            super(refDate, deviceType, value, budget, percentThreshold);
        }

        @Override
        @JsonIgnore
        public EnumAlertTemplate getTemplate()
        {
            return (deviceType == EnumDeviceType.AMPHIRO)?
                EnumAlertTemplate.NEAR_DAILY_SHOWER_BUDGET: EnumAlertTemplate.NEAR_DAILY_WATER_BUDGET;
        }
    }
    
    private static class ExceededBudgetParameters extends BasicParameters
    {
        public ExceededBudgetParameters()
        {
            super();
        }

        public ExceededBudgetParameters(
            DateTime refDate, EnumDeviceType deviceType, double value, double budget, int percentThreshold)
        {
            super(refDate, deviceType, value, budget, percentThreshold);
        }
        
        @Override
        @JsonIgnore
        public EnumAlertTemplate getTemplate()
        {
            return (deviceType == EnumDeviceType.AMPHIRO)?
                EnumAlertTemplate.REACHED_DAILY_SHOWER_BUDGET: EnumAlertTemplate.REACHED_DAILY_WATER_BUDGET;
        }
    }
    
    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {   
        DateTime start = refDate.minusDays(1).withTimeAtStartOfDay();
        
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .sliding(start, +1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(deviceType);

        Double consumption  = (series != null)?
            series.get(EnumDataField.VOLUME, EnumMetric.SUM) : null;
        if (consumption == null)
            return Collections.emptyList();

        int budget = config.getBudget(deviceType, EnumTimeUnit.DAY);
        double percentConsumed = 100.0 * (consumption / budget);
        
        ParameterizedTemplate parameterizedTemplate;
        if (percentConsumed < BUDGET_NEAR_PERCENTAGE_THRESHOLD) {
            parameterizedTemplate = null;
        } else if (percentConsumed < BUDGET_OVER_PERCENTAGE_THRESHOLD) {
            parameterizedTemplate = new NearBudgetParameters(
                refDate, deviceType, consumption, budget,  BUDGET_NEAR_PERCENTAGE_THRESHOLD);
        } else {
            parameterizedTemplate = new ExceededBudgetParameters(
                refDate, deviceType, consumption, budget,  BUDGET_OVER_PERCENTAGE_THRESHOLD);
        }
        if (parameterizedTemplate == null)
            return Collections.emptyList();
        
        MessageResolutionStatus<ParameterizedTemplate> result = 
            new SimpleMessageResolutionStatus<>(true, parameterizedTemplate); 
        return Collections.singletonList(result);
    }

}
