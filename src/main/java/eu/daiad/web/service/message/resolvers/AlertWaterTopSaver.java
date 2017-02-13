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
import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.ConsumptionStats.EnumStatistic;
import eu.daiad.web.model.EnumDayOfWeek;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.EnumTimeUnit;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.Alert.ParameterizedTemplate;
import eu.daiad.web.model.message.Alert.SimpleParameterizedTemplate;
import eu.daiad.web.model.message.EnumAlertTemplate;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.message.SimpleMessageResolutionStatus;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumDataField;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.Point;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractAlertResolver;

@MessageGenerator(period = "P1W", dayOfWeek = EnumDayOfWeek.MONDAY, maxPerWeek = 1)
@Component
@Scope("prototype")
public class AlertWaterTopSaver extends AbstractAlertResolver
{
    private static final Set<EnumDeviceType> supportedDevices = EnumSet.of(EnumDeviceType.METER);
    
    @Autowired
    IDataService dataService;
    
    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        ComputedNumber weekly25pThreshold = stats.get(
            EnumStatistic.THRESHOLD_BOTTOM_25P_WEEKLY, EnumDeviceType.METER, EnumDataField.VOLUME);
        if (weekly25pThreshold == null || weekly25pThreshold.getValue() == null)
            return Collections.emptyList();
        
        ComputedNumber weekly10pThreshold = stats.get(
            EnumStatistic.THRESHOLD_BOTTOM_10P_WEEKLY, EnumDeviceType.METER, EnumDataField.VOLUME);
        if (weekly10pThreshold == null || weekly10pThreshold.getValue() == null)
            return Collections.emptyList();
        
        double weeklyThreshold = config.getVolumeThreshold(EnumDeviceType.METER, EnumTimeUnit.WEEK);
        
        DateTime start = refDate.minusWeeks(1)
            .withDayOfWeek(DateTimeConstants.MONDAY)
            .withTimeAtStartOfDay();
        
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .sliding(start, +1, EnumTimeUnit.WEEK, EnumTimeAggregation.ALL)
            .meter()
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.METER);
        Double consumption = (series != null)? 
            series.get(EnumDataField.VOLUME, EnumMetric.SUM): null;
        if (consumption == null || consumption < weeklyThreshold)
            return Collections.emptyList();
        
        ParameterizedTemplate parameterizedTemplate = null;
        if (consumption < weekly10pThreshold.getValue()) {
            parameterizedTemplate = new SimpleParameterizedTemplate(
                refDate, EnumDeviceType.METER, EnumAlertTemplate.TOP_10_PERCENT_OF_SAVERS);
        } else if (consumption < weekly25pThreshold.getValue()) {
            parameterizedTemplate = new SimpleParameterizedTemplate(
                refDate, EnumDeviceType.METER, EnumAlertTemplate.TOP_25_PERCENT_OF_SAVERS);
        }
        
        if (parameterizedTemplate == null)
            return Collections.emptyList();
        
        MessageResolutionStatus<ParameterizedTemplate> result = 
            new SimpleMessageResolutionStatus<>(true, parameterizedTemplate); 
        return Collections.singletonList(result);
    }
    
    @Override
    public Set<EnumDeviceType> getSupportedDevices()
    {
        return Collections.unmodifiableSet(supportedDevices);
    }
}
