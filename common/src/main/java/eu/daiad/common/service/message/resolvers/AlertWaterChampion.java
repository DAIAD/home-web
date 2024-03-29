package eu.daiad.common.service.message.resolvers;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.FluentIterable;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.daiad.common.annotate.message.MessageGenerator;
import eu.daiad.common.model.EnumTimeAggregation;
import eu.daiad.common.model.EnumTimeUnit;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.message.Alert.ParameterizedTemplate;
import eu.daiad.common.model.message.Alert.SimpleParameterizedTemplate;
import eu.daiad.common.model.message.EnumAlertTemplate;
import eu.daiad.common.model.message.MessageResolutionStatus;
import eu.daiad.common.model.message.SimpleMessageResolutionStatus;
import eu.daiad.common.model.query.DataQuery;
import eu.daiad.common.model.query.DataQueryBuilder;
import eu.daiad.common.model.query.DataQueryResponse;
import eu.daiad.common.model.query.EnumDataField;
import eu.daiad.common.model.query.EnumMeasurementDataSource;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.Point;
import eu.daiad.common.model.query.SeriesFacade;
import eu.daiad.common.service.IDataService;
import eu.daiad.common.service.message.AbstractAlertResolver;

@MessageGenerator(period = "P1M", dayOfMonth = 1, maxPerWeek = 1)
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
