package eu.daiad.web.service.message;

import static eu.daiad.web.model.device.EnumDeviceType.METER;
import static eu.daiad.web.model.query.EnumDataField.DURATION;
import static eu.daiad.web.model.query.EnumDataField.FLOW;
import static eu.daiad.web.model.query.EnumDataField.TEMPERATURE;
import static eu.daiad.web.model.query.EnumDataField.VOLUME;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.FluentIterable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountTipEntity;
import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.ConsumptionStats.EnumStatistic;
import eu.daiad.web.model.EnumDayOfWeek;
import eu.daiad.web.model.EnumPartOfDay;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.EnumTimeUnit;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.EnumAlertTemplate;
import eu.daiad.web.model.message.EnumRecommendationTemplate;
import eu.daiad.web.model.message.IMessageResolutionStatus;
import eu.daiad.web.model.message.Insight;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.message.Recommendation;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumDataField;
import eu.daiad.web.model.query.EnumMeasurementDataSource;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.Point;
import eu.daiad.web.model.query.SeriesFacade;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IAccountTipRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.IEnergyCalculator;
import eu.daiad.web.service.IPriceDataService;

@Service()
public class DefaultMessageResolverService implements IMessageResolverService
{
    private static final Log logger = LogFactory.getLog(DefaultMessageResolverService.class);

    @Autowired
    IDataService dataService;

    @Autowired
    IUserRepository userRepository;

    @Autowired
    IAccountTipRepository accountTipRepository;

    @Autowired
    IDeviceRepository deviceRepository;

    @Autowired
    IPriceDataService priceData;

    @Autowired
    IEnergyCalculator energyCalculator;

    public static final double VOLUME_DAILY_THRESHOLD = 7.0;

    public static final double VOLUME_WEEKLY_THRESHOLD =
        DateTimeConstants.DAYS_PER_WEEK * VOLUME_DAILY_THRESHOLD;

    public static final double VOLUME_MONTHLY_THRESHOLD = // approx 30.5 days/month
        30.5 * VOLUME_DAILY_THRESHOLD;

    public static Map<EnumTimeUnit, Double> volumeThreshold = new EnumMap<>(EnumTimeUnit.class);
    static {
        volumeThreshold.put(EnumTimeUnit.DAY, VOLUME_DAILY_THRESHOLD);
        volumeThreshold.put(EnumTimeUnit.WEEK, VOLUME_WEEKLY_THRESHOLD);
        volumeThreshold.put(EnumTimeUnit.MONTH, VOLUME_MONTHLY_THRESHOLD);
    }

    @Override
    public MessageResolutionPerAccountStatus resolve(
        IMessageGeneratorService.Configuration config,
        UtilityInfo utility, ConsumptionStats stats, UUID accountKey)
    {
        AccountEntity account = userRepository.getAccountByKey(accountKey);

        DateTimeZone tz = DateTimeZone.forID(utility.getTimezone());
        DateTime refDate = config.getRefDate().toDateTime(tz);

        EnumSet<EnumDeviceType> deviceTypes =
            EnumSet.of(EnumDeviceType.AMPHIRO, EnumDeviceType.METER);

        MessageResolutionPerAccountStatus status = new MessageResolutionPerAccountStatus(accountKey);

        status.setMeterInstalled(
            isMeterInstalledForUser(account));

        status.setAmphiroInstalled(
            isAmphiroInstalledForUser(account));

        status.setAlertWaterLeakSWM(
            alertWaterLeakSWM(account, refDate, stats));

        status.setAlertWaterQualitySWM(
            alertWaterQualitySWM(account, refDate, stats));

        status.setAlertNearDailyBudgetSWM(
            alertNearDailyBudget(config, account, refDate, EnumDeviceType.METER));

        status.setAlertNearWeeklyBudgetSWM(
            alertNearWeeklyBudget(config, account, refDate, EnumDeviceType.METER));

        status.setAlertReachedDailyBudgetSWM(
            alertReachedDailyBudget(config, account, refDate, EnumDeviceType.METER));

        status.setAlertWaterChampionSWM(
            alertWaterChampion(config, account, refDate, EnumDeviceType.METER));

        status.setAlertTooMuchWaterConsumptionSWM(
            alertTooMuchWaterConsumption(config, stats, account, refDate, EnumDeviceType.METER));

        status.setAlertReducedWaterUseSWM(
            alertReducedWaterUse(config, account, refDate, EnumDeviceType.METER));

        status.setAlertWaterEfficiencyLeaderSWM(
            alertWaterEfficiencyLeaderSWM(config, stats, account, refDate));

        status.setAlertPromptGoodJobMonthlySWM(
            alertPromptGoodJobMonthlySWM(config, stats, account, refDate));

        status.setAlertLitresSavedSWM(
            alertLitresSavedSWM(config, account, refDate));

        status.setAlertTop25SaverWeeklySWM(
            alertTop25SaverWeeklySWM(config, stats, account, refDate));

        status.setAlertTop10SaverWeeklySWM(
            alertTop10SaverWeeklySWM(config, stats, account, refDate));

        status.setAlertShowerStillOnAmphiro(
            alertShowerStillOnAmphiro(account, refDate, stats));

        status.setAlertHotTemperatureAmphiro(
            alertHotTemperatureAmphiro(account, refDate, stats));

        status.setAlertNearDailyBudgetAmphiro(
            alertNearDailyBudget(config, account, refDate, EnumDeviceType.AMPHIRO));

        status.setAlertNearWeeklyBudgetAmphiro(
            alertNearWeeklyBudget(config, account, refDate, EnumDeviceType.AMPHIRO));

        status.setAlertReachedDailyBudgetAmphiro(
            alertReachedDailyBudget(config, account, refDate, EnumDeviceType.AMPHIRO));

        status.setAlertShowerChampionAmphiro(
            alertWaterChampion(config, account, refDate, EnumDeviceType.AMPHIRO));

        status.setAlertTooMuchWaterConsumptionAmphiro(
            alertTooMuchWaterConsumption(config, stats, account, refDate, EnumDeviceType.AMPHIRO));

        status.setAlertTooMuchEnergyAmphiro(
            alertTooMuchEnergyAmphiro(config, stats, account, refDate));

        status.setAlertReducedWaterUseAmphiro(
            alertReducedWaterUse(config, account, refDate, EnumDeviceType.AMPHIRO));

        status.setRecommendLessShowerTimeAmphiro(
            recommendLessShowerTimeAmphiro(config, stats, account, refDate));

        status.setRecommendLowerTemperatureAmphiro(
            recommendLowerTemperatureAmphiro(config, stats, account, refDate));

        status.setRecommendLowerFlowAmphiro(
            recommendLowerFlowAmphiro(config, stats, account, refDate));

        status.setRecommendShowerHeadChangeAmphiro(
            recommendShowerHeadChangeAmphiro(config, stats, account, refDate));

        status.setRecommendShampooChangeAmphiro(
            recommendShampooChangeAmphiro(config, stats, account, refDate));

        status.setRecommendReduceFlowWhenNotNeededAmphiro(
            recommendReduceFlowWhenNotNeededAmphiro(config, stats, account, refDate));

        status.setInitialStaticTips(
            initialStaticTipsForAccount(account));

        status.setStaticTip(
            produceStaticTipForAccount(account, config.getTipInterval()));

        // Insight A.1

        for (EnumDeviceType deviceType: deviceTypes)
            status.addInsight(computeInsightA1(config, account, refDate, deviceType));

        // Insight A.2

        for (EnumDeviceType deviceType: deviceTypes)
            status.addInsight(computeInsightA2(config, account, refDate, deviceType));

        // Insight A.3

        for (EnumDeviceType deviceType: deviceTypes)
            for (EnumPartOfDay partOfDay: EnumPartOfDay.values())
                status.addInsight(
                    computeInsightA3(config, account, refDate, deviceType, partOfDay)
                );

        // Insight A.4

        for (EnumDeviceType deviceType: deviceTypes)
            status.addInsight(computeInsightA4(config, account, refDate, deviceType));

        // Insight B.1

        for (EnumDeviceType deviceType: deviceTypes)
            for (EnumTimeUnit u: EnumSet.of(EnumTimeUnit.WEEK, EnumTimeUnit.MONTH))
                status.addInsight(computeInsightB1(config, account, refDate, deviceType, u));

        // Insight B.2

        for (EnumDeviceType deviceType: deviceTypes)
            for (EnumTimeUnit u: EnumSet.of(EnumTimeUnit.WEEK, EnumTimeUnit.MONTH))
                status.addInsight(computeInsightB2(config, account, refDate, deviceType, u));

        // Insight B.3

        for (EnumDeviceType deviceType: deviceTypes)
            status.addInsights(computeInsightB3(config, account, refDate, deviceType));

        // Insight B.4

        for (EnumDeviceType deviceType: deviceTypes)
            status.addInsight(computeInsightB4(config, account, refDate, deviceType));

        // Insight B.5

        for (EnumDeviceType deviceType: deviceTypes)
            status.addInsight(computeInsightB5(config, account, refDate, deviceType));

        return status;
    }

