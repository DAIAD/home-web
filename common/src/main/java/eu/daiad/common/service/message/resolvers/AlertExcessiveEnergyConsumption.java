package eu.daiad.common.service.message.resolvers;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.daiad.common.annotate.message.MessageGenerator;
import eu.daiad.common.model.EnumDayOfWeek;
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
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.Point;
import eu.daiad.common.model.query.SeriesFacade;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.service.IDataService;
import eu.daiad.common.service.IEnergyCalculator;
import eu.daiad.common.service.IPriceDataService;
import eu.daiad.common.service.message.AbstractAlertResolver;

@MessageGenerator(period = "P1W", dayOfWeek = EnumDayOfWeek.MONDAY, maxPerWeek = 1)
@Component
@Scope("prototype")
public class AlertExcessiveEnergyConsumption extends AbstractAlertResolver
{
    public static final double HIGH_TEMPERATURE_THRESHOLD = 40.0;

    public static final double HIGH_TEMPERATURE_RATIO_OF_POINTS = 0.75;

    private static final Set<EnumDeviceType> supportedDevices = EnumSet.of(EnumDeviceType.AMPHIRO);

    @Autowired
    IDataService dataService;

    @Autowired
    IPriceDataService priceData;

    @Autowired
    IEnergyCalculator energyCalculator;

    @Autowired
    IUserRepository userRepository;

    @Override
    public List<MessageResolutionStatus<ParameterizedTemplate>> resolve(
        UUID accountKey, EnumDeviceType deviceType)
    {
        DateTime start = refDate.withTimeAtStartOfDay();

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(start, -30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
            .user("user", accountKey)
            .amphiro()
            .sum()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return Collections.emptyList();

        int numPoints = series.size();
        int numPointsHigh = series.count(
            EnumDataField.TEMPERATURE, EnumMetric.AVERAGE, Point.aboveValue(HIGH_TEMPERATURE_THRESHOLD));
        double ratioHigh = ((double) numPointsHigh) / numPoints;
        double monthlyConsumption = series.aggregate(EnumDataField.VOLUME, EnumMetric.SUM, new Sum());

        if (ratioHigh < HIGH_TEMPERATURE_RATIO_OF_POINTS)
            return Collections.emptyList();

        // Consumes excessive energy, estimate annual savings if changes behavior

        final int numMonthsPerYear = 12;
        final double pricePerKwh = priceData.getPricePerKwh(utility.getCountry());
        double annualSavings = numMonthsPerYear * pricePerKwh *
            energyCalculator.computeEnergyToRiseTemperature(2.0, monthlyConsumption);

        ParameterizedTemplate parameterizedTemplate =
            new SimpleParameterizedTemplate(
                refDate, EnumDeviceType.AMPHIRO, EnumAlertTemplate.TOO_MUCH_ENERGY)
            .withMoney1(annualSavings);

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
