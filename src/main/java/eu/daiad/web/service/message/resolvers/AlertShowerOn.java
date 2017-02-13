package eu.daiad.web.service.message.resolvers;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.FluentIterable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.daiad.web.annotate.message.MessageGenerator;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.EnumTimeUnit;
import eu.daiad.web.model.device.EnumDeviceType;
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
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractAlertResolver;

@MessageGenerator()
@Component
@Scope("prototype")
public class AlertShowerOn extends AbstractAlertResolver
{ 
    public static final int DURATION_THRESHOLD_IN_MINUTES = 30;
 
    private static final Set<EnumDeviceType> supportedDevices = EnumSet.of(EnumDeviceType.AMPHIRO);
    
    @Autowired
    IDataService dataService;
    
    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {   
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
            .user("user", accountKey)
            .amphiro()
            .max();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return Collections.emptyList();

        FluentIterable<Point> points = FluentIterable
            .of(series.iterPoints(EnumDataField.DURATION, EnumMetric.MAX));

        if (points.allMatch(Point.belowValue(DURATION_THRESHOLD_IN_MINUTES)))
            return Collections.emptyList();;

        ParameterizedTemplate parameterizedTemplate = new SimpleParameterizedTemplate(
            refDate, EnumDeviceType.AMPHIRO, EnumAlertTemplate.SHOWER_ON);
        
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