    // Static tips - initial 3 static tips
    private boolean initialStaticTipsForAccount(AccountEntity account)
    {
        DateTime lastCreatedOn = getDateOfLastTip(account.getKey());
        return (lastCreatedOn == null);
    }

    // Static tip
    private boolean produceStaticTipForAccount(AccountEntity account, int staticTipInterval)
    {
        DateTime lastCreatedOn = getDateOfLastTip(account.getKey());
        return (lastCreatedOn == null || lastCreatedOn.isBefore(DateTime.now().minusDays(staticTipInterval)));
    }

    // Alert #1 - Check for water leaks!
    private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterLeakSWM(
        AccountEntity account, DateTime refDate, ConsumptionStats stats)
    {
        final double VOLUME_THRESHOLD_PER_HOUR = 2.0; // lt

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -48, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
            .user("user", account.getKey())
            .meter()
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.METER);
        if (series == null || series.isEmpty())
            return null;

        FluentIterable<Point> points = FluentIterable
            .of(series.iterPoints(VOLUME, EnumMetric.SUM));

        if (points.anyMatch(Point.belowValue(VOLUME_THRESHOLD_PER_HOUR)))
            return null;

        Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
            refDate, EnumDeviceType.METER, EnumAlertTemplate.WATER_LEAK);
        return new MessageResolutionStatus<>(true, parameters);
    }

    // Alert #2 - Shower still on!
    public MessageResolutionStatus<Alert.ParameterizedTemplate> alertShowerStillOnAmphiro(
        AccountEntity account, DateTime refDate, ConsumptionStats stats)
    {
        final int DURATION_THRESHOLD_IN_MINUTES = 30;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
            .user("user", account.getKey())
            .amphiro()
            .max();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return null;

        FluentIterable<Point> points = FluentIterable
            .of(series.iterPoints(DURATION, EnumMetric.MAX));

        if (points.allMatch(Point.belowValue(DURATION_THRESHOLD_IN_MINUTES)))
            return null;

        Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
            refDate, EnumDeviceType.AMPHIRO, EnumAlertTemplate.SHOWER_ON);
        return new MessageResolutionStatus<>(true, parameters);
    }

    // Alert #5 - Water quality not assured!
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterQualitySWM(
        AccountEntity account, DateTime refDate, ConsumptionStats stats)
    {
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
            .user("user", account.getKey())
            .meter()
            .sum();

        // Todo: Fire only when outside temperature is above a threshold

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.METER);

        boolean fire = (series == null || series.isEmpty());
        if (!fire) {
            FluentIterable<Point> points = FluentIterable
                .of(series.iterPoints(VOLUME, EnumMetric.SUM));
            fire = !points.anyMatch(Point.notZero());
        }

        if (!fire)
            return null;

        Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
            refDate, EnumDeviceType.METER, EnumAlertTemplate.WATER_QUALITY);
        return new MessageResolutionStatus<>(true, parameters);
    }

    // Alert #6 - Water too hot!
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertHotTemperatureAmphiro(
        AccountEntity account, DateTime refDate, ConsumptionStats stats)
    {
        final double TEMPERATURE_THRESHOLD = 45.0;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
            .user("user", account.getKey())
            .amphiro()
            .max();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return null;

        FluentIterable<Point> points = FluentIterable
            .of(series.iterPoints(TEMPERATURE, EnumMetric.MAX));

        if (points.allMatch(Point.belowValue(TEMPERATURE_THRESHOLD)))
            return null;

        Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
            refDate, EnumDeviceType.AMPHIRO, EnumAlertTemplate.HIGH_TEMPERATURE);
        return new MessageResolutionStatus<>(true, parameters);
    }

    // Alert #7, #9 - Reached 80% of your daily water budget {integer1} {integer2}
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertNearDailyBudget(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final int BUDGET_NEAR_PERCENTAGE_THRESHOLD = 80;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(deviceType);

        Double consumed  = (series != null)?
            series.get(VOLUME, EnumMetric.SUM) : null;
        if (consumed == null)
            return null;

        int budget = config.getDailyBudget(deviceType);
        Double remaining = (consumed > budget)? 0.0 : (budget - consumed);
        double percentUsed = 100 * (consumed / budget);

        Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                refDate, deviceType,
                (deviceType == EnumDeviceType.AMPHIRO?
                    EnumAlertTemplate.NEAR_DAILY_SHOWER_BUDGET : EnumAlertTemplate.NEAR_DAILY_WATER_BUDGET)
            )
            .setInteger1(consumed.intValue())
            .setInteger2(remaining.intValue());

        return new MessageResolutionStatus<>(
            percentUsed > BUDGET_NEAR_PERCENTAGE_THRESHOLD, parameters);
    }

    // Alert #8, #10 - Reached 80% of your weekly water budget {integer1} {integer2}
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertNearWeeklyBudget(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final int BUDGET_NEAR_PERCENTAGE_THRESHOLD = 80;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(deviceType);

        Double consumed  = (series != null)?
            series.get(VOLUME, EnumMetric.SUM) : null;
        if (consumed == null)
            return null;

        int budget = config.getWeeklyBudget(deviceType);
        Double remaining = (consumed > budget)? 0.0 : (budget - consumed);
        double percentUsed = 100 * (consumed / budget);

        Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                refDate, deviceType,
                (deviceType == EnumDeviceType.AMPHIRO?
                    EnumAlertTemplate.NEAR_WEEKLY_SHOWER_BUDGET: EnumAlertTemplate.NEAR_WEEKLY_WATER_BUDGET)
            )
            .setInteger1(consumed.intValue())
            .setInteger2(remaining.intValue());

        return new MessageResolutionStatus<>(
            percentUsed > BUDGET_NEAR_PERCENTAGE_THRESHOLD, parameters);
    }

    // Alert #11, #12 - Reached daily Water Budget {integer1}
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertReachedDailyBudget(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final int BUDGET_PERCENTAGE_THRESHOLD = 120;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(deviceType);

        Double consumed  = (series != null)?
            series.get(VOLUME, EnumMetric.SUM) : null;
        if (consumed == null)
            return null;

        int budget = config.getDailyBudget(deviceType);
        double percentUsed = 100 * (consumed / budget);

        Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                refDate, deviceType,
                (deviceType == EnumDeviceType.AMPHIRO?
                    EnumAlertTemplate.REACHED_DAILY_SHOWER_BUDGET: EnumAlertTemplate.REACHED_DAILY_WATER_BUDGET)
            )
            .setInteger1(Integer.valueOf(budget))
            .setInteger2(consumed.intValue());

        return new MessageResolutionStatus<>(
            percentUsed > BUDGET_PERCENTAGE_THRESHOLD, parameters);
    }

    // Alert #13, #14 - You are a real water champion!
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterChampion(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final int MAX_NUM_CONSECUTIVE_ZEROS = 10; // to consider the user as absent
        final double dailyBudget = config.getDailyBudget(deviceType);

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(deviceType);
        if (series == null || series.isEmpty())
            return null;

        FluentIterable<Point> points = FluentIterable
            .of(series.iterPoints(VOLUME, EnumMetric.SUM));

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
            return null;

        Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
            refDate, deviceType,
            (deviceType == EnumDeviceType.AMPHIRO)?
                EnumAlertTemplate.SHOWER_CHAMPION : EnumAlertTemplate.WATER_CHAMPION
        );
        return new MessageResolutionStatus<>(true, parameters);
    }

    // Alert #15, #16 - You are using too much water {integer1}
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchWaterConsumption(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final double HIGH_CONSUMPTION_RATIO = 2.0; // in terms of average consumption
        final int WEEKS_PER_YEAR = 52; // not exactly, it's 52 or 53

        ComputedNumber weeklyAverage = stats.get(
            EnumStatistic.AVERAGE_WEEKLY, deviceType, EnumDataField.VOLUME);
        if (weeklyAverage == null || weeklyAverage.getValue() == null)
            return null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(deviceType);

        Double consumed = (series != null)?
            series.get(VOLUME, EnumMetric.SUM) : null;
        if (consumed == null)
            return null;

        if (consumed > HIGH_CONSUMPTION_RATIO * weeklyAverage.getValue()) {
            // Get a rough estimate for annual savings if average behavior is adopted
            Double annualSavings = (consumed - weeklyAverage.getValue()) * WEEKS_PER_YEAR;
            Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                    refDate, deviceType,
                    (deviceType == EnumDeviceType.AMPHIRO)?
                        EnumAlertTemplate.TOO_MUCH_WATER_SHOWER: EnumAlertTemplate.TOO_MUCH_WATER_METER
                )
                .setInteger1(annualSavings.intValue())
                .setInteger2(consumed.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }

        return null;
    }

    // Alert #17 - You are spending too much energy for showering {integer1} {currency}
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchEnergyAmphiro(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        final double HIGH_TEMPERATURE_THRESHOLD = 45.0;
        final double HIGH_TEMPERATURE_RATIO_OF_POINTS = 0.8;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
            .user("user", account.getKey())
            .amphiro()
            .sum()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return null;

        int numPoints = series.size();
        int numPointsHigh = series.count(
            TEMPERATURE, EnumMetric.AVERAGE, Point.aboveValue(HIGH_TEMPERATURE_THRESHOLD));
        double ratioHigh = ((double) numPointsHigh) / numPoints;

        double monthlyConsumption = series.aggregate(VOLUME, EnumMetric.SUM, new Sum());

        if (ratioHigh > HIGH_TEMPERATURE_RATIO_OF_POINTS) {
            Locale locale = Locale.forLanguageTag(account.getLocale());
            double pricePerKwh = priceData.getPricePerKwh(locale);
            Double annualSavings =
                energyCalculator.computeEnergyToRiseTemperature(2, 12 * monthlyConsumption) * pricePerKwh;
            Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                    refDate, EnumDeviceType.AMPHIRO, EnumAlertTemplate.TOO_MUCH_ENERGY
                )
                .setCurrency1(annualSavings);
            return new MessageResolutionStatus<>(true, parameters);
        }

        return null;
    }

    // Alert #18, #19 - You have greatly reduced your water use {integer1} percent
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertReducedWaterUse(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        DataQuery query = null;
        DataQueryResponse queryResponse = null;
        SeriesFacade series = null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        DateTime registerDate = account.getCreatedOn();
        query = queryBuilder
            .sliding(registerDate, +7,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double c0 = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (c0 == null)
            return null;

        query = queryBuilder
            .sliding(refDate, -7,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double c1 = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (c1 == null)
            return null;

        Double percentDiff = 100 * ((c0 - c1) / c0);
        if (percentDiff > 10 && percentDiff < 60) {
            Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                refDate, deviceType,
                (deviceType == EnumDeviceType.AMPHIRO)?
                    EnumAlertTemplate.REDUCED_WATER_USE_SHOWER: EnumAlertTemplate.REDUCED_WATER_USE_METER)
                .setInteger1(percentDiff.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }

        return null;
    }

    // Alert #20 - Congratulations! You are a water efficiency leader {integer1} litres
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterEfficiencyLeaderSWM(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        ComputedNumber monthlyAverage = stats.get(EnumStatistic.AVERAGE_MONTHLY, METER, VOLUME);
        if (monthlyAverage == null || monthlyAverage.getValue() == null)
            return null;

        ComputedNumber monthlyThreshold = stats.get(EnumStatistic.THRESHOLD_BOTTOM_10P_MONTHLY, METER, VOLUME);
        if (monthlyThreshold == null || monthlyThreshold.getValue() == null)
            return null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .user("user", account.getKey())
            .meter()
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.METER);
        Double consumed = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (consumed == null)
            return null;

        if (consumed < Math.min(monthlyThreshold.getValue(), monthlyAverage.getValue())) {
            int annualSavings = (int) (monthlyAverage.getValue() - consumed) * 12;
            Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                    refDate, EnumDeviceType.METER, EnumAlertTemplate.WATER_EFFICIENCY_LEADER
                )
                .setInteger1(annualSavings);
            return new MessageResolutionStatus<>(true, parameters);
        }

        return null;
    }

    // Alert #21 - noop

    // Alert #22 - You are doing a great job!
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertPromptGoodJobMonthlySWM(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        ComputedNumber monthlyAverage = stats.get(EnumStatistic.AVERAGE_WEEKLY, METER, VOLUME);
        if (monthlyAverage == null || monthlyAverage.getValue() == null)
            return null;

        DataQuery query = null;
        DataQueryResponse queryResponse = null;
        SeriesFacade series = null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .meter()
            .sum();

        query = queryBuilder
            .sliding(refDate, -30,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(EnumDeviceType.METER);
        Double c0 = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (c0 == null)
            return null;

        query = queryBuilder
            .sliding(refDate.minusDays(30), -30,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(EnumDeviceType.METER);
        Double c1 = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (c1 == null)
            return null;

        Double percentDiff = 100 * ((c1 - c0) / c1);
        if (percentDiff > 25 || (percentDiff > 6 && c0 < monthlyAverage.getValue())) {
            Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                    refDate, EnumDeviceType.METER, EnumAlertTemplate.GOOD_JOB_MONTHLY)
                .setInteger1(percentDiff.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }
        return null;
    }

    // Alert #23 - You have already saved {integer1} litres of water!
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertLitresSavedSWM(
        IMessageGeneratorService.Configuration config, AccountEntity account, DateTime refDate)
    {
        final double VOLUME_WEEKLY_DIFF_THRESHOLD = 100;

        DataQuery query = null;
        DataQueryResponse queryResponse = null;
        SeriesFacade series = null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .meter()
            .sum();

        query = queryBuilder
            .sliding(refDate, -7,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(EnumDeviceType.METER);
        Double c0 = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (c0 == null)
            return null;

        query = queryBuilder
            .sliding(refDate.minusDays(7), -7,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(EnumDeviceType.METER);
        Double c1 = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (c1 == null)
            return null;

        Double diff = c1 - c0;
        if (diff > VOLUME_WEEKLY_DIFF_THRESHOLD) {
            Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                refDate, EnumDeviceType.METER, EnumAlertTemplate.LITERS_ALREADY_SAVED)
            .setInteger1(diff.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }

        return null;
    }

    // Alert #24 - Congratulations! You are one of the top 25% savers in your region.
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTop25SaverWeeklySWM(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        ComputedNumber weeklyThreshold = stats.get(EnumStatistic.THRESHOLD_BOTTOM_25P_WEEKLY, METER, VOLUME);
        if (weeklyThreshold == null || weeklyThreshold.getValue() == null)
            return null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .meter()
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.METER);
        Double c0 = (series != null)? series.get(VOLUME, EnumMetric.SUM): null;
        if (c0 == null)
            return null;

        if (c0 < weeklyThreshold.getValue()) {
            Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                refDate, EnumDeviceType.METER, EnumAlertTemplate.TOP_25_PERCENT_OF_SAVERS
            );
            return new MessageResolutionStatus<>(true, parameters);
        }
        return null;
    }

    // Alert #25 - Congratulations! You are among the top 10% group of savers in your region.
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTop10SaverWeeklySWM(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        ComputedNumber weeklyThreshold = stats.get(EnumStatistic.THRESHOLD_BOTTOM_10P_WEEKLY, METER, VOLUME);
        if (weeklyThreshold == null || weeklyThreshold.getValue() == null)
            return null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .meter()
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.METER);
        Double c0 = (series != null)? series.get(VOLUME, EnumMetric.SUM): null;
        if (c0 == null)
            return null;

        if (c0 < weeklyThreshold.getValue()) {
            Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                refDate, EnumDeviceType.METER, EnumAlertTemplate.TOP_10_PERCENT_OF_SAVERS
            );
            return new MessageResolutionStatus<>(true, parameters);
        }

        return null;
    }

    // Recommendation #1 - Spend 1 less minute in the shower and save {integer1} {integer2}
    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLessShowerTimeAmphiro(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        ComputedNumber monthlyAverageDuration = stats.get(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, DURATION);
        if (monthlyAverageDuration == null || monthlyAverageDuration.getValue() == null)
            return null;

        ComputedNumber monthlyAverageConsumption = stats.get(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, VOLUME);
        if (monthlyAverageConsumption == null || monthlyAverageConsumption.getValue() == null)
            return null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .amphiro()
            .sum()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return null;

        Double quarterlyUserConsumption = series.get(VOLUME, EnumMetric.SUM);
        if (quarterlyUserConsumption == null)
            return null;
        Double monthlyUserAverageConsumption = quarterlyUserConsumption / 3;

        Double monthlyUserAverageDuration = series.get(DURATION, EnumMetric.AVERAGE);
        if (monthlyUserAverageDuration == null)
            return null;

        boolean fire = (
            (monthlyUserAverageDuration > monthlyAverageDuration.getValue() * 1.5) &&
            (monthlyUserAverageConsumption > monthlyAverageConsumption.getValue())
        );
        if (fire) {
            Double annualSavings =
                (monthlyUserAverageConsumption - monthlyAverageConsumption.getValue()) * 12;
            Recommendation.ParameterizedTemplate parameters = new Recommendation.SimpleParameterizedTemplate(
                    refDate, EnumDeviceType.AMPHIRO,
                    EnumRecommendationTemplate.LESS_SHOWER_TIME
                )
                .setInteger1(annualSavings.intValue())
                .setInteger2(Double.valueOf(annualSavings * 2.0).intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }

        return null;
    }

    // Recommendation #2 - You could save {currency1} euros if you used a bit less hot water in the shower. {currency2}
    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLowerTemperatureAmphiro(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        ComputedNumber monthlyAverageTemperature = stats.get(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, TEMPERATURE);
        if (monthlyAverageTemperature == null || monthlyAverageTemperature.getValue() == null)
            return null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .amphiro()
            .sum()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return null;

        Double quarterlyUserConsumption = series.get(VOLUME, EnumMetric.SUM);
        if (quarterlyUserConsumption == null)
            return null;
        Double monthlyUserAverageConsumption = quarterlyUserConsumption / 3;

        Double monthlyUserAverageTemperature = series.get(TEMPERATURE, EnumMetric.AVERAGE);
        if (monthlyUserAverageTemperature == null)
            return null;

        boolean fire = (monthlyUserAverageTemperature > monthlyAverageTemperature.getValue() * 1.0);
        if (fire) {
            double annualUserAverageConsumption = monthlyUserAverageConsumption * 12;
            Locale locale = Locale.forLanguageTag(account.getLocale());
            double pricePerKwh = priceData.getPricePerKwh(locale);
            Double annualSavings =
                energyCalculator.computeEnergyToRiseTemperature(2, annualUserAverageConsumption) * pricePerKwh;
            Recommendation.ParameterizedTemplate parameters = new Recommendation.SimpleParameterizedTemplate(
                    refDate, EnumDeviceType.AMPHIRO,
                    EnumRecommendationTemplate.LOWER_TEMPERATURE
                )
                .setCurrency1(annualSavings)
                .setCurrency2(annualSavings);
            return new MessageResolutionStatus<>(true, parameters);
        }

        return null;
    }

    // Recommendation #3 - Reduce the water flow in the shower and gain {integer1} {integer2}
    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLowerFlowAmphiro(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        ComputedNumber monthlyAverageFlow = stats.get(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, FLOW);
        if (monthlyAverageFlow == null || monthlyAverageFlow.getValue() == null)
            return null;

        ComputedNumber monthlyAverageConsumption = stats.get(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, VOLUME);
        if (monthlyAverageConsumption == null || monthlyAverageConsumption.getValue() == null)
            return null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .amphiro()
            .sum()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return null;

        Double monthlyUserAverageFlow = series.get(FLOW, EnumMetric.AVERAGE);
        if (monthlyUserAverageFlow == null)
            return null;
        if (monthlyUserAverageFlow < monthlyAverageFlow.getValue())
            return null;

        Double quarterUserConsumption = series.get(VOLUME, EnumMetric.SUM);
        if (quarterUserConsumption == null)
            return null;

        Double annualUserConsumption = quarterUserConsumption * 4;
        Double annualAverageConsumption = monthlyAverageConsumption.getValue() * 12;
        if (annualUserConsumption > annualAverageConsumption) {
            Double annualSavings = annualUserConsumption - annualAverageConsumption;
            Recommendation.ParameterizedTemplate parameters = new Recommendation.SimpleParameterizedTemplate(
                    refDate, EnumDeviceType.AMPHIRO, EnumRecommendationTemplate.LOWER_FLOW
                )
                .setInteger1(annualSavings.intValue())
                .setInteger2(annualSavings.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }

        return null;
    }

    // Recommendation #4 - Change your shower head and save {integer1} {integer2}
    // Todo: This computation is identical to Recommendation #3, maybe discard #4
    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendShowerHeadChangeAmphiro(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        ComputedNumber monthlyAverageFlow = stats.get(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, FLOW);
        if (monthlyAverageFlow == null || monthlyAverageFlow.getValue() == null)
            return null;

        ComputedNumber monthlyAverageConsumption = stats.get(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, VOLUME);
        if (monthlyAverageConsumption == null || monthlyAverageConsumption.getValue() == null)
            return null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .amphiro()
            .sum()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return null;

        Double monthlyUserAverageFlow = series.get(FLOW, EnumMetric.AVERAGE);
        if (monthlyUserAverageFlow == null)
            return null;

        Double quarterUserConsumption = series.get(VOLUME, EnumMetric.SUM);
        if (quarterUserConsumption == null)
            return null;

        Double annualUserConsumption = quarterUserConsumption * 4;
        Double annualAverageConsumption = monthlyAverageConsumption.getValue() * 12;
        if (annualUserConsumption > annualAverageConsumption) {
            Double annualSavings = annualUserConsumption - annualAverageConsumption;
            Recommendation.ParameterizedTemplate parameters = new Recommendation.SimpleParameterizedTemplate(
                    refDate, EnumDeviceType.AMPHIRO, EnumRecommendationTemplate.CHANGE_SHOWERHEAD
                )
                .setInteger1(annualSavings.intValue())
                .setInteger2(annualSavings.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }
        return null;
    }

    // Recommendation #5 - Have you considered changing your shampoo? {integer1} percent
    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendShampooChangeAmphiro(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        ComputedNumber monthlyAverageConsumption = stats.get(
            EnumStatistic.AVERAGE_MONTHLY, EnumDeviceType.AMPHIRO, VOLUME);
        if (monthlyAverageConsumption == null || monthlyAverageConsumption.getValue() == null)
            return null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .amphiro()
            .sum()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return null;

        Double quarterUserConsumption = series.get(VOLUME, EnumMetric.SUM);
        if (quarterUserConsumption == null)
            return null;
        double monthlyUserAverageConsumption = quarterUserConsumption / 3;

        if (monthlyUserAverageConsumption > monthlyAverageConsumption.getValue()) {
            // Compute percent of usage above others
            Double percentDiff = 100.0 *
                ((monthlyUserAverageConsumption / monthlyAverageConsumption.getValue()) - 1.0);
            Recommendation.ParameterizedTemplate parameters = new Recommendation.SimpleParameterizedTemplate(
                    refDate, EnumDeviceType.AMPHIRO, EnumRecommendationTemplate.CHANGE_SHAMPOO
                )
                .setInteger1(percentDiff.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }
        return null;
    }

    // Recommendation #6 - When showering, reduce the water flow when you do not need it {integer1} {integer2}
    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendReduceFlowWhenNotNeededAmphiro(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate)
    {
        ComputedNumber monthlyAveragePerSession = stats.get(
            EnumStatistic.AVERAGE_MONTHLY_PER_SESSION, EnumDeviceType.AMPHIRO, VOLUME);
        if (monthlyAveragePerSession == null || monthlyAveragePerSession.getValue() == null)
            return null;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .sliding(refDate, -3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .amphiro()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        SeriesFacade series = queryResponse.getFacade(EnumDeviceType.AMPHIRO);
        if (series == null || series.isEmpty())
            return null;

        Double monthlyUserAveragePerSession = series.get(VOLUME, EnumMetric.AVERAGE);
        if (monthlyUserAveragePerSession == null)
            return null;

        // Todo - Calculate the number of sessions per year when available
        // Todo - Add additional check for flow adjustments during the session when available
        int numberOfSessionsPerYear = 100;
        if (monthlyUserAveragePerSession > monthlyAveragePerSession.getValue()) {
            // Compute liters more than average
            Double moreLitersThanOthersInYear =
                (monthlyUserAveragePerSession - monthlyAveragePerSession.getValue()) * numberOfSessionsPerYear;
            Recommendation.ParameterizedTemplate parameters = new Recommendation.SimpleParameterizedTemplate(
                    refDate, EnumDeviceType.AMPHIRO, EnumRecommendationTemplate.REDUCE_FLOW_WHEN_NOT_NEEDED
                )
                .setInteger1(moreLitersThanOthersInYear.intValue())
                .setInteger2(moreLitersThanOthersInYear.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }

        return null;
    }

    private boolean isMeterInstalledForUser(AccountEntity account)
    {
        DeviceRegistrationQuery query = new DeviceRegistrationQuery(EnumDeviceType.METER);
        return !deviceRepository.getUserDevices(account.getKey(), query).isEmpty();
    }

    private boolean isAmphiroInstalledForUser(AccountEntity account)
    {
        DeviceRegistrationQuery query = new DeviceRegistrationQuery(EnumDeviceType.AMPHIRO);
        return !deviceRepository.getUserDevices(account.getKey(), query).isEmpty();
    }

    private MessageResolutionStatus<Insight.ParameterizedTemplate> computeInsightA1(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final double K = 1.28;  // a threshold (in units of standard deviation) of significant change
        final int N = 12;       // number of past weeks to examine
        final double F = 0.5;   // a threshold ratio of non-nulls for collected values

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Compute for target day

        query = queryBuilder
            .sliding(refDate, +1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double targetValue = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (targetValue == null || targetValue < VOLUME_DAILY_THRESHOLD)
            return null; // nothing to compare to

        // Compute for past N weeks for a given day-of-week

        DateTime start = refDate;
        SummaryStatistics summary = new SummaryStatistics();
        for (int i = 0; i < N; i++) {
            start = start.minusWeeks(1);
            query = queryBuilder
                .sliding(start, +1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            Double val = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
            if (val != null)
                summary.addValue(val);
        }
        if (summary.getN() < N * F)
            return null; // too few values

        // Seems we have sufficient data for the past weeks

        double avgValue = summary.getMean();
        if (avgValue < VOLUME_DAILY_THRESHOLD)
            return null; // not reliable; consumption is too low

        double sd = Math.sqrt(summary.getPopulationVariance());
        double normValue = (sd > 0)? ((targetValue - avgValue) / sd) : Double.POSITIVE_INFINITY;
        double score = (sd > 0)? (Math.abs(normValue) / (2 * K)) : Double.POSITIVE_INFINITY;

        logger.debug(String.format(
            "Insight A1 for account %s/%s: Consumption for same week day of last %d weeks to %s:%n  " +
                "value=%.2f μ=%.2f σ=%.2f x*=%.2f score=%.2f",
             account.getKey(), deviceType, N, refDate.toString("EEE dd/MM/YYYY"),
             targetValue, avgValue, sd, normValue, score));

        return new MessageResolutionStatus<Insight.ParameterizedTemplate>(
            score,
            new Insight.A1Parameters(refDate, deviceType, targetValue, avgValue)
        );
    }

    /**
     * Note: The logic for insight A.2 is same with B.1 (merge?)
     */
    private MessageResolutionStatus<Insight.ParameterizedTemplate> computeInsightA2(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final double K = 1.28;  // a threshold (in units of standard deviation) of significant change
        final int N = 30;       // number of past days to examine
        final double F = 0.6;   // a threshold ratio of non-nulls for collected values

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Compute for target day

        query = queryBuilder
            .sliding(refDate, +1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double targetValue = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (targetValue == null || targetValue < VOLUME_DAILY_THRESHOLD)
            return null; // nothing to compare to

        // Compute for past N days

        DateTime start = refDate;
        SummaryStatistics summary = new SummaryStatistics();
        for (int i = 0; i < N; i++) {
            start = start.minusDays(1);
            query = queryBuilder
                .sliding(start, +1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            Double val = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
            if (val != null)
                summary.addValue(val);
        }
        if (summary.getN() < N * F)
            return null; // too few values

        // Seems we have sufficient data for the past days

        double avgValue = summary.getMean();
        if (avgValue < VOLUME_DAILY_THRESHOLD)
            return null; // not reliable; consumption is too low

        double sd = Math.sqrt(summary.getPopulationVariance());
        double normValue = (sd > 0)? ((targetValue - avgValue) / sd) : Double.POSITIVE_INFINITY;
        double score = (sd > 0)? (Math.abs(normValue) / (2 * K)) : Double.POSITIVE_INFINITY;

        logger.debug(String.format(
            "Insight A2 for account %s/%s: Consumption for last %d days to %s:%n  " +
                "value=%.2f μ=%.2f σ=%.2f x*=%.2f score=%.2f",
             account.getKey(), deviceType, N, refDate.toString("dd/MM/YYYY"),
             targetValue, avgValue, sd, normValue, score));

        return new MessageResolutionStatus<Insight.ParameterizedTemplate>(
            score,
            new Insight.A2Parameters(refDate, deviceType, targetValue, avgValue)
        );
    }

    private MessageResolutionStatus<Insight.ParameterizedTemplate> computeInsightA3(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType, EnumPartOfDay partOfDay)
    {
        final double PERCENTAGE_CHANGE_THRESHOLD = 40;
        final int N = 30;       // number of past days to examine
        final double F = 0.6;   // a threshold ratio of non-nulls for collected values
        final double threshold = VOLUME_DAILY_THRESHOLD * partOfDay.asFractionOfDay();

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Compute for part-of-day for target day

        Interval r = partOfDay.toInterval(refDate);
        query = queryBuilder
            .absolute(r.getStart(), r.getEnd(), EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double targetValue = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (targetValue == null || targetValue < threshold)
            return null; // nothing to compare to

        // Compute for part-of-day for past N days

        // Note: Querying for each partOfDay is not so efficient, maybe query for
        // the whole day and keep separate sums
        DateTime start = refDate;
        SummaryStatistics summary = new SummaryStatistics();
        for (int i = 0; i < N; i++) {
            start = start.minusDays(1);
            Interval r1 = partOfDay.toInterval(start);
            query = queryBuilder
                .absolute(r1.getStart(), r1.getEnd(), EnumTimeAggregation.ALL)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            Double val = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
            if (val != null)
                summary.addValue(val);
        }
        if (summary.getN() < N * F)
            return null; // too few values

        // Seems we have sufficient data for the past days

        double avgValue = summary.getMean();
        if (avgValue < threshold)
            return null; // not reliable; consumption is too low

        double percentDiff = 100.0 * (targetValue - avgValue) / avgValue;
        double score = Math.abs(percentDiff) / (2 * PERCENTAGE_CHANGE_THRESHOLD);

        logger.debug(String.format(
            "Insight A3 for account %s/%s: Consumption at %s of last %d days to %s:%n  " +
                "value=%.2f μ=%.2f score=%.2f",
             account.getKey(), deviceType, partOfDay, N, refDate.toString("dd/MM/YYYY"),
             targetValue, avgValue, score));

        return new MessageResolutionStatus<Insight.ParameterizedTemplate>(
            score,
            new Insight.A3Parameters(refDate, partOfDay, deviceType, targetValue, avgValue)
        );
    }

    private MessageResolutionStatus<Insight.ParameterizedTemplate> computeInsightA4(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Compute for every part-of-day for target day

        EnumMap<EnumPartOfDay, Double> parts = new EnumMap<>(EnumPartOfDay.class);
        double sumOfParts = 0;
        boolean missingPart = false;
        for (EnumPartOfDay partOfDay: EnumPartOfDay.values()) {
            Interval r = partOfDay.toInterval(refDate);
            query = queryBuilder
                .absolute(r.getStart(), r.getEnd(), EnumTimeAggregation.ALL)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            Double y = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
            if (y == null) {
                missingPart = true;
                break;
            }
            sumOfParts += y;
            parts.put(partOfDay, y);
        }

        if (missingPart)
            return null;

        if (sumOfParts < VOLUME_DAILY_THRESHOLD)
            return null; // not reliable; overall consumption is too low

        // We have sufficient data for all parts of target day

        logger.debug(String.format(
            "Insight A4 for account %s/%s: Consumption for %s is %.2flt:%n  " +
                "morning=%.2f%% afternoon=%.2f%% night=%.2f%%",
             account.getKey(), deviceType, refDate.toString("dd/MM/YYYY"), sumOfParts,
             100 * parts.get(EnumPartOfDay.MORNING) / sumOfParts,
             100 * parts.get(EnumPartOfDay.AFTERNOON) / sumOfParts,
             100 * parts.get(EnumPartOfDay.NIGHT) / sumOfParts
        ));

        Insight.ParameterizedTemplate parameters = new Insight.A4Parameters(refDate, deviceType, sumOfParts)
            .setParts(parts);
        return new MessageResolutionStatus<>(true, parameters);
    }

    private MessageResolutionStatus<Insight.ParameterizedTemplate> computeInsightB1(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType, EnumTimeUnit timeUnit)
    {
        Assert.state(timeUnit == EnumTimeUnit.WEEK || timeUnit == EnumTimeUnit.MONTH);

        final double K = 1.28;  // a threshold (in units of standard deviation) of significant change
        final double F = 0.6;   // a threshold ratio of non-nulls for collected values
        final DateTime targetDate = timeUnit.startOf(refDate);
        final Period period = timeUnit.toPeriod();
        final Period P = Period.months(+2); // the whole period under examination
        final int N = // number of unit-sized periods
            timeUnit.numParts(new Interval(targetDate.minus(P), targetDate));
        final double threshold = volumeThreshold.get(timeUnit);

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Compute for target period

        query = queryBuilder
            .sliding(targetDate, +1, timeUnit, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double targetValue = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (targetValue == null || targetValue < threshold)
            return null; // nothing to compare to

        // Compute for past N periods

        DateTime start = targetDate;
        SummaryStatistics summary = new SummaryStatistics();
        for (int i = 0; i < N; i++) {
            start = start.minus(period);
            query = queryBuilder
                .sliding(start, +1, timeUnit, EnumTimeAggregation.ALL)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            Double val = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
            if (val != null)
                summary.addValue(val);
        }
        if (summary.getN() < N * F)
            return null; // too few values

        // Seems we have sufficient data

        double avgValue = summary.getMean();
        if (avgValue < threshold)
            return null; // not reliable; consumption is too low

        double sd = Math.sqrt(summary.getPopulationVariance());
        double normValue = (sd > 0)? ((targetValue - avgValue) / sd) : Double.POSITIVE_INFINITY;
        double score = (sd > 0)? (Math.abs(normValue) / (2 * K)) : Double.POSITIVE_INFINITY;

        logger.debug(String.format(
            "Insight B1 for account %s/%s: Consumption for period %s to %s:%n  " +
                "value=%.2f μ=%.2f σ=%.2f x*=%.2f score=%.2f",
             account.getKey(), deviceType, period.multipliedBy(N), targetDate.toString("dd/MM/YYYY"),
             targetValue, avgValue, sd, normValue, score));

        return new MessageResolutionStatus<Insight.ParameterizedTemplate>(
            score,
            new Insight.B1Parameters(refDate, timeUnit, deviceType, targetValue, avgValue)
        );
    }

    private MessageResolutionStatus<Insight.ParameterizedTemplate> computeInsightB2(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType, EnumTimeUnit timeUnit)
    {
        Assert.state(timeUnit == EnumTimeUnit.WEEK || timeUnit == EnumTimeUnit.MONTH);

        final double PERCENTAGE_CHANGE_THRESHOLD = 40;
        final DateTime targetDate = timeUnit.startOf(refDate);
        final Period period = timeUnit.toPeriod();
        final double threshold = volumeThreshold.get(timeUnit);

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Compute for target period

        query = queryBuilder
            .sliding(targetDate, +1, timeUnit, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double targetValue = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (targetValue == null || targetValue < threshold)
            return null; // nothing to compare to

        // Compute for previous period

        query = queryBuilder
            .sliding(targetDate.minus(period), +1, timeUnit, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double previousValue = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (previousValue == null || previousValue < threshold)
            return null; // nothing to compare to

        // Seems we have sufficient data

        double percentDiff = 100.0 * (targetValue - previousValue) / previousValue;
        double score = Math.abs(percentDiff) / (2 * PERCENTAGE_CHANGE_THRESHOLD);

        logger.debug(String.format(
            "Insight B2 for account %s/%s: Consumption for previous period %s of %s:%n  " +
                "value=%.2f previous=%.2f change=%.2f%% score=%.2f",
             account.getKey(), deviceType, period, targetDate.toString("dd/MM/YYYY"),
             targetValue, previousValue, percentDiff, score));

        return new MessageResolutionStatus<Insight.ParameterizedTemplate>(
            score,
            new Insight.B2Parameters(refDate, timeUnit, deviceType, targetValue, previousValue)
        );
    }

    private List<MessageResolutionStatus<Insight.ParameterizedTemplate>> computeInsightB3(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final double F = 0.6; // a threshold ratio of non-nulls for collected values
        final DateTime targetDate = EnumTimeUnit.WEEK.startOf(refDate);
        final DateTimeZone tz = refDate.getZone();
        final int N = 8; // number of weeks to examine

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(tz)
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Initialize sums for each day of week, and sum for all days

        Map<EnumDayOfWeek, Sum> sumPerDay = new EnumMap<>(EnumDayOfWeek.class);
        for (EnumDayOfWeek day: EnumDayOfWeek.values())
            sumPerDay.put(day, new Sum());
        Sum sum = new Sum();

        // Fetch data for N past weeks

        DateTime start = targetDate.plusWeeks(1);
        for (int i = 0; i < N; i++) {
            DateTime end = start;
            start = start.minusWeeks(1);
            // Execute query for current week
            query = queryBuilder
                .absolute(start, end, EnumTimeAggregation.DAY)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            if (series == null)
                continue;
            FluentIterable<Point> points = FluentIterable
                .of(series.iterPoints(VOLUME, EnumMetric.SUM))
                .filter(Point.betweenTime(start, end));
            // Update partial sums for each day of week
            for (Point p: points) {
                DateTime t = p.getTimestamp().toDateTime(tz);
                double value = p.getValue();
                EnumDayOfWeek day = EnumDayOfWeek.valueOf(t.getDayOfWeek());
                sumPerDay.get(day).increment(value);
                sum.increment(value);
            }
        }

        // Do we have sufficient data for each day?

        boolean sufficient = true;
        for (EnumDayOfWeek day: EnumDayOfWeek.values())
            if (sumPerDay.get(day).getN() < N * F) {
                sufficient = false;
                break;
            }
        if (!sufficient)
            return Collections.emptyList();

        // Compute average daily consumption per each day-of-week; Find peak days

        double minOfDay = Double.POSITIVE_INFINITY, maxOfDay = Double.NEGATIVE_INFINITY;
        EnumDayOfWeek dayMin = null, dayMax = null;
        for (EnumDayOfWeek day: EnumDayOfWeek.values()) {
            Sum sy = sumPerDay.get(day);
            double y = sy.getResult() / sy.getN();
            if (y < minOfDay) {
                minOfDay = y;
                dayMin = day;
            }
            if (y > maxOfDay) {
                maxOfDay = y;
                dayMax = day;
            }
        }

        if (maxOfDay < VOLUME_DAILY_THRESHOLD)
            return Collections.emptyList(); // not reliable; overall consumption is too low

        // Compute average daily consumption for all days

        double avg = sum.getResult() / sum.getN();

        // Produce 2 insights, one for each peak (min, max)

        logger.debug(String.format(
            "Insight B3 for account %s/%s: Consumption for %d weeks to %s:%n  " +
                "min=%.2f dayMin=%s - max=%.2f dayMax=%s - average=%.2f",
             account.getKey(), deviceType, N, targetDate.plusWeeks(1).toString("dd/MM/YYYY"),
             minOfDay, dayMin, maxOfDay, dayMax, avg));

        return Arrays.asList(
            new MessageResolutionStatus<Insight.ParameterizedTemplate>(
                true,
                new Insight.B3Parameters(refDate, deviceType, minOfDay, avg, dayMin)
            ),
            new MessageResolutionStatus<Insight.ParameterizedTemplate>(
                true,
                new Insight.B3Parameters(refDate, deviceType, maxOfDay, avg, dayMax)
            )
        );
    }

    private MessageResolutionStatus<Insight.ParameterizedTemplate> computeInsightB4(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final double F = 0.6; // a threshold ratio of non-nulls for collected values
        final DateTime targetDate = EnumTimeUnit.WEEK.startOf(refDate);
        final DateTimeZone tz = refDate.getZone();
        final int N = 8; // number of weeks to examine

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(tz)
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Initialize sums for working/weekend days

        Map<EnumDayOfWeek.Type, Sum> sumPerType = new EnumMap<>(EnumDayOfWeek.Type.class);
        sumPerType.put(EnumDayOfWeek.Type.WEEKDAY, new Sum());
        sumPerType.put(EnumDayOfWeek.Type.WEEKEND, new Sum());

        // Fetch data for N past weeks

        DateTime start = targetDate.plusWeeks(1);
        for (int i = 0; i < N; i++) {
            DateTime end = start;
            start = start.minusWeeks(1);
            // Execute query for current week
            query = queryBuilder
                .absolute(start, end, EnumTimeAggregation.DAY)
                .build();
            queryResponse = dataService.execute(query);
            series = queryResponse.getFacade(deviceType);
            if (series == null)
                continue;
            FluentIterable<Point> points = FluentIterable
                .of(series.iterPoints(VOLUME, EnumMetric.SUM))
                .filter(Point.betweenTime(start, end));
            // Update partial sums for each type (working/weekend day)
            for (Point p: points) {
                DateTime t = p.getTimestamp().toDateTime(tz);
                double value = p.getValue();
                EnumDayOfWeek day = EnumDayOfWeek.valueOf(t.getDayOfWeek());
                sumPerType.get(day.getType()).increment(value);
            }
        }

        // Do we have sufficient data?

        Sum weekdaySum = sumPerType.get(EnumDayOfWeek.Type.WEEKDAY);
        Sum weekendSum = sumPerType.get(EnumDayOfWeek.Type.WEEKEND);

        final int N1 = (int) (N * F);
        if (weekdaySum.getN() < 5 * N1 || weekendSum.getN() < 2 * N1)
            return null;

        // Compute average for each type (working/weekend) day

        double weekdayAverage = weekdaySum.getResult() / weekdaySum.getN();
        double weekendAverage = weekendSum.getResult() / weekendSum.getN();

        if (weekdayAverage < VOLUME_DAILY_THRESHOLD && weekendAverage < VOLUME_DAILY_THRESHOLD)
            return null; // not reliable; both parts have too low consumption

        logger.debug(String.format(
            "Insight B4 for account %s/%s: Consumption for %d weeks to %s:%n  " +
                "weekday-average=%.2f weekend-average=%.2f",
             account.getKey(), deviceType, N, targetDate.plusWeeks(1).toString("dd/MM/YYYY"),
             weekdayAverage, weekendAverage));

        return new MessageResolutionStatus<Insight.ParameterizedTemplate>(
            true,
            new Insight.B4Parameters(refDate, deviceType, weekdayAverage, weekendAverage)
        );
    }

    private MessageResolutionStatus<Insight.ParameterizedTemplate> computeInsightB5(
        IMessageGeneratorService.Configuration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final DateTime targetDate = EnumTimeUnit.MONTH.startOf(refDate);
        final DateTimeZone tz = refDate.getZone();
        final double threshold = // approx 30.5 days per month
            30.5 * VOLUME_DAILY_THRESHOLD;

        // Build a common part of a data-service query

        DataQuery query;
        DataQueryResponse queryResponse;
        SeriesFacade series;

        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(tz)
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();

        // Compute for target month

        query = queryBuilder
            .sliding(targetDate, +1, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double targetValue = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (targetValue == null || targetValue < threshold)
            return null; // nothing to compare to

        // Compute for same month a year ago

        query = queryBuilder
            .sliding(targetDate.minusYears(1), +1, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        series = queryResponse.getFacade(deviceType);
        Double previousValue = (series != null)? series.get(VOLUME, EnumMetric.SUM) : null;
        if (previousValue == null || previousValue < threshold)
            return null; // nothing to compare to

        // Seems we have sufficient data

        logger.debug(String.format(
            "Insight B5 for account %s/%s: Consumption for month %s compared to %s (a year ago):%n  " +
                "value=%.2f previous=%.2f",
             account.getKey(), deviceType,
             targetDate.toString("MM/YYYY"), targetDate.minusYears(1).toString("MM/YYYY"),
             targetValue, previousValue));

        return new MessageResolutionStatus<Insight.ParameterizedTemplate>(
            true,
            new Insight.B5Parameters(refDate, deviceType, targetValue, previousValue)
        );
    }

    //
    // ~ Helpers
    //

    private DateTime getDateOfLastTip(UUID accountKey)
    {
        AccountTipEntity e = accountTipRepository.findLastForAccount(accountKey);
        return (e == null)? null : e.getCreatedOn();
    }
}
