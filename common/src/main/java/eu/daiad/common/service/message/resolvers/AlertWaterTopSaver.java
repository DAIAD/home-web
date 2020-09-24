package eu.daiad.common.service.message.resolvers;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.daiad.common.annotate.message.MessageGenerator;
import eu.daiad.common.model.EnumDayOfWeek;
import eu.daiad.common.model.EnumStatistic;
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
import eu.daiad.common.model.query.EnumMeasurementField;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.Point;
import eu.daiad.common.model.query.SeriesFacade;
import eu.daiad.common.service.IDataService;
import eu.daiad.common.service.message.AbstractAlertResolver;

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
        Assert.state(deviceType == EnumDeviceType.METER, "[Assertion failed] - Device type must be METER");

        final Period period = Period.weeks(1);
        final EnumTimeAggregation granularity = EnumTimeAggregation.WEEK;
        final EnumMeasurementField measurementField = EnumMeasurementField.METER_VOLUME;

        DateTime end = refDate.withDayOfWeek(DateTimeConstants.MONDAY)
            .withTimeAtStartOfDay();

        Double weekly25pThreshold = statisticsService.getNumber(
                end, period, measurementField, EnumStatistic.PERCENTILE_25P_OF_USERS)
            .getValue();

        Double weekly10pThreshold = statisticsService.getNumber(
                end, period, measurementField, EnumStatistic.PERCENTILE_10P_OF_USERS)
            .getValue();

        if (weekly25pThreshold == null || weekly10pThreshold == null)
            return Collections.emptyList();

        double weeklyThreshold = config.getVolumeThreshold(EnumDeviceType.METER, EnumTimeUnit.WEEK);

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", accountKey)
            .absolute(end.minus(period), end, granularity)
            .meter()
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.METER);
        Interval interval = query.getTime().asInterval();
        Double consumption = (series != null)?
            series.get(EnumDataField.VOLUME, EnumMetric.SUM, Point.betweenTime(interval)):
            null;
        if (consumption == null || consumption < weeklyThreshold)
            return Collections.emptyList();

        ParameterizedTemplate parameterizedTemplate = null;
        if (consumption < weekly10pThreshold) {
            parameterizedTemplate = new SimpleParameterizedTemplate(
                refDate, EnumDeviceType.METER, EnumAlertTemplate.TOP_10_PERCENT_OF_SAVERS);
        } else if (consumption < weekly25pThreshold) {
            parameterizedTemplate = new SimpleParameterizedTemplate(
                refDate, EnumDeviceType.METER, EnumAlertTemplate.TOP_25_PERCENT_OF_SAVERS);
        }

        if (parameterizedTemplate == null)
            return Collections.emptyList();

        MessageResolutionStatus<ParameterizedTemplate> result =
            new SimpleMessageResolutionStatus<>(parameterizedTemplate);
        return Collections.singletonList(result);
    }

    @Override
    public Set<EnumDeviceType> getSupportedDevices()
    {
        return Collections.unmodifiableSet(supportedDevices);
    }
}
