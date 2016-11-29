package eu.daiad.web.service.message;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.DynamicRecommendation;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;
import eu.daiad.web.model.message.EnumPartOfDay;
import eu.daiad.web.model.message.IMessageResolutionStatus;
import eu.daiad.web.model.message.Insight;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.model.query.AmphiroDataPoint;
import eu.daiad.web.model.query.DataPoint;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumDataField;
import eu.daiad.web.model.query.EnumMeasurementDataSource;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.EnumTimeAggregation;
import eu.daiad.web.model.query.EnumTimeUnit;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MeterDataPoint;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.user.UserInfo;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IAccountStaticRecommendationRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.IEnergyCalculator;
import eu.daiad.web.service.IPriceDataService;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountStaticRecommendationEntity;
import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.ConsumptionStats.EnumStatistic;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.error.DeviceErrorCode;

import static org.apache.commons.math3.stat.StatUtils.mean;
import static org.apache.commons.math3.stat.StatUtils.populationVariance;

import static eu.daiad.web.model.device.EnumDeviceType.AMPHIRO;
import static eu.daiad.web.model.device.EnumDeviceType.METER;
import static eu.daiad.web.model.query.EnumDataField.VOLUME;
import static eu.daiad.web.model.query.EnumDataField.TEMPERATURE;
import static eu.daiad.web.model.query.EnumDataField.DURATION;
import static eu.daiad.web.model.query.EnumDataField.FLOW;
import static eu.daiad.web.model.query.EnumDataField.ENERGY;

@Service()
public class DefaultMessageResolverService implements IMessageResolverService 
{
    private static final Log logger = LogFactory.getLog(DefaultMessageResolverService.class);
    
    @Autowired
    IDataService dataService;

    @Autowired
    IUserRepository userRepository;
    
    @Autowired
    IAccountStaticRecommendationRepository accountStaticRecommendationRepository;
        
    @Autowired 
    IDeviceRepository deviceRepository;      
    
    @Autowired
    IPriceDataService priceData;
    
    @Autowired
    IEnergyCalculator energyCalculator;
    
    @Override
    public MessageResolutionPerAccountStatus resolve(
        MessageCalculationConfiguration config, 
        UtilityInfo utility, ConsumptionStats stats, UUID accountKey) 
    {      
        AccountEntity account = userRepository.getAccountByKey(accountKey);
               
        DateTimeZone tz = DateTimeZone.forID(utility.getTimezone());
        DateTime refDate = config.getRefDate().toDateTime(tz);

        EnumDeviceType[] deviceTypes = new EnumDeviceType[] {
                EnumDeviceType.AMPHIRO, EnumDeviceType.METER
        };
        
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
            produceStaticTipForAccount(account, config.getStaticTipInterval()));
        
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
        
       // Insight A.3
        
