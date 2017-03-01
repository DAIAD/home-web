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
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractAlertResolver;

@MessageGenerator(period = "P1W", dayOfWeek = EnumDayOfWeek.MONDAY, maxPerWeek = 1)
@Component
@Scope("prototype")
public class AlertWeeklyWaterSavings extends AbstractAlertResolver
{
    public static final double CHANGE_THRESHOLD = 100; // lt
    
    private static final Set<EnumDeviceType> supportedDevices = EnumSet.of(EnumDeviceType.METER);

    public static class Parameters extends Message.AbstractParameters
        implements ParameterizedTemplate
    {
        @NotNull
        @DecimalMin("1E+0")
        private Double value;
        
        @NotNull
        @DecimalMin("1E+0")
        private Double previousValue;
        
        public Parameters()
        {}
        
        protected Parameters(
            DateTime refDate, EnumDeviceType deviceType, double value, double previousValue)
        {
            super(refDate, deviceType);
            this.value = value;
            this.previousValue = previousValue;
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
        
        @JsonProperty("previousValue")
        public Double getPreviousValue()
        {
            return previousValue;
        }

        @JsonProperty("previousValue")
        public void setPreviousValue(double previousValue)
        {
            this.previousValue = previousValue;
        }
        
        @Override
        public ParameterizedTemplate withLocale(Locale target, ICurrencyRateService currencyRate)
        {
            return this;
        }
        
        @NotNull
        @DecimalMin("5E+0")
        @JsonIgnore
        public Double getWeeklySavings()
        {
            return (value != null && previousValue != null)? (previousValue - value) : null;
        }
        
        @Override
        @JsonIgnore
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = super.getParameters();
            
            parameters.put("value", value);
            parameters.put("consumption", value);
            
            parameters.put("previous_value", previousValue);
            parameters.put("previous_consumption", previousValue);
            
            parameters.put("weekly_savings", Integer.valueOf(getWeeklySavings().intValue()));
            
            return parameters;
        }
        
        @Override
        @JsonIgnore
        public EnumAlertTemplate getTemplate()
        {
            return EnumAlertTemplate.LITERS_ALREADY_SAVED;
        }
    }
    
    @Autowired
    IDataService dataService;
    
    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        DataQuery query = null;
        DataQueryResponse queryResponse = null;
        SeriesFacade series = null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .meter()
            .sum();

        double weeklyThreshold = config.getVolumeThreshold(EnumDeviceType.METER, EnumTimeUnit.WEEK);
        
        DateTime start = refDate.minusWeeks(1)
            .withDayOfWeek(DateTimeConstants.MONDAY)
            .withTimeAtStartOfDay();
        
        query = queryBuilder
            .sliding(start, +1,  EnumTimeUnit.WEEK, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(EnumDeviceType.METER);
        Double c0 = (series != null)? 
            series.get(EnumDataField.VOLUME, EnumMetric.SUM) : null;
        if (c0 == null || c0 < weeklyThreshold)
            return Collections.emptyList();

        query = queryBuilder
            .sliding(start.minusWeeks(1), +1,  EnumTimeUnit.WEEK, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(EnumDeviceType.METER);
        Double c1 = (series != null)? 
            series.get(EnumDataField.VOLUME, EnumMetric.SUM) : null;
        if (c1 == null || c1 < weeklyThreshold)
            return Collections.emptyList();

        Double change = c1 - c0;
        if (change > CHANGE_THRESHOLD) {
            ParameterizedTemplate parameterizedTemplate = new Parameters(refDate, deviceType, c0, c1);
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
