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

import org.apache.commons.collections4.FluentIterable;
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
import eu.daiad.web.model.query.Point;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.service.ICurrencyRateService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.AbstractAlertResolver;

@MessageGenerator(period = "P1M", dayOfMonth = 1)
@Component
@Scope("prototype")
public class AlertWaterChampion extends AbstractAlertResolver
{
    @Autowired
    IDataService dataService;
    
    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {    
        final int MAX_NUM_CONSECUTIVE_ZEROS = 10; // to consider the user as absent
        
        final double dailyBudget = config.getBudget(deviceType, EnumTimeUnit.DAY);

        DateTime start = refDate.minusMonths(1)
            .withDayOfMonth(1)
            .withTimeAtStartOfDay();
        
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(start, +1, EnumTimeUnit.MONTH, EnumTimeAggregation.DAY)
            .user("user", accountKey)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(deviceType);
        if (series == null || series.isEmpty())
            return Collections.emptyList();

        FluentIterable<Point> points = FluentIterable
            .of(series.iterPoints(EnumDataField.VOLUME, EnumMetric.SUM));

        boolean fire = true;
        int consecutiveZeros = 0;
        for (Point p: points) {
            double dailyConsumption = p.getValue();
            if (dailyConsumption > 0) {
                consecutiveZeros = 0;
                if (dailyConsumption > dailyBudget) {
                    fire = false; // exceeded daily limit
                    break;
                }
            } else {
                consecutiveZeros++;
                if (consecutiveZeros > MAX_NUM_CONSECUTIVE_ZEROS) {
                    fire = false; // the user is probably absent
                    break;
                }
            }
        }
        if (!fire)
            return Collections.emptyList();

        ParameterizedTemplate parameterizedTemplate = new SimpleParameterizedTemplate(
            refDate, deviceType, (deviceType == EnumDeviceType.AMPHIRO)?
                EnumAlertTemplate.SHOWER_CHAMPION : EnumAlertTemplate.WATER_CHAMPION);        
        MessageResolutionStatus<ParameterizedTemplate> result = 
            new SimpleMessageResolutionStatus<>(parameterizedTemplate); 
        return Collections.singletonList(result);
    }

}