        return status;
    }
    
    // Static tips - initial 3 static tips
    private boolean initialStaticTipsForAccount(AccountEntity account) 
    {  
        DateTime lastCreatedOn = getDateOfLastStaticRecommendation(account.getKey());
        return (lastCreatedOn == null);
    }
        
    // Static tip
    private boolean produceStaticTipForAccount(AccountEntity account, int staticTipInterval) 
    {
        DateTime lastCreatedOn = getDateOfLastStaticRecommendation(account.getKey());
        return (lastCreatedOn == null || lastCreatedOn.isBefore(DateTime.now().minusDays(staticTipInterval)));
    }
        
    // Alert #1 - Check for water leaks!
    private IMessageResolutionStatus<Alert.Parameters> alertWaterLeakSWM(
        AccountEntity account, DateTime refDate, ConsumptionStats stats) 
    {
        final double VOLUME_THRESHOLD_PER_HOUR = 2.0; // lit
        
        boolean fire = false;
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -48, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
            .user("user", account.getKey())
            .meter()
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        ArrayList<GroupDataSeries> series = queryResponse.getMeters();

        for (GroupDataSeries s: series) {
            if (!s.getPoints().isEmpty()) {
                boolean aboveThreshold = true;
                for (DataPoint p: s.getPoints()) {
                    if (((MeterDataPoint) p).getVolume().get(EnumMetric.SUM) < VOLUME_THRESHOLD_PER_HOUR) {
                        aboveThreshold = false;
                        break;
                    }
                }
                if (aboveThreshold) {
                    fire = true;
                    break;
                }
            }
        }
        
        Alert.Parameters parameters = new Alert.CommonParameters(
            refDate, EnumDeviceType.METER, EnumAlertType.WATER_LEAK);
        return new MessageResolutionStatus<>(fire, parameters);
    }

    // Alert #2 - Shower still on!
    public MessageResolutionStatus<Alert.Parameters> alertShowerStillOnAmphiro(
        AccountEntity account, DateTime refDate, ConsumptionStats stats) 
    {   
        final int DURATION_THRESHOLD_IN_MINUTES = 30;
        
        boolean fire = false;
        
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
            .user("user", account.getKey())
            .amphiro()
            .max();
        
        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();
        
        for (GroupDataSeries s: dataSeriesAmphiro) {
            if (!s.getPoints().isEmpty()) {
                for (DataPoint point : s.getPoints()) {
                    AmphiroDataPoint point1 = (AmphiroDataPoint) point;
                    if (point1.getDuration().get(EnumMetric.MAX) > DURATION_THRESHOLD_IN_MINUTES) {
                        fire = true;
                    }
                }
            }
        }
        
        Alert.Parameters parameters = new Alert.CommonParameters(
            refDate, EnumDeviceType.AMPHIRO, EnumAlertType.SHOWER_ON);
        return new MessageResolutionStatus<>(fire, parameters);
    }

    // Alert #5 - Water quality not assured!
    public IMessageResolutionStatus<Alert.Parameters> alertWaterQualitySWM(
        AccountEntity account, DateTime refDate, ConsumptionStats stats) 
    {        
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
            .user("user", account.getKey())
            .meter()
            .sum();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();
     
        boolean fire = false;
        for (GroupDataSeries s : dataSeriesMeter) {
            if (!s.getPoints().isEmpty()) {
                for (DataPoint p: s.getPoints()) {
                    if (((MeterDataPoint) p).getVolume().get(EnumMetric.SUM) == 0) {
                        fire = true;
                    }
                }
            } else {
                fire = true;
            }
            if (fire)
                break;
        }
        
        Alert.Parameters parameters = new Alert.CommonParameters(
            refDate, EnumDeviceType.METER, EnumAlertType.WATER_QUALITY);
        return new MessageResolutionStatus<>(fire, parameters);
    }

    // Alert #6 - Water too hot!
    public IMessageResolutionStatus<Alert.Parameters> alertHotTemperatureAmphiro(
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
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

        boolean fire = false;
        for (GroupDataSeries s: dataSeriesAmphiro) {
            if (!s.getPoints().isEmpty()) {
                for (DataPoint p: s.getPoints()) {
                    if (((AmphiroDataPoint) p).getTemperature().get(EnumMetric.MAX) > TEMPERATURE_THRESHOLD) {
                        fire = true;
                        break;
                    }
                }
            }
            if (fire)
                break;
        }
        
        Alert.Parameters parameters = new Alert.CommonParameters(
            refDate, EnumDeviceType.AMPHIRO, EnumAlertType.HOT_TEMPERATURE);
        return new MessageResolutionStatus<>(fire, parameters);
    }

    // Alert #7, #9 - Reached 80% of your daily water budget {integer1} {integer2}
    public IMessageResolutionStatus<Alert.Parameters> alertNearDailyBudget(
        MessageCalculationConfiguration config,
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
   
        Double consumed  = queryResponse.getSingleResult(
            deviceType, EnumDataField.VOLUME, EnumMetric.SUM); 
        if (consumed == null)
            return null;
        
        int budget = config.getDailyBudget(deviceType);
        Double remaining = (consumed > budget)? 0.0 : (budget - consumed);
        
        double percentUsed = 100 * (consumed / budget);
        
        Alert.Parameters parameters = new Alert.CommonParameters(
                refDate, deviceType, 
                (deviceType == EnumDeviceType.AMPHIRO?
                    EnumAlertType.NEAR_DAILY_SHOWER_BUDGET:
                    EnumAlertType.NEAR_DAILY_WATER_BUDGET)
            )
            .setInteger1(consumed.intValue())
            .setInteger2(remaining.intValue());
        
        return new MessageResolutionStatus<Alert.Parameters>(
            percentUsed > BUDGET_NEAR_PERCENTAGE_THRESHOLD, parameters);
    }
    
    // Alert #8, #10 - Reached 80% of your weekly water budget {integer1} {integer2}
    public IMessageResolutionStatus<Alert.Parameters> alertNearWeeklyBudget(
        MessageCalculationConfiguration config,
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

        Double consumed  = queryResponse.getSingleResult(
            deviceType, EnumDataField.VOLUME, EnumMetric.SUM);
        if (consumed == null)
            return null;
        
        int budget = config.getWeeklyBudget(deviceType);
        Double remaining = (consumed > budget)? 0.0 : (budget - consumed);     
        double percentUsed = 100 * (consumed / budget);
        
        Alert.Parameters parameters = new Alert.CommonParameters(
                refDate, deviceType, 
                (deviceType == EnumDeviceType.AMPHIRO?
                    EnumAlertType.NEAR_WEEKLY_SHOWER_BUDGET:
                    EnumAlertType.NEAR_WEEKLY_WATER_BUDGET)
            )
            .setInteger1(consumed.intValue())
            .setInteger2(remaining.intValue());
        
        return new MessageResolutionStatus<Alert.Parameters>(
            percentUsed > BUDGET_NEAR_PERCENTAGE_THRESHOLD, parameters);
    }

    // Alert #11, #12 - Reached daily Water Budget {integer1}
    public IMessageResolutionStatus<Alert.Parameters> alertReachedDailyBudget(
        MessageCalculationConfiguration config,
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

        Double consumed  = queryResponse.getSingleResult(
            deviceType, EnumDataField.VOLUME, EnumMetric.SUM);
        if (consumed == null)
            return null;
        
        int budget = config.getDailyBudget(deviceType);     
        double percentUsed = 100 * (consumed / budget);
        
        Alert.Parameters parameters = new Alert.CommonParameters(
                refDate, deviceType, 
                (deviceType == EnumDeviceType.AMPHIRO?
                    EnumAlertType.REACHED_DAILY_SHOWER_BUDGET:
                    EnumAlertType.REACHED_DAILY_WATER_BUDGET)
            )
            .setInteger1(Integer.valueOf(budget))
            .setInteger2(consumed.intValue());
        
        return new MessageResolutionStatus<Alert.Parameters>(
            percentUsed > BUDGET_PERCENTAGE_THRESHOLD, parameters);
    }
    
    // Alert #13, #14 - You are a real water champion!
    public IMessageResolutionStatus<Alert.Parameters> alertWaterChampion(
        MessageCalculationConfiguration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType) 
    {
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
            .user("user", account.getKey())
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();
        
        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        
        List<GroupDataSeries> series = queryResponse.getSeries(deviceType);
        if (series.isEmpty())
            return null;
        
        double dailyBudget = config.getDailyBudget(deviceType);      
        boolean fire = true;
        for (GroupDataSeries s: series) {
            List<Double> values = new ArrayList<>();
            for (DataPoint p: s.getPoints()) {
                double dailyConsumption;
                if (deviceType == EnumDeviceType.AMPHIRO) 
                    dailyConsumption = ((AmphiroDataPoint) p).getVolume().get(EnumMetric.SUM);
                else
                    dailyConsumption = ((MeterDataPoint) p).getVolume().get(EnumMetric.SUM);
                values.add(dailyConsumption);
                if (dailyConsumption > dailyBudget) {
                    fire = false;
                    break;
                }
            }
            fire = fire && (countConsecutiveZeros(values) < 10);
            if (!fire)
                break;
        }
        
        Alert.Parameters parameters = new Alert.CommonParameters(
            refDate, deviceType, 
            (deviceType == EnumDeviceType.AMPHIRO)? 
                EnumAlertType.SHOWER_CHAMPION:
                EnumAlertType.WATER_CHAMPION
        );
        return new MessageResolutionStatus<>(fire, parameters);
    }
    
    // Alert #15, #16 - You are using too much water {integer1}
    public IMessageResolutionStatus<Alert.Parameters> alertTooMuchWaterConsumption(
        MessageCalculationConfiguration config,
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

        Double consumed = queryResponse.getSingleResult(
            deviceType, EnumDataField.VOLUME, EnumMetric.SUM);
        if (consumed == null)
            return null;

        if (consumed > HIGH_CONSUMPTION_RATIO * weeklyAverage.getValue()) {
            // Get a rough estimate for annual savings if average behavior is adopted
            Double annualSavings = (consumed - weeklyAverage.getValue()) * WEEKS_PER_YEAR;
            Alert.Parameters parameters = new Alert.CommonParameters(
                    refDate, deviceType, 
                    (deviceType == EnumDeviceType.AMPHIRO)?
                        EnumAlertType.TOO_MUCH_WATER_AMPHIRO: 
                        EnumAlertType.TOO_MUCH_WATER_METER
                )
                .setInteger1(annualSavings.intValue())
                .setInteger2(consumed.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }
        
        return null;
    }

    // Alert #17 - You are spending too much energy for showering {integer1} {currency}
    public IMessageResolutionStatus<Alert.Parameters> alertTooMuchEnergyAmphiro(
        MessageCalculationConfiguration config, 
        ConsumptionStats stats, AccountEntity account, DateTime refDate) 
    {       
        final double HIGH_TEMPERATURE_THRESHOLD = 45.0;
        final double HIGH_TEMPERATURE_RATIO_OF_POINTS = 0.8; 
        
        Locale locale = Locale.forLanguageTag(account.getLocale());
        
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .sliding(refDate, -30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
            .user("user", account.getKey())
            .amphiro()
            .sum()
            .average();

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        
        List<GroupDataSeries> series = queryResponse.getDevices();
        if (series == null || series.isEmpty())
            return null;
        
        double monthlyConsumption = 0;
        int numPoints = 0, numPointsHigh = 0;
        for (GroupDataSeries s: series) {
            if (!s.getPoints().isEmpty()) {
                List<DataPoint> points = s.getPoints();
                numPoints += points.size();
                for (DataPoint p: points) {
                    AmphiroDataPoint ap = (AmphiroDataPoint) p;
                    monthlyConsumption += ap.getVolume().get(EnumMetric.SUM);
                    if (ap.getTemperature().get(EnumMetric.AVERAGE) > HIGH_TEMPERATURE_THRESHOLD)
                        numPointsHigh++;
                }
            }
        }
        
        double ratioHigh = ((double) numPointsHigh) / numPoints;
        if (ratioHigh > HIGH_TEMPERATURE_RATIO_OF_POINTS) {
            double pricePerKwh = priceData.getPricePerKwh(locale);
            Double annualSavings = 
                energyCalculator.computeEnergyToRiseTemperature(2, 12 * monthlyConsumption) * pricePerKwh;
            Alert.Parameters parameters = new Alert.CommonParameters(
                    refDate, EnumDeviceType.AMPHIRO, EnumAlertType.TOO_MUCH_ENERGY
                )
                .setCurrency1(annualSavings);
            return new MessageResolutionStatus<>(true, parameters);
        } 
        
        return null;
    }
    
    // Alert #18, #19 - You have greatly reduced your water use {integer1} percent
    public IMessageResolutionStatus<Alert.Parameters> alertReducedWaterUse(
        MessageCalculationConfiguration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        DataQuery query = null;
        DataQueryResponse queryResponse = null;
        
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
        
        Double c0 = queryResponse.getSingleResult(deviceType, VOLUME, EnumMetric.SUM);
        if (c0 == null)
            return null;
        
        query = queryBuilder
            .sliding(refDate, -7,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        Double c1 = queryResponse.getSingleResult(deviceType, VOLUME, EnumMetric.SUM);
        if (c1 == null)
            return null;
        
        Double percentDiff = 100 * ((c0 - c1) / c0);
        if (percentDiff > 10 && percentDiff < 60) {
            Alert.Parameters parameters = new Alert.CommonParameters(
                refDate, deviceType, 
                (deviceType == EnumDeviceType.AMPHIRO)?
                    EnumAlertType.REDUCED_WATER_USE_IN_SHOWER:
                    EnumAlertType.REDUCED_WATER_USE)
                .setInteger1(percentDiff.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        } 
        
        return null;
    }
    
    // Alert #20 - Congratulations! You are a water efficiency leader {integer1} litres
    public IMessageResolutionStatus<Alert.Parameters> alertWaterEfficiencyLeaderSWM(
        MessageCalculationConfiguration config,
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
        
        Double consumed = queryResponse.getSingleResult(METER, VOLUME, EnumMetric.SUM);
        if (consumed == null)
            return null;
        
        if (consumed < Math.min(monthlyThreshold.getValue(), monthlyAverage.getValue())) {
            int annualSavings = (int) (monthlyAverage.getValue() - consumed) * 12;
            Alert.Parameters parameters = new Alert.CommonParameters(
                    refDate, EnumDeviceType.METER, EnumAlertType.WATER_EFFICIENCY_LEADER
                )
                .setInteger1(annualSavings);
            return new MessageResolutionStatus<>(true, parameters);
        } 
        
        return null;
    }

    // Alert #21 - noop
    
    // Alert #22 - You are doing a great job!
    public IMessageResolutionStatus<Alert.Parameters> alertPromptGoodJobMonthlySWM(
        MessageCalculationConfiguration config,
        ConsumptionStats stats, AccountEntity account, DateTime refDate) 
    {
        ComputedNumber monthlyAverage = stats.get(EnumStatistic.AVERAGE_WEEKLY, METER, VOLUME);
        if (monthlyAverage == null || monthlyAverage.getValue() == null)
            return null;
        
        DataQuery query = null;
        DataQueryResponse queryResponse = null;
        
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .meter()
            .sum();
        
        query = queryBuilder
            .sliding(refDate, -30,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        Double c0 = queryResponse.getSingleResult(EnumDeviceType.METER, VOLUME, EnumMetric.SUM);
        if (c0 == null)
            return null;
        
        query = queryBuilder
            .sliding(refDate.minusDays(30), -30,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        Double c1 = queryResponse.getSingleResult(EnumDeviceType.METER, VOLUME, EnumMetric.SUM);
        if (c1 == null)
            return null;
        
        Double percentDiff = 100 * ((c1 - c0) / c1);
        if (percentDiff > 25 || (percentDiff > 6 && c0 < monthlyAverage.getValue())) {
            Alert.Parameters parameters = new Alert.CommonParameters(
                    refDate, EnumDeviceType.METER, EnumAlertType.GOOD_JOB_MONTHLY)
                .setInteger1(percentDiff.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }
        
        return null;
    }
    
    // Alert #23 - You have already saved {integer1} litres of water!
    public IMessageResolutionStatus<Alert.Parameters> alertLitresSavedSWM(
        MessageCalculationConfiguration config, AccountEntity account, DateTime refDate) 
    {
        final double VOLUME_WEEKLY_DIFF_THRESHOLD = 100; 
        
        DataQuery query = null;
        DataQueryResponse queryResponse = null;
        
        DataQueryBuilder queryBuilder = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())
            .meter()
            .sum();
        
        query = queryBuilder
            .sliding(refDate, -7,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        Double c0 = queryResponse.getSingleResult(EnumDeviceType.METER, VOLUME, EnumMetric.SUM);
        if (c0 == null)
            return null;
        
        query = queryBuilder
            .sliding(refDate.minusDays(7), -7,  EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
            .build();
        queryResponse = dataService.execute(query);
        Double c1 = queryResponse.getSingleResult(EnumDeviceType.METER, VOLUME, EnumMetric.SUM);
        if (c1 == null)
            return null;

        Double diff = c1 - c0;
        if (diff > VOLUME_WEEKLY_DIFF_THRESHOLD) {
            Alert.Parameters parameters = new Alert.CommonParameters(
                refDate, EnumDeviceType.METER, EnumAlertType.LITERS_ALREADY_SAVED)
            .setInteger1(diff.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }
        
        return null;
    }
    
    // Alert #24 - Congratulations! You are one of the top 25% savers in your region.
    public IMessageResolutionStatus<Alert.Parameters> alertTop25SaverWeeklySWM(
        MessageCalculationConfiguration config,
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
        Double c0 = queryResponse.getSingleResult(EnumDeviceType.METER, VOLUME, EnumMetric.SUM);
        if (c0 == null)
            return null;
        
        if (c0 < weeklyThreshold.getValue()) {
            Alert.Parameters parameters = new Alert.CommonParameters(
                refDate, EnumDeviceType.METER, EnumAlertType.TOP_25_PERCENT_OF_SAVERS
            );
            return new MessageResolutionStatus<>(true, parameters);
        } 
        return null;
    }

    // Alert #25 - Congratulations! You are among the top 10% group of savers in your region.
    public IMessageResolutionStatus<Alert.Parameters> alertTop10SaverWeeklySWM(
        MessageCalculationConfiguration config,
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
        Double c0 = queryResponse.getSingleResult(EnumDeviceType.METER, VOLUME, EnumMetric.SUM);
        if (c0 == null)
            return null;
        
        if (c0 < weeklyThreshold.getValue()) {
            Alert.Parameters parameters = new Alert.CommonParameters(
                refDate, EnumDeviceType.METER, EnumAlertType.TOP_10_PERCENT_OF_SAVERS
            );
            return new MessageResolutionStatus<>(true, parameters);
        }
        
        return null;
    }
 
    // Recommendation #1 - Spend 1 less minute in the shower and save {integer1} {integer2}
    public IMessageResolutionStatus<DynamicRecommendation.Parameters> recommendLessShowerTimeAmphiro(
        MessageCalculationConfiguration config,
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
        
        ArrayList<GroupDataSeries> series = queryResponse.getDevices();
        if (series == null || series.isEmpty())
            return null;

        Double quarterlyUserConsumption = queryResponse.getSingleResult(
            EnumDeviceType.AMPHIRO, VOLUME, EnumMetric.SUM);
        if (quarterlyUserConsumption == null)
            return null;
        Double monthlyUserAverageConsumption = quarterlyUserConsumption / 3;
        
        Double monthlyUserAverageDuration = queryResponse.getSingleResult(
            EnumDeviceType.AMPHIRO, DURATION, EnumMetric.AVERAGE
        );
        if (monthlyUserAverageDuration == null)
            return null;
        
        boolean fire = (
            (monthlyUserAverageDuration > monthlyAverageDuration.getValue() * 1.5) &&
            (monthlyUserAverageConsumption > monthlyAverageConsumption.getValue())
        );
        if (fire) {
            Double annualSavings = 
                (monthlyUserAverageConsumption - monthlyAverageConsumption.getValue()) * 12;
            DynamicRecommendation.Parameters parameters = new DynamicRecommendation.CommonParameters(
                    refDate, EnumDeviceType.AMPHIRO, 
                    EnumDynamicRecommendationType.LESS_SHOWER_TIME
                )
                .setInteger1(annualSavings.intValue())
                .setInteger2(Double.valueOf(annualSavings * 2.0).intValue());
            return new MessageResolutionStatus<>(true, parameters);
        } 
        
        return null;
    }
    
    // Recommendation #2 - You could save {currency1} euros if you used a bit less hot water in the shower. {currency2}
    public IMessageResolutionStatus<DynamicRecommendation.Parameters> recommendLowerTemperatureAmphiro(
        MessageCalculationConfiguration config,
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
        
        ArrayList<GroupDataSeries> series = queryResponse.getDevices();
        if (series == null || series.isEmpty())
            return null;

        Double quarterlyUserConsumption = queryResponse.getSingleResult(
            EnumDeviceType.AMPHIRO, VOLUME, EnumMetric.SUM);
        if (quarterlyUserConsumption == null)
            return null;
        Double monthlyUserAverageConsumption = quarterlyUserConsumption / 3;
        
        Double monthlyUserAverageTemperature = queryResponse.getSingleResult(
            EnumDeviceType.AMPHIRO, TEMPERATURE, EnumMetric.AVERAGE
        );
        if (monthlyUserAverageTemperature == null)
            return null;
        
        boolean fire = (monthlyUserAverageTemperature > monthlyAverageTemperature.getValue() * 1.0);
        if (fire) {
            double annualUserAverageConsumption = monthlyUserAverageConsumption * 12;
            Locale locale = Locale.forLanguageTag(account.getLocale());
            double pricePerKwh = priceData.getPricePerKwh(locale);
            Double annualSavings = 
                energyCalculator.computeEnergyToRiseTemperature(2, annualUserAverageConsumption) * pricePerKwh;
            DynamicRecommendation.Parameters parameters = new DynamicRecommendation.CommonParameters(
                    refDate, EnumDeviceType.AMPHIRO, 
                    EnumDynamicRecommendationType.LOWER_TEMPERATURE
                )
                .setCurrency1(annualSavings)
                .setCurrency2(annualSavings);
            return new MessageResolutionStatus<>(true, parameters);
        }
        
        return null;
    }

    // Recommendation #3 - Reduce the water flow in the shower and gain {integer1} {integer2}
    public IMessageResolutionStatus<DynamicRecommendation.Parameters> recommendLowerFlowAmphiro(
        MessageCalculationConfiguration config,
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
        
        ArrayList<GroupDataSeries> series = queryResponse.getDevices();
        if (series == null || series.isEmpty())
            return null;

        Double monthlyUserAverageFlow = queryResponse.getSingleResult(
            EnumDeviceType.AMPHIRO, FLOW, EnumMetric.AVERAGE);
        if (monthlyUserAverageFlow == null)
            return null;
        if (monthlyUserAverageFlow < monthlyAverageFlow.getValue())
            return null;
        
        Double quarterUserConsumption = queryResponse.getSingleResult(
            EnumDeviceType.AMPHIRO, VOLUME, EnumMetric.SUM);
        if (quarterUserConsumption == null)
            return null;
        
        Double annualUserConsumption = quarterUserConsumption * 4;
        Double annualAverageConsumption = monthlyAverageConsumption.getValue() * 12;
        if (annualUserConsumption > annualAverageConsumption) {
            Double annualSavings = annualUserConsumption - annualAverageConsumption;
            DynamicRecommendation.Parameters parameters = new DynamicRecommendation.CommonParameters(
                    refDate, EnumDeviceType.AMPHIRO, EnumDynamicRecommendationType.LOWER_FLOW
                )
                .setInteger1(annualSavings.intValue())
                .setInteger2(annualSavings.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }
 
        return null;
    }

    // Recommendation #4 - Change your shower head and save {integer1} {integer2}
    // Todo: This computation is identical to #3, maybe discard #4
    public IMessageResolutionStatus<DynamicRecommendation.Parameters> recommendShowerHeadChangeAmphiro(
        MessageCalculationConfiguration config,
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
        
        Double monthlyUserAverageFlow = queryResponse.getSingleResult(
            EnumDeviceType.AMPHIRO, FLOW, EnumMetric.AVERAGE);
        if (monthlyUserAverageFlow == null)
            return null;
        
        Double quarterUserConsumption = queryResponse.getSingleResult(
            EnumDeviceType.AMPHIRO, VOLUME, EnumMetric.SUM);
        if (quarterUserConsumption == null)
            return null;
        
        Double annualUserConsumption = quarterUserConsumption * 4;
        Double annualAverageConsumption = monthlyAverageConsumption.getValue() * 12;
        if (annualUserConsumption > annualAverageConsumption) {
            Double annualSavings = annualUserConsumption - annualAverageConsumption;
            DynamicRecommendation.Parameters parameters = new DynamicRecommendation.CommonParameters(
                    refDate, EnumDeviceType.AMPHIRO, EnumDynamicRecommendationType.CHANGE_SHOWERHEAD
                )
                .setInteger1(annualSavings.intValue())
                .setInteger2(annualSavings.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        }
        
        return null;
    }

    // Recommendation #5 - Have you considered changing your shampoo? {integer1} percent
    public IMessageResolutionStatus<DynamicRecommendation.Parameters> recommendShampooChangeAmphiro(
        MessageCalculationConfiguration config,
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
        
        Double quarterUserConsumption = queryResponse.getSingleResult(
            EnumDeviceType.AMPHIRO, VOLUME, EnumMetric.SUM);
        if (quarterUserConsumption == null)
            return null;
        double monthlyUserAverageConsumption = quarterUserConsumption / 3;

        if (monthlyUserAverageConsumption > monthlyAverageConsumption.getValue()) {
            // Compute percent of usage above others
            Double percentDiff = 100.0 * 
                ((monthlyUserAverageConsumption / monthlyAverageConsumption.getValue()) - 1.0);
            DynamicRecommendation.Parameters parameters = new DynamicRecommendation.CommonParameters(
                    refDate, EnumDeviceType.AMPHIRO, EnumDynamicRecommendationType.SHAMPOO_CHANGE
                )
                .setInteger1(percentDiff.intValue());
            return new MessageResolutionStatus<>(true, parameters);
        } 
        
        return null;
    }

    // Recommendation #6 - When showering, reduce the water flow when you do not need it {integer1} {integer2}
    public IMessageResolutionStatus<DynamicRecommendation.Parameters> recommendReduceFlowWhenNotNeededAmphiro(
        MessageCalculationConfiguration config,
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
        
        Double monthlyUserAveragePerSession = queryResponse.getSingleResult(
            EnumDeviceType.AMPHIRO, VOLUME, EnumMetric.AVERAGE);
        if (monthlyUserAveragePerSession == null)
            return null;
        
        // Todo - calculate the number of sessions per year when available
        // Todo - add additional check for flow adjustments during the session when available
        int numberOfSessionsPerYear = 100;
        if (monthlyUserAveragePerSession > monthlyAveragePerSession.getValue()) {
            // Compute liters more than average
            Double moreLitersThanOthersInYear = 
                (monthlyUserAveragePerSession - monthlyAveragePerSession.getValue()) * numberOfSessionsPerYear;
            DynamicRecommendation.Parameters parameters = new DynamicRecommendation.CommonParameters(
                    refDate, EnumDeviceType.AMPHIRO, EnumDynamicRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED
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
                     
    private MessageResolutionStatus<Insight.BasicParameters> computeInsightA1(
        MessageCalculationConfiguration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final double K = 1.28;  // a threshold (in units of standard deviation) of significant change
        final int N = 12;       // number of past weeks to examine
        final double F = 0.5;   // a threshold ratio of non-nulls for collected values
                
        // Build a common part of a data-service query
        
        DataQueryResponse qr;
        DataQueryBuilder qb = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())  
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();
        
        // Compute for target day
        
        qb.sliding(refDate, +1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL);
        qr = dataService.execute(qb.build());
        Double refValue = qr.getSingleResult(deviceType, VOLUME, EnumMetric.SUM);        
        if (refValue == null)
            return null; // nothing to compare to
        
        // Compute for past N weeks for a given day-of-week   
        
        DateTime start = refDate;
        List<Double> values = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            start = start.minusWeeks(1);
            qb.sliding(start, +1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL);
            qr = dataService.execute(qb.build());
            Double val = qr.getSingleResult(deviceType, VOLUME, EnumMetric.SUM);
            if (val != null)
                values.add(val);
        }    
        if (values.size() < N * F)
            return null; // too few values, the average is not reliable
        
        // Seems we have sufficient data for the past weeks
        
        double[] pvalues = ArrayUtils.toPrimitive(values.toArray(new Double[0]));
        double avgValue = mean(pvalues); 
        double sd = Math.sqrt(populationVariance(pvalues, avgValue));
        double normValue = (refValue - avgValue) / sd; // normalized value
        double score = Math.abs(normValue) / (2 * K);
        
        logger.debug(String.format(
            "Insight A1 for account %s/%s: Consumption for same week day of last %d weeks since %s:\n\t" + 
                "value=%.2f mean=%.2f stddevp=%.2f x*=%.2f score=%.2f", 
             account.getKey(), deviceType, N, refDate.toString("dd/MM/YYYY"), 
             refValue, avgValue, sd, normValue, score));
        
        return new MessageResolutionStatus<Insight.BasicParameters>(
            score, 
            Insight.newA1Parameters(refDate, deviceType, refValue, avgValue)
        );
    }
    
    private MessageResolutionStatus<Insight.BasicParameters> computeInsightA2(
        MessageCalculationConfiguration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType)
    {
        final double K = 1.28;  // a threshold (in units of standard deviation) of significant change
        final int N = 30;       // number of past days to examine
        final double F = 0.6;   // a threshold ratio of non-nulls for collected values
        
        // Build a common part of a data-service query      
        
        DataQueryResponse qr;
        DataQueryBuilder qb = new DataQueryBuilder()
            .timezone(refDate.getZone())
            .user("user", account.getKey())  
            .source(EnumMeasurementDataSource.fromDeviceType(deviceType))
            .sum();
        
        // Compute for target day
        
        qb.sliding(refDate, +1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL);
        qr = dataService.execute(qb.build());
        Double refValue = qr.getSingleResult(deviceType, VOLUME, EnumMetric.SUM);
        if (refValue == null)
            return null; // nothing to compare to
        
        // Compute for past N days
        
        DateTime start = refDate;
        List<Double> values = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            start = start.minusDays(1);
            qb.sliding(start, +1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL);
            qr = dataService.execute(qb.build());
            Double val = qr.getSingleResult(deviceType, VOLUME, EnumMetric.SUM);
            if (val != null)
                values.add(val);
        }   
        if (values.size() < N * F)
            return null; // too few values, the average is not reliable
        
        // Seems we have sufficient data for the past days
        
        double[] pvalues = ArrayUtils.toPrimitive(values.toArray(new Double[0]));
        double avgValue = mean(pvalues); 
        double sd = Math.sqrt(populationVariance(pvalues, avgValue));
        double normValue = (refValue - avgValue) / sd; // normalized value
        double score = Math.abs(normValue) / (2 * K);
        
        logger.debug(String.format(
            "Insight A2 for account %s/%s: Consumption for last %d days since %s:\n\t" + 
                "value=%.2f mean=%.2f stddevp=%.2f x*=%.2f score=%.2f", 
             account.getKey(), deviceType, N, refDate.toString("dd/MM/YYYY"), 
             refValue, avgValue, sd, normValue, score));
        
        return new MessageResolutionStatus<Insight.BasicParameters>(
            score,
            Insight.newA2Parameters(refDate, deviceType, refValue, avgValue)
        );
    }
    
    private MessageResolutionStatus<Insight.BasicParameters> computeInsightA3(
        MessageCalculationConfiguration config,
        AccountEntity account, DateTime refDate, EnumDeviceType deviceType, EnumPartOfDay partOfDay)
    {
        final double PERCENTAGE_CHANGE_THRESHOLD = 40;
        final double VOLUME_LOW_THRESHOLD = 15; // a lower threshold for volume (litres)
        final int N = 30;       // number of past days to examine
        final double F = 0.6;   // a threshold ratio of non-nulls for collected values
         
        // Build a common part of a data-service query      
        DataQuery query;
        DataQueryResponse queryResponse;
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
        Double refValue = queryResponse.getSingleResult(deviceType, VOLUME, EnumMetric.SUM);
        if (refValue == null || refValue < VOLUME_LOW_THRESHOLD)
            return null;
                
        // Compute for part-of-day for past N days
        
        DateTime start = refDate;
        List<Double> values = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            start = start.minusDays(1);
            Interval r1 = partOfDay.toInterval(start);
            query = queryBuilder
                .absolute(r1.getStart(), r1.getEnd(), EnumTimeAggregation.ALL)
                .build();
            queryResponse = dataService.execute(query);
            Double val = queryResponse.getSingleResult(deviceType, VOLUME, EnumMetric.SUM);
            if (val != null)
                values.add(val);
        }   
        if (values.size() < N * F)
            return null; // too few values, the average is not reliable
        
        // Seems we have sufficient data for the past days
        
        double[] pvalues = ArrayUtils.toPrimitive(values.toArray(new Double[0]));
        double avgValue = mean(pvalues); 
        double percentDiff = 100.0 * (refValue - avgValue) / avgValue;
        double score = Math.abs(percentDiff) / (2 * PERCENTAGE_CHANGE_THRESHOLD);
        
        logger.debug(String.format(
            "Insight A3 for account %s/%s: Consumption at %s of last %d days since %s:\n\t" + 
                "value=%.2f mean=%.2f score=%.2f", 
             account.getKey(), deviceType, partOfDay, N, refDate.toString("dd/MM/YYYY"),
             refValue, avgValue, score));
        
        return new MessageResolutionStatus<Insight.BasicParameters>(
            score,
            Insight.newA3Parameters(refDate, partOfDay, deviceType, refValue, avgValue)
        );
    }
    
    //
    // ~ Helpers
    //
    
    private DateTime getDateOfLastStaticRecommendation(UUID accountKey) 
    {
        AccountStaticRecommendationEntity e = accountStaticRecommendationRepository
            .findLastForAccount(accountKey);
        return (e == null)? null : e.getCreatedOn();
    }
    
    private int countConsecutiveZeros(List<Double> values) 
    {
        int maxLength = 0;
        int currLength = 0;
        for (Double value: values) {
            if (value == 0)
                currLength++;
            else
                currLength = 0;
            if (currLength > maxLength) {
                maxLength = currLength;
            }
        }
        return maxLength;
    }
}
