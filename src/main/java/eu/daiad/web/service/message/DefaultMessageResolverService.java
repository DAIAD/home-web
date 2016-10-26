package eu.daiad.web.service.message;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.message.ConsumptionStats;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.PendingMessageStatus;
import eu.daiad.web.model.query.AmphiroDataPoint;
import eu.daiad.web.model.query.DataPoint;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.EnumTimeAggregation;
import eu.daiad.web.model.query.EnumTimeUnit;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MeterDataPoint;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IMessageManagementRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
//import eu.daiad.web.service.weather.DailyWeatherData;
//import eu.daiad.web.service.weather.IWeatherRepository;

@Service()
public class DefaultMessageResolverService implements IMessageResolverService {

	@Autowired
	IDataService dataService;

	@Autowired
	IUserRepository userRepository;
        
    @Autowired
    IMessageManagementRepository messageManagementRepository;
        
    @Autowired 
    IDeviceRepository deviceRepository;
    
//    @Autowired
//    private IWeatherRepository weatherRepository;       
    
    private static final Float AMPHIRO_TEMPERATURE_THRESHOLD = 45f;
    private static final Integer AMPHIRO_DURATION_THRESHOLD_IN_MINUTES = 30;
    
    @Override
	public PendingMessageStatus resolve(
	        MessageCalculationConfiguration config, UtilityInfo utility, ConsumptionStats stats, UUID accountKey) 
    {      
		AuthenticatedUser account = this.userRepository.getUserByKey(accountKey);
		
		DateTimeZone tz = DateTimeZone.forID(utility.getTimezone());
		
		PendingMessageStatus status = new PendingMessageStatus();

        status.setMeterInstalled(
                this.isMeterInstalledForUser(accountKey));
                
        status.setAmphiroInstalled(
                this.isAmphiroInstalledForUser(accountKey));                
                
		status.setAlertWaterLeakSWM(
		        this.alertWaterLeakSWM(accountKey, tz));

		status.setAlertWaterQualitySWM(
		        this.alertWaterQualitySWM(accountKey, tz));

		status.setAlertNearDailyBudgetSWM(
		        this.alertNearDailyBudgetSWM(config, accountKey, tz));
        
		status.setAlertNearWeeklyBudgetSWM(
		        this.alertNearWeeklyBudgetSWM(config, accountKey, tz));

		status.setAlertReachedDailyBudgetSWM(
		        this.alertReachedDailyBudgetSWM(config, accountKey, tz));

		status.setAlertWaterChampionSWM(
		        this.alertWaterChampionSWM(config, accountKey, tz));

		status.setAlertTooMuchWaterConsumptionSWM(
		        this.alertTooMuchWaterConsumptionSWM(stats, accountKey, tz));

		status.setAlertReducedWaterUseSWM(
		        this.alertReducedWaterUseSWM(accountKey, account.getCreatedOn(), tz));

		status.setAlertWaterEfficiencyLeaderSWM(
		        this.alertWaterEfficiencyLeaderSWM(stats, accountKey, tz));

		status.setAlertPromptGoodJobMonthlySWM(
		        this.alertPromptGoodJobMonthlySWM(stats, accountKey, tz));

		status.setAlertLitresSavedSWM(
		        this.alertLitresSavedSWM(accountKey, tz));

		status.setAlertTop25SaverWeeklySWM(
		        this.alertTop25SaverWeeklySWM(stats, accountKey, tz));
		
		status.setAlertTop10SaverSWM(
		        this.alertTop10SaverSWM(stats, accountKey, tz));

		status.setAlertShowerStillOnAmphiro(
		        this.alertShowerStillOnAmphiro(stats, accountKey, tz));

		status.setAlertHotTemperatureAmphiro(
		        this.alertHotTemperatureAmphiro(stats, accountKey, tz));

		status.setAlertNearDailyBudgetAmphiro(
		        this.alertNearDailyBudgetAmphiro(config, accountKey, tz));
		
		status.setAlertNearWeeklyBudgetAmphiro(
		        this.alertNearWeeklyBudgetAmphiro(config, accountKey, tz));

		status.setAlertReachedDailyBudgetAmphiro(
		        this.alertReachedDailyBudgetAmphiro(config, accountKey, tz));

		status.setAlertShowerChampionAmphiro(
		        this.alertShowerChampionAmphiro(config, accountKey, tz));

		status.setAlertTooMuchWaterConsumptionAmphiro(
		        this.alertTooMuchWaterConsumptionAmphiro(stats, accountKey, tz));

		status.setAlertTooMuchEnergyAmphiro(
		        this.alertTooMuchEnergyAmphiro(stats, accountKey, tz));

		status.setAlertImprovedShowerEfficiencyAmphiro(
		        this.alertImprovedShowerEfficiencyAmphiro(accountKey, account.getCreatedOn(), tz));

		status.setRecommendLessShowerTimeAmphiro(
		        this.recommendLessShowerTimeAmphiro(stats, accountKey, tz));

		status.setRecommendLowerTemperatureAmphiro(
		        this.recommendLowerTemperatureAmphiro(stats, accountKey, tz));

		status.setRecommendLowerFlowAmphiro(
		        this.recommendLowerFlowAmphiro(stats, accountKey, tz));

		status.setRecommendShowerHeadChangeAmphiro(
		        this.recommendShowerHeadChangeAmphiro(stats, accountKey, tz));

		status.setRecommendShampooChangeAmphiro(
		        this.recommendShampooChangeAmphiro(stats, accountKey, tz));

		status.setRecommendReduceFlowWhenNotNeededAmphiro(
		        this.recommendReduceFlowWhenNotNeededAmphiro(stats, accountKey, tz));
                
        status.setInitialStaticTips(
                this.initialStaticTipsForAccount(account));
                
        status.setStaticTip(
                this.produceStaticTipForAccount(account, config.getStaticTipInterval()));
                
		return status;
	}
        
    //random three initial static tips
    private boolean initialStaticTipsForAccount(AuthenticatedUser user) {                        
        boolean initialStaticTips = false;            
        DateTime lastCreatedOn = messageManagementRepository.getLastDateOfAccountStaticRecommendation(user);

        if(lastCreatedOn == null ){
            initialStaticTips = true;
        }
            return initialStaticTips;
    }
        
    //random static tip
    private boolean produceStaticTipForAccount(AuthenticatedUser user, int staticTipInterval) {                        
        boolean produceStaticTip = false;            
        DateTime lastCreatedOn = messageManagementRepository.getLastDateOfAccountStaticRecommendation(user);

        if(lastCreatedOn == null || lastCreatedOn.isBefore(DateTime.now().minusDays(staticTipInterval))){
            produceStaticTip = true;
        }
        return produceStaticTip;
    }
        
	// 1 alert - Check for water leaks!
	private boolean alertWaterLeakSWM(UUID accountKey, DateTimeZone timezone) {
		boolean fireAlert = false;
		DataQueryBuilder queryBuilder = new DataQueryBuilder();
		queryBuilder.timezone(timezone).sliding(-48, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
						.user("user", accountKey).meter().sum();

		DataQuery query = queryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);
		ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

		for (GroupDataSeries serie : dataSeriesMeter) {
			if (!serie.getPoints().isEmpty()) {
				ArrayList<DataPoint> points = serie.getPoints();
				for (DataPoint point : points) {
					MeterDataPoint meterPoint = (MeterDataPoint) point;
					if (meterPoint.getVolume().get(EnumMetric.SUM) > 2) { //2 litres threshold per hour
						fireAlert = true;
					}
                    else{
                        return false;
                    }
				}
			}
		}
		return fireAlert;
	}

	// 2 alert - Shower still on!
	public boolean alertShowerStillOnAmphiro(ConsumptionStats aggregates, UUID accountKey,
					DateTimeZone timezone) 
	{
	    
	    boolean fireAlert = false;
		
		DataQueryBuilder queryBuilder = new DataQueryBuilder();
		queryBuilder.timezone(timezone).sliding(-24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
						.user("user", accountKey).amphiro().max();

		DataQuery query = queryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);
		ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

		for (GroupDataSeries serie : dataSeriesAmphiro) {
                        if (!serie.getPoints().isEmpty()) {
                                ArrayList<DataPoint> points = serie.getPoints();
                                for (DataPoint point : points) {
                                        AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) point;
                                        if (amphiroPoint.getDuration().get(EnumMetric.MAX) > AMPHIRO_DURATION_THRESHOLD_IN_MINUTES) {
                                                fireAlert = true;
                                        }
                                }
                        }
		}
		return fireAlert;
	}

	// 5 alert - Water quality not assured!
	public boolean alertWaterQualitySWM(UUID accountKey, DateTimeZone timezone) {
		boolean fireAlert = false;

        //check for outside temperature.
//        int utilityId = userRepository.getUserByKey(accountKey).getUtilityId();
//        List<DailyWeatherData> dailyWeatherData = 
//                weatherRepository.getDailyData(0, utilityId, DateTime.now(timezone).minusDays(1), DateTime.now(timezone));
//        
//        Double maxTemperature = dailyWeatherData.get(0).getMaxTemperature();
//        
//        if(maxTemperature < 27){
//            return false;
//        }
        
		DataQueryBuilder queryBuilder = new DataQueryBuilder();
		queryBuilder.timezone(timezone).sliding(-24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
						.user("user", accountKey).meter().sum();

		DataQuery query = queryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);
		ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

		for (GroupDataSeries serie : dataSeriesMeter) {
			if (!serie.getPoints().isEmpty()) { // check for non existent data
				ArrayList<DataPoint> points = serie.getPoints();
				for (DataPoint point : points) {
					MeterDataPoint meterPoint = (MeterDataPoint) point;
					if (meterPoint.getVolume().get(EnumMetric.SUM) == 0) {
						fireAlert = true;
					}
				}
			} else {
				fireAlert = true;
			}
		}
		return fireAlert;
	}

	// 6 alert - Water too hot!
	public boolean alertHotTemperatureAmphiro(ConsumptionStats aggregates, UUID accountKey,
					DateTimeZone timezone) 
	{
		boolean fireAlert = false;
		DataQueryBuilder queryBuilder = new DataQueryBuilder();
		queryBuilder.timezone(timezone).sliding(-24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
						.user("user", accountKey).amphiro().max();

		DataQuery query = queryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);
		ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

		for (GroupDataSeries serie : dataSeriesAmphiro) {
			if (!serie.getPoints().isEmpty()) { // check for non existent data
				ArrayList<DataPoint> points = serie.getPoints();
				for (DataPoint point : points) {
					AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) point;
					if (amphiroPoint.getTemperature().get(EnumMetric.MAX) > AMPHIRO_TEMPERATURE_THRESHOLD) {
						fireAlert = true;
					}
				}
			}
		}
		return fireAlert;
	}

	// 7 alert - Reached 80% of your daily water budget {integer1} {integer2}
	public SimpleEntry<Integer, Integer> alertNearDailyBudgetSWM(MessageCalculationConfiguration config,
					UUID accountKey, DateTimeZone timezone) {

		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).meter()
						.user("user", accountKey).sum();
		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);

        if(queryResponse.getMeters().isEmpty()){
            return null;
        }
                
		GroupDataSeries dataSeriesMeter = queryResponse.getMeters().get(0);
		ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
		if (dataPoints == null || dataPoints.isEmpty()) {
			return null;
		}
		DataPoint dataPoint = dataPoints.get(0);
		MeterDataPoint meterPoint = (MeterDataPoint) dataPoint;
		Double lastDaySum = meterPoint.getVolume().get(EnumMetric.SUM);

		double percentUsed = ((100 * lastDaySum) / config.getDailyBudget());
		if (percentUsed > 80) {
            
            Double remainingLitres;
            if(lastDaySum > config.getDailyBudget()){
                remainingLitres = 0.0;
            }
            else{
                remainingLitres = config.getDailyBudget() - lastDaySum;
            }
            
			return new SimpleEntry<>(lastDaySum.intValue(), remainingLitres.intValue());
		} else {
			return null;
		}
	}

	// 8 alert - Reached 80% of your weekly water budget {integer1} {integer2}
	public SimpleEntry<Integer, Integer> alertNearWeeklyBudgetSWM(MessageCalculationConfiguration config,
					UUID accountKey, DateTimeZone timezone) {

		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).meter()
						.user("user", accountKey).sum();
		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);

        if(queryResponse.getMeters().isEmpty()){
            return null;
        }
                                
		GroupDataSeries dataSeriesMeter = queryResponse.getMeters().get(0);
		ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
		if (dataPoints == null || dataPoints.isEmpty()) {
			return null;
		}
		DataPoint dataPoint = dataPoints.get(0);
		MeterDataPoint meterPoint = (MeterDataPoint) dataPoint;
		Double lastWeekSum = meterPoint.getVolume().get(EnumMetric.SUM);

		double percentUsed = (( 100 * lastWeekSum) / config.getWeeklyBudget());   
        
        Double remainingLitres;
        if(lastWeekSum > config.getWeeklyBudget()){
            remainingLitres = 0.0;
        }
        else{
            remainingLitres = config.getWeeklyBudget() - lastWeekSum;
        }
        
		if (percentUsed > 80) {
			return new SimpleEntry<>(lastWeekSum.intValue(), remainingLitres.intValue());
		} else {
			return null;
		}
	}

	// 9 alert - Reached 80% of your daily shower budget {integer1} {integer2}
	public SimpleEntry<Integer, Integer> alertNearDailyBudgetAmphiro(MessageCalculationConfiguration config,
					UUID accountKey, DateTimeZone timezone) {

		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro()
						.user("user", accountKey).sum();
		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);

        if(queryResponse.getDevices().isEmpty()){
            return null;
        }
                
		GroupDataSeries dataSeriesAmphiro = queryResponse.getDevices().get(0);
		ArrayList<DataPoint> dataPoints = dataSeriesAmphiro.getPoints();
		if (dataPoints == null || dataPoints.isEmpty()) {
			return null;
		}
		DataPoint dataPoint = dataPoints.get(0);
		AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) dataPoint;
		Double lastDaySum = amphiroPoint.getVolume().get(EnumMetric.SUM);

		double percentUsed = (100 * lastDaySum) / config.getDailyBudgetAmphiro();

        Double remainingLitres;
        if(lastDaySum > config.getDailyBudgetAmphiro()){
            remainingLitres = 0.0;
        }
        else{
            remainingLitres = config.getDailyBudgetAmphiro() - lastDaySum;
        }
        
		if (percentUsed > 80) {
			return new SimpleEntry<>(lastDaySum.intValue(), remainingLitres.intValue());
		} else {
			return null;
		}
	}

	// 10 alert - Reached 80% of your weekly shower budget {integer1} {integer2}
	public SimpleEntry<Integer, Integer> alertNearWeeklyBudgetAmphiro(MessageCalculationConfiguration config,
					UUID accountKey, DateTimeZone timezone) {

		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro()
						.user("user", accountKey).sum();
		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);

        if(queryResponse.getDevices().isEmpty()){
            return null;
        }
                
		GroupDataSeries dataSeriesAmphiro = queryResponse.getDevices().get(0);
		ArrayList<DataPoint> dataPoints = dataSeriesAmphiro.getPoints();
		if (dataPoints == null || dataPoints.isEmpty()) {
			return null;
		}
		DataPoint dataPoint = dataPoints.get(0);
		AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) dataPoint;
		Double lastWeekSum = amphiroPoint.getVolume().get(EnumMetric.SUM);

		double percentUsed = (100 * lastWeekSum) / config.getWeeklyBudgetAmphiro();

        Double remainingLitres;
        if(lastWeekSum > config.getWeeklyBudgetAmphiro()){
            remainingLitres = 0.0;
        }
        else{
            remainingLitres = config.getWeeklyBudgetAmphiro() - lastWeekSum;
        }
        
		if (percentUsed > 80) {
			return new SimpleEntry<>(lastWeekSum.intValue(), remainingLitres.intValue());
		} else {
			return null;
		}
	}

	// 11 alert - Reached daily Water Budget {integer1}
	public Entry<Boolean, Integer> alertReachedDailyBudgetSWM(MessageCalculationConfiguration config, UUID accountKey,
					DateTimeZone timezone) {

		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).meter()
						.user("user", accountKey).sum();
		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);

        if(queryResponse.getMeters().isEmpty()){
            return null;
        }
                
		GroupDataSeries dataSeriesMeter = queryResponse.getMeters().get(0);
		ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
		if (dataPoints == null || dataPoints.isEmpty()) {
			return new SimpleEntry<>(false, config.getDailyBudget());
		}
		DataPoint dataPoint = dataPoints.get(0);
		MeterDataPoint meterPoint = (MeterDataPoint) dataPoint;
		Double lastDaySum = meterPoint.getVolume().get(EnumMetric.SUM);
		SimpleEntry<Boolean, Integer> entry;
		if (lastDaySum > config.getDailyBudget() * 1.2) {
			entry = new SimpleEntry<>(true, config.getDailyBudget());
		} else {
			entry = null;
		}
		return entry;
	}

	// 12 alert - Reached daily Shower Budget {integer1}
	public Entry<Boolean, Integer> alertReachedDailyBudgetAmphiro(MessageCalculationConfiguration config,
					UUID accountKey, DateTimeZone timezone) {

		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro()
						.user("user", accountKey).sum();
		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);

        if(queryResponse.getDevices().isEmpty()){
            return null;
        }
                
		GroupDataSeries dataSeriesAmphiro = queryResponse.getDevices().get(0);
		ArrayList<DataPoint> dataPoints = dataSeriesAmphiro.getPoints();
		if (dataPoints == null || dataPoints.isEmpty()) {
			return new SimpleEntry<>(false, config.getDailyBudgetAmphiro());
		}
		DataPoint dataPoint = dataPoints.get(0);
		AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) dataPoint;
		Double lastDaySum = amphiroPoint.getVolume().get(EnumMetric.SUM);

		if (lastDaySum > config.getDailyBudgetAmphiro() * 1.2) {
			return new SimpleEntry<>(true, config.getDailyBudgetAmphiro());
		} else {
			return new SimpleEntry<>(false, config.getDailyBudgetAmphiro());
		}
	}

	// 13 alert - You are a real water champion!
	public boolean alertWaterChampionSWM(MessageCalculationConfiguration config, UUID accountKey, DateTimeZone timezone) {

		boolean fireAlert = true;
		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY).meter()
						.user("user", accountKey).sum();
		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);

                if(queryResponse.getMeters().isEmpty()){
                        return false;
                }
                
		GroupDataSeries dataSeriesMeter = queryResponse.getMeters().get(0);
		ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
		if (dataPoints == null || dataPoints.isEmpty()) {
			return false;
		}

		List<Double> values = new ArrayList<>();
		for (DataPoint point : dataPoints) {
			MeterDataPoint meterPoint = (MeterDataPoint) point;
			Double daySum = meterPoint.getVolume().get(EnumMetric.SUM);
			values.add(daySum);
			if (daySum > config.getDailyBudget()) {
				fireAlert = false;
			}
		}

		if (computeConsecutiveZeroConsumptions(values) > 10) {
			fireAlert = false;
		}

		return fireAlert;
	}

	// 14 alert - You are a real shower champion!
	public boolean alertShowerChampionAmphiro(MessageCalculationConfiguration config, UUID accountKey,
					DateTimeZone timezone) {

		boolean fireAlert = true;
		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY).amphiro()
						.user("user", accountKey).sum();
		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);

                if(queryResponse.getDevices().isEmpty()){
                        return false;
                }
                
		GroupDataSeries dataSeriesAmphiro = queryResponse.getDevices().get(0);
		ArrayList<DataPoint> dataPoints = dataSeriesAmphiro.getPoints();
		if (dataPoints == null || dataPoints.isEmpty()) {
			return false;
		}

		List<Double> values = new ArrayList<>();
		for (DataPoint point : dataPoints) {
			AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) point;
			Double daySum = amphiroPoint.getVolume().get(EnumMetric.SUM);
			values.add(daySum);
			if (daySum > config.getDailyBudgetAmphiro()) {
				fireAlert = false;
			}
		}

		if (computeConsecutiveZeroConsumptions(values) > 10) {
			fireAlert = false;
		}

		return fireAlert;
	}

	// 15 alert - You are using too much water {integer1}
	public SimpleEntry<Boolean, Double> alertTooMuchWaterConsumptionSWM(ConsumptionStats aggregates,
					UUID accountKey, DateTimeZone timezone) {

		if (aggregates.getAverageWeeklySWM().getValue() == null) {
			return new SimpleEntry<>(false, null);
		}

		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).meter()
						.user("user", accountKey).sum();
		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);

        if(queryResponse.getMeters().isEmpty()){
            return new SimpleEntry<>(false, null);
        }                
                
		GroupDataSeries dataSeriesMeter = queryResponse.getMeters().get(0);
		ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
		if (dataPoints == null || dataPoints.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}
		DataPoint dataPoint = dataPoints.get(0);
		MeterDataPoint meterPoint = (MeterDataPoint) dataPoint;
		Double lastWeekSum = meterPoint.getVolume().get(EnumMetric.SUM);

		if (lastWeekSum > 2 * aggregates.getAverageWeeklySWM().getValue()) {
			// return annual savings if average behavior is adopted. Multiply
			// with 52 weeks for annual value (liters).
			return new SimpleEntry<>(true, (lastWeekSum - aggregates.getAverageWeeklySWM().getValue()) * 52);
		} else {
			return new SimpleEntry<>(false, null);
		}
	}

	// 16 alert - You are using too much water in the shower {integer1}
	public SimpleEntry<Boolean, Double> alertTooMuchWaterConsumptionAmphiro(ConsumptionStats aggregates,
					UUID accountKey, DateTimeZone timezone) {

		if (aggregates.getAverageWeeklyAmphiro().getValue() == null) {
			return new SimpleEntry<>(false, null);
		}
		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro()
						.user("user", accountKey).sum();
		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);

        if(queryResponse.getDevices().isEmpty()){
            return new SimpleEntry<>(false, null);
        }                
                                
		GroupDataSeries dataSeriesAmphiro = queryResponse.getDevices().get(0);
		ArrayList<DataPoint> dataPoints = dataSeriesAmphiro.getPoints();
		if (dataPoints == null || dataPoints.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}
		DataPoint dataPoint = dataPoints.get(0);
		AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) dataPoint;
		Double lastWeekSum = amphiroPoint.getVolume().get(EnumMetric.SUM);
		SimpleEntry<Boolean, Double> entry;

		if (lastWeekSum > 2 * aggregates.getAverageWeeklyAmphiro().getValue()) {

			// return annual savings if average behavior is adopted. Multiply
			// with 52 weeks for annual value.
			entry = new SimpleEntry<>(true, (lastWeekSum - aggregates.getAverageWeeklyAmphiro().getValue()) * 52);
		} else {
			entry = new SimpleEntry<>(false, null);
		}
		return entry;
	}

	// TODO : Fix bug - returning false positive with 0 energy consumption

	// 17 alert - You are spending too much energy for showering {integer1} {currency}
	public SimpleEntry<Boolean, Double> alertTooMuchEnergyAmphiro(ConsumptionStats aggregates,
					UUID accountKey, DateTimeZone timezone) 
	{	    
	    boolean fireAlert = true;
		double monthlyShowerConsumption = 0;
		
		DataQueryBuilder queryBuilder = new DataQueryBuilder();
		queryBuilder.timezone(timezone).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
						.user("user", accountKey).amphiro().sum().average();

		DataQuery query = queryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(query);
		ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();
		if (dataSeriesAmphiro == null) {
			return new SimpleEntry<>(false, null);
		}

        if(dataSeriesAmphiro.get(0).getPoints().isEmpty()){
            return new SimpleEntry<>(false, null);            
        }
        
		for (GroupDataSeries serie : dataSeriesAmphiro) {
			if (!serie.getPoints().isEmpty()) { // check for non existent data
				ArrayList<DataPoint> points = serie.getPoints();
				for (DataPoint point : points) {

					AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) point;
					monthlyShowerConsumption = monthlyShowerConsumption + amphiroPoint.getVolume().get(EnumMetric.SUM);

					if (amphiroPoint.getTemperature().get(EnumMetric.AVERAGE) > AMPHIRO_TEMPERATURE_THRESHOLD) {
						fireAlert = true && fireAlert;
					} else {
						fireAlert = false; // if one average temp is below
											// threshold don't alert
					}
				}
			}
		}

		return new SimpleEntry<>(fireAlert, monthlyShowerConsumption * 12);
	}

	// 18 alert - Well done! You have greatly reduced your water use {integer1}
	// percent
	public SimpleEntry<Boolean, Integer> alertReducedWaterUseSWM(UUID accountKey, DateTime startingWeek,
					DateTimeZone timezone) {

        boolean fireAlert = false;
		DataQueryBuilder firstWeekDataQueryBuilder = new DataQueryBuilder();
		firstWeekDataQueryBuilder.timezone(timezone)
						.absolute(startingWeek, startingWeek.plusDays(7), EnumTimeAggregation.ALL).meter()
						.user("user", accountKey).sum();
		DataQuery firstWeekQuery = firstWeekDataQueryBuilder.build();
		DataQueryResponse firstWeekQueryResponse = dataService.execute(firstWeekQuery);
        if(firstWeekQueryResponse.getMeters().isEmpty()){
            return new SimpleEntry<>(false, null);
        }
		GroupDataSeries firstWeekDataSeriesMeter = firstWeekQueryResponse.getMeters().get(0);
		ArrayList<DataPoint> firstWeekDataPoints = firstWeekDataSeriesMeter.getPoints();
		if (firstWeekDataPoints == null || firstWeekDataPoints.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}
		DataPoint firstWeekDataPoint = firstWeekDataPoints.get(0);
		MeterDataPoint firstWeekMeterDataPoint = (MeterDataPoint) firstWeekDataPoint;
		Double firstWeekSum = firstWeekMeterDataPoint.getVolume().get(EnumMetric.SUM);

		DataQueryBuilder lastWeekDataQueryBuilder = new DataQueryBuilder();
		lastWeekDataQueryBuilder.timezone(timezone).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).meter()
						.user("user", accountKey).sum();
		DataQuery lastWeekQuery = lastWeekDataQueryBuilder.build();
		DataQueryResponse lastWeekQueryResponse = dataService.execute(lastWeekQuery);
        if(lastWeekQueryResponse.getMeters().isEmpty()){
            return new SimpleEntry<>(false, null);
        }
		GroupDataSeries lastWeekDataSeriesMeter = lastWeekQueryResponse.getMeters().get(0);
		ArrayList<DataPoint> lastWeekDataPoints = lastWeekDataSeriesMeter.getPoints();

		if (lastWeekDataPoints == null || lastWeekDataPoints.isEmpty()) {
            
			return new SimpleEntry<>(false, null);
		}
        
		List<Double> values = new ArrayList<>();
		for (DataPoint point : lastWeekDataPoints) {
			MeterDataPoint meterPoint = (MeterDataPoint) point;
			Double daySum = meterPoint.getVolume().get(EnumMetric.SUM);
			values.add(daySum);
		}
        
		if (computeConsecutiveZeroConsumptions(values) > 3) {
			return new SimpleEntry<>(false, null);
		} 
        
		DataPoint lastWeekDataPoint = lastWeekDataPoints.get(0);
		MeterDataPoint lastWeekMeterDataPoint = (MeterDataPoint) lastWeekDataPoint;
		Double lastWeekSum = lastWeekMeterDataPoint.getVolume().get(EnumMetric.SUM);

		SimpleEntry<Boolean, Integer> entry;
		Double percentDifference = 100 - ((lastWeekSum * 100) / firstWeekSum);

		if (percentDifference > 10 && percentDifference < 60) {
			entry = new SimpleEntry<>(true, percentDifference.intValue());
		} else {
			entry = new SimpleEntry<>(false, percentDifference.intValue());
		}
		return entry;
	}

	// 19 alert - Well done! You have greatly improved your shower efficiency
	// {integer1} percent
	public SimpleEntry<Boolean, Integer> alertImprovedShowerEfficiencyAmphiro(UUID accountKey, DateTime startingWeek,
					DateTimeZone timezone) {

		DataQueryBuilder firstWeekDataQueryBuilder = new DataQueryBuilder();
		firstWeekDataQueryBuilder.timezone(timezone)
						.absolute(startingWeek, startingWeek.plusDays(7), EnumTimeAggregation.ALL).amphiro()
						.user("user", accountKey).sum();
		DataQuery firstWeekQuery = firstWeekDataQueryBuilder.build();
		DataQueryResponse firstWeekQueryResponse = dataService.execute(firstWeekQuery);
                
        if(firstWeekQueryResponse.getDevices().isEmpty()){
            return new SimpleEntry<>(false, null);
        }
                
		GroupDataSeries firstWeekDataSeriesMeter = firstWeekQueryResponse.getDevices().get(0);
		ArrayList<DataPoint> firstWeekDataPoints = firstWeekDataSeriesMeter.getPoints();

		if (firstWeekDataPoints == null || firstWeekDataPoints.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}

		DataPoint firstWeekDataPoint = firstWeekDataPoints.get(0);
		AmphiroDataPoint firstWeekAmphiroPoint = (AmphiroDataPoint) firstWeekDataPoint;
		Double firstWeekSum = firstWeekAmphiroPoint.getVolume().get(EnumMetric.SUM);

		DataQueryBuilder lastWeekDataQueryBuilder = new DataQueryBuilder();
		lastWeekDataQueryBuilder.timezone(timezone).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro()
						.user("user", accountKey).sum();
		DataQuery lastWeekQuery = lastWeekDataQueryBuilder.build();
		DataQueryResponse lastWeekQueryResponse = dataService.execute(lastWeekQuery);
                
        if(lastWeekQueryResponse.getDevices().isEmpty()){
            return new SimpleEntry<>(false, null);
        }
                
		GroupDataSeries lastWeekDataSeriesMeter = lastWeekQueryResponse.getDevices().get(0);
		ArrayList<DataPoint> lastWeekDataPoints = lastWeekDataSeriesMeter.getPoints();

		if (lastWeekDataPoints == null || lastWeekDataPoints.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}

		DataPoint lastWeekDataPoint = lastWeekDataPoints.get(0);
		AmphiroDataPoint lastWeekAmphiroPoint = (AmphiroDataPoint) lastWeekDataPoint;
		Double lastWeekSum = lastWeekAmphiroPoint.getVolume().get(EnumMetric.SUM);
        
		List<Double> values = new ArrayList<>();
		for (DataPoint point : lastWeekDataPoints) {
			AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) point;
			Double daySum = amphiroPoint.getVolume().get(EnumMetric.SUM);
			values.add(daySum);
		}
        
		if (computeConsecutiveZeroConsumptions(values) > 3) {
			return new SimpleEntry<>(false, null);
		} 
        
		SimpleEntry<Boolean, Integer> entry;
		Double percentDifference = 100 - ((lastWeekSum * 100) / firstWeekSum);

		if (percentDifference >= 10 && percentDifference < 60) {
			entry = new SimpleEntry<>(true, percentDifference.intValue());
		} else {
			entry = new SimpleEntry<>(false, percentDifference.intValue());
		}
		return entry;
	}

	// 20 alert - Congratulations! You are a water efficiency leader {integer1}
	// litres
	public SimpleEntry<Boolean, Integer> alertWaterEfficiencyLeaderSWM(ConsumptionStats aggregates,
					UUID accountKey, DateTimeZone timezone) {

		if (aggregates.getTop10BaseMonthSWM().getValue() == null || aggregates.getAverageMonthlySWM().getValue() == null) {

			return new SimpleEntry<>(false, null);
		}

		Double baseTop10Consumption = aggregates.getTop10BaseMonthSWM().getValue();
		Double averageMonthlyAllUsers = aggregates.getAverageMonthlySWM().getValue();

		DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
		dataQueryBuilder.timezone(timezone).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
						.user("user", accountKey).meter().sum();

		DataQuery query = dataQueryBuilder.build();
		DataQueryResponse result = dataService.execute(query);
                
                if(result.getMeters().isEmpty()){
                    return new SimpleEntry<>(false, null);
                }
                
		GroupDataSeries meter = result.getMeters().get(0);
		ArrayList<DataPoint> dataPoints = meter.getPoints();

		if (dataPoints == null || dataPoints.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}

		DataPoint dataPoint = dataPoints.get(0);
		MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
		Map<EnumMetric, Double> m = meterDataPoint.getVolume();
		Double currentMonthAverage = (m.get(EnumMetric.SUM)) / 30;

		SimpleEntry<Boolean, Integer> entry;
		if (currentMonthAverage < baseTop10Consumption) {
			int litersSavedInYear = (int) (averageMonthlyAllUsers - currentMonthAverage) * 12;
			entry = new SimpleEntry<>(true, litersSavedInYear);
		} else {
			entry = new SimpleEntry<>(false, null);
		}

		return entry;
	}

	// 21 alert does not need a computation here.

	// 22 alert - You are doing a great job!
	public boolean alertPromptGoodJobMonthlySWM(ConsumptionStats aggregates, UUID accountKey,
					DateTimeZone timezone) {
		if (aggregates.getAverageMonthlySWM() == null) {
			return false;
		}

		boolean fireAlert;
		Double currentMonthConsumptionSWM = null;
		Double previousMonthConsumptionSWM = null;
		DataQueryBuilder currentMonthQueryBuilder = new DataQueryBuilder();
		currentMonthQueryBuilder.timezone(timezone).sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
						.user("user", accountKey).meter().sum();

		DataQuery currentMonthQuery = currentMonthQueryBuilder.build();
		DataQueryResponse currentMonthQueryResponse = dataService.execute(currentMonthQuery);
		ArrayList<GroupDataSeries> currentMonthDataSeriesMeter = currentMonthQueryResponse.getMeters();

		for (GroupDataSeries serie : currentMonthDataSeriesMeter) {
			if (!serie.getPoints().isEmpty()) {
				ArrayList<DataPoint> points = serie.getPoints();
				DataPoint dataPoint = points.get(0);
				MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
				Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
				currentMonthConsumptionSWM = metricsMap.get(EnumMetric.SUM);
			}
		}

		DataQueryBuilder previousMonthQueryBuilder = new DataQueryBuilder();
		previousMonthQueryBuilder.timezone(timezone)
						.absolute(DateTime.now().minusDays(60), DateTime.now().minusDays(30), EnumTimeAggregation.ALL)
						.user("user", accountKey).meter().sum();

		DataQuery previousMonthQuery = previousMonthQueryBuilder.build();
		DataQueryResponse previousMonthQueryResponse = dataService.execute(previousMonthQuery);
		ArrayList<GroupDataSeries> previousMonthDataSeriesMeter = previousMonthQueryResponse.getMeters();

		for (GroupDataSeries serie : previousMonthDataSeriesMeter) {
			if (!serie.getPoints().isEmpty()) {
				ArrayList<DataPoint> points = serie.getPoints();
				DataPoint dataPoint = points.get(0);
				MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
				Map<EnumMetric, Double> ma = meterDataPoint.getVolume();
				previousMonthConsumptionSWM = ma.get(EnumMetric.SUM);
			}
		}

		if ((currentMonthConsumptionSWM != null) && (previousMonthConsumptionSWM != null)
						&& (currentMonthConsumptionSWM < previousMonthConsumptionSWM)) {
			double percentDifferenceFromPreviousMonth = 100 - (currentMonthConsumptionSWM * 100)
							/ previousMonthConsumptionSWM;

			if (percentDifferenceFromPreviousMonth > 25) {
				fireAlert = true;
			} else {

				fireAlert = percentDifferenceFromPreviousMonth > 6
								&& currentMonthConsumptionSWM < aggregates.getAverageMonthlySWM().getValue();
			}
		} else {
			fireAlert = false;
		}

		return fireAlert;
	}

	// 23 alert - You have already saved {integer1} litres of water!
	public SimpleEntry<Boolean, Integer> alertLitresSavedSWM(UUID accountKey, DateTimeZone timezone) {
		Double currentWeekConsumptionSWM = null;
		Double previousWeekConsumptionSWM = null;
		DataQueryBuilder currentWeekQueryBuilder = new DataQueryBuilder();
		currentWeekQueryBuilder.timezone(timezone).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
						.user("user", accountKey).meter().sum();

		DataQuery currentWeekQuery = currentWeekQueryBuilder.build();
		DataQueryResponse currentWeekQueryResponse = dataService.execute(currentWeekQuery);
		ArrayList<GroupDataSeries> currentWeekDataSeriesMeter = currentWeekQueryResponse.getMeters();

		for (GroupDataSeries serie : currentWeekDataSeriesMeter) {
			if (!serie.getPoints().isEmpty()) {
				ArrayList<DataPoint> points = serie.getPoints();
				DataPoint dataPoint = points.get(0);
				MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
				Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
				currentWeekConsumptionSWM = metricsMap.get(EnumMetric.SUM);
			}
		}

		DataQueryBuilder previousWeekQueryBuilder = new DataQueryBuilder();
		previousWeekQueryBuilder.timezone(timezone)
						.absolute(DateTime.now().minusDays(14), DateTime.now().minusDays(7), EnumTimeAggregation.ALL)
						.user("user", accountKey).meter().sum();

		DataQuery previousWeekQuery = previousWeekQueryBuilder.build();
		DataQueryResponse previousWeekQueryResponse = dataService.execute(previousWeekQuery);
		ArrayList<GroupDataSeries> previousWeekDataSeriesMeter = previousWeekQueryResponse.getMeters();

		for (GroupDataSeries serie : previousWeekDataSeriesMeter) {
			if (!serie.getPoints().isEmpty()) {
				ArrayList<DataPoint> points = serie.getPoints();
				DataPoint dataPoint = points.get(0);
				MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
				Map<EnumMetric, Double> ma = meterDataPoint.getVolume();
				previousWeekConsumptionSWM = ma.get(EnumMetric.SUM);
			}
		}

		if ((previousWeekConsumptionSWM != null) && (currentWeekConsumptionSWM != null)) {
			if (previousWeekConsumptionSWM - currentWeekConsumptionSWM > 100) {
				Double litresSavedThisWeek = previousWeekConsumptionSWM - currentWeekConsumptionSWM;
				return new SimpleEntry<>(true, litresSavedThisWeek.intValue());
			} else {
				return new SimpleEntry<>(false, null);
			}
		}

		return new SimpleEntry<>(false, null);
	}

	// 24 alert - Congratulations! You are one of the top 25% savers in your
	// region.
	public boolean alertTop25SaverWeeklySWM(ConsumptionStats aggregates, UUID accountKey,
					DateTimeZone timezone) {

		if (aggregates.getTop25BaseWeekSWM() == null || aggregates.getTop25BaseWeekSWM().getValue() == null) {
			return false;
		}

		Double currentWeekConsumptionSWM = null;
		DataQueryBuilder currentWeekQueryBuilder = new DataQueryBuilder();
		currentWeekQueryBuilder.timezone(timezone).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
						.user("user", accountKey).meter().sum();

		DataQuery currentWeekQuery = currentWeekQueryBuilder.build();
		DataQueryResponse currentWeekQueryResponse = dataService.execute(currentWeekQuery);
		ArrayList<GroupDataSeries> currentWeekDataSeriesMeter = currentWeekQueryResponse.getMeters();
        
		if (currentWeekDataSeriesMeter == null || currentWeekDataSeriesMeter.isEmpty()) {
			return false;
		}
        
		for (GroupDataSeries serie : currentWeekDataSeriesMeter) {
			if (!serie.getPoints().isEmpty()) {
				ArrayList<DataPoint> points = serie.getPoints();
				DataPoint dataPoint = points.get(0);
				MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
				Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
				currentWeekConsumptionSWM = (metricsMap.get(EnumMetric.SUM)) / 7;
			}
		}

        if(currentWeekConsumptionSWM == null){
            return false;
        }
        
		return currentWeekConsumptionSWM < aggregates.getTop25BaseWeekSWM().getValue();
	}

	// 25 alert - Congratulations! You are among the top group of savers in your
	// city.
	public boolean alertTop10SaverSWM(ConsumptionStats aggregates, UUID accountKey, DateTimeZone timezone) {

		if (aggregates.getTop10BaseWeekSWM().getValue() == null) {
			return false;
		}

		Double currentWeekConsumptionSWM = null;
		DataQueryBuilder currentWeekQueryBuilder = new DataQueryBuilder();
		currentWeekQueryBuilder.timezone(timezone).sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
						.user("user", accountKey).meter().sum();

		DataQuery currentWeekQuery = currentWeekQueryBuilder.build();
		DataQueryResponse currentWeekQueryResponse = dataService.execute(currentWeekQuery);
		ArrayList<GroupDataSeries> currentWeekDataSeriesMeter = currentWeekQueryResponse.getMeters();

		if (currentWeekDataSeriesMeter == null || currentWeekDataSeriesMeter.isEmpty()) {
			return false;
		}

		for (GroupDataSeries serie : currentWeekDataSeriesMeter) {
			if (!serie.getPoints().isEmpty()) {
				ArrayList<DataPoint> points = serie.getPoints();
				DataPoint dataPoint = points.get(0);
				MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
				Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
				currentWeekConsumptionSWM = (metricsMap.get(EnumMetric.SUM)) / 7;
			}
		}
		return ((currentWeekConsumptionSWM != null) && (currentWeekConsumptionSWM < aggregates
						.getTop10BaseWeekSWM().getValue()));
	}

	// 1 recommendation - Spend 1 less minute in the shower and save {integer1}
	// {integer2}
	public SimpleEntry<Boolean, Integer> recommendLessShowerTimeAmphiro(ConsumptionStats aggregates,
					UUID accountKey, DateTimeZone timezone) {

		if (aggregates.getAverageDurationAmphiro().getValue() == null || aggregates.getAverageMonthlyAmphiro().getValue() == null) {
			return new SimpleEntry<>(false, null);
		}

		boolean fireAlert = false;
		double userAverageMonthlyConsumption = 0;
		DataQueryBuilder durationQueryBuilder = new DataQueryBuilder();
		durationQueryBuilder.timezone(timezone).sliding(-3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
						.user("user", accountKey).amphiro().average();

		DataQuery durationQuery = durationQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(durationQuery);
		ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

		if (dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}

		for (GroupDataSeries serie : dataSeriesAmphiro) {
                        if (!serie.getPoints().isEmpty()) {
                                DataPoint dataPoint = serie.getPoints().get(0);
                                AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
                                userAverageMonthlyConsumption = (amphiroDataPoint.getVolume().get(EnumMetric.SUM)) / 3;
                                if (amphiroDataPoint.getDuration().get(EnumMetric.AVERAGE) > (aggregates.getAverageDurationAmphiro().getValue()*1.5)) {
                                        fireAlert = true;
                                }
                        }
		}

		Double averageMonthlyConsumptionAggregate = aggregates.getAverageMonthlyAmphiro().getValue();
		if ((averageMonthlyConsumptionAggregate != null)
						&& (userAverageMonthlyConsumption > averageMonthlyConsumptionAggregate)) {
			Double annualSavings = (userAverageMonthlyConsumption - averageMonthlyConsumptionAggregate) * 12;
			return new SimpleEntry<>(fireAlert, annualSavings.intValue());
		} else {
			return new SimpleEntry<>(false, null);
		}
	}

	// 2 recommendation - You could save {currency1} euros if you used a bit
	// less hot water in the shower. {currency2}
	public SimpleEntry<Boolean, Integer> recommendLowerTemperatureAmphiro(ConsumptionStats aggregates,
					UUID accountKey, DateTimeZone timezone) {
		if (aggregates.getAverageTemperatureAmphiro().getValue() == null) {
			return new SimpleEntry<>(false, null);
		}

		boolean fireAlert = false;
		double userAverageMonthlyConsumption = 0;
		DataQueryBuilder durationQueryBuilder = new DataQueryBuilder();
		durationQueryBuilder.timezone(timezone).sliding(-3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
						.user("user", accountKey).amphiro().sum().average();

		DataQuery durationQuery = durationQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(durationQuery);
		ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

		if (dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}

		for (GroupDataSeries serie : dataSeriesAmphiro) {
                        if (!serie.getPoints().isEmpty()) {
                                DataPoint p = serie.getPoints().get(0);
                                AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) p;
                                userAverageMonthlyConsumption = (amphiroPoint.getVolume().get(EnumMetric.SUM)) / 3;

                                if (amphiroPoint.getTemperature().get(EnumMetric.AVERAGE) > aggregates.getAverageTemperatureAmphiro().getValue()) {
                                        fireAlert = true;
                                }
                        }
		}

		Double annualConsumption = userAverageMonthlyConsumption * 12;
		return new SimpleEntry<>(fireAlert, annualConsumption.intValue());
	}

	// 3 recommendation - Reduce the water flow in the shower and gain
	// {integer1} {integer2}
	public SimpleEntry<Boolean, Integer> recommendLowerFlowAmphiro(ConsumptionStats aggregates,
					UUID accountKey, DateTimeZone timezone) {

		if (aggregates.getAverageMonthlyAmphiro().getValue() == null) {
			return new SimpleEntry<>(false, null);
		}

		double userAverageFlow = 0;
		double userThreeMonthsConsumption = 0;
		DataQueryBuilder flowQueryBuilder = new DataQueryBuilder();
		flowQueryBuilder.timezone(timezone).sliding(-3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
						.user("user", accountKey).amphiro().average();

		DataQuery flowQuery = flowQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(flowQuery);
		ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

		if (dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}

		for (GroupDataSeries serie : dataSeriesAmphiro) {
                        if (!serie.getPoints().isEmpty()) {
                                DataPoint dataPoint = serie.getPoints().get(0);
                                AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
                                userAverageFlow = amphiroDataPoint.getFlow().get(EnumMetric.AVERAGE);
                        }
		}

		if (userAverageFlow > aggregates.getAverageFlowAmphiro().getValue()) {
			DataQueryBuilder volumeQueryBuilder = new DataQueryBuilder();
			volumeQueryBuilder.timezone(timezone).sliding(-3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
							.user("user", accountKey).amphiro().sum();

			DataQuery volumeQuery = flowQueryBuilder.build();
			DataQueryResponse volumeQueryResponse = dataService.execute(volumeQuery);
			ArrayList<GroupDataSeries> volumeDataSeriesAmphiro = volumeQueryResponse.getDevices();

			if (volumeDataSeriesAmphiro == null || volumeDataSeriesAmphiro.isEmpty()) {
				return new SimpleEntry<>(false, null);
			}

			for (GroupDataSeries serie : volumeDataSeriesAmphiro) {
                                if (!serie.getPoints().isEmpty()) {
                                        DataPoint dataPoint = serie.getPoints().get(0);
                                        AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
                                        userThreeMonthsConsumption = amphiroDataPoint.getVolume().get(EnumMetric.SUM);
                                }
			}

			Double userAnnualConsumption = userThreeMonthsConsumption * 4;
			Double averageAnnualConsumption = aggregates.getAverageMonthlyAmphiro().getValue() * 12;
			if (userAnnualConsumption > (averageAnnualConsumption)) {
				Double literSavedAnnualy = userAnnualConsumption - averageAnnualConsumption;

				return new SimpleEntry<>(true, literSavedAnnualy.intValue());
			} else {
				return new SimpleEntry<>(false, null);
			}
		} else {
			return new SimpleEntry<>(false, null);
		}
	}

	// 4 recommendation - Change your shower head and save {integer1} {integer2}
	public SimpleEntry<Boolean, Integer> recommendShowerHeadChangeAmphiro(ConsumptionStats aggregates,
					UUID accountKey, DateTimeZone timezone) {
		if (aggregates.getAverageMonthlyAmphiro().getValue() == null) {
			return new SimpleEntry<>(false, null);
		}

		boolean fireAlert = false;
		double userAverageFlow = 0;
		double userThreeMonthsConsumption = 0;
		DataQueryBuilder flowQueryBuilder = new DataQueryBuilder();
		flowQueryBuilder.timezone(timezone).sliding(-3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
						.user("user", accountKey).amphiro().average();

		DataQuery flowQuery = flowQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(flowQuery);
		ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

		if (dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}

		for (GroupDataSeries serie : dataSeriesAmphiro) {
                        if (!serie.getPoints().isEmpty()) {
                                DataPoint dataPoint = serie.getPoints().get(0);
                                AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
                                userAverageFlow = amphiroDataPoint.getFlow().get(EnumMetric.AVERAGE);
                        }
		}

		if (userAverageFlow > aggregates.getAverageFlowAmphiro().getValue()) {
			DataQueryBuilder volumeQueryBuilder = new DataQueryBuilder();
			volumeQueryBuilder.timezone(timezone).sliding(-3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
							.user("user", accountKey).amphiro().sum();

			DataQuery volumeQuery = flowQueryBuilder.build();
			DataQueryResponse volumeQueryResponse = dataService.execute(volumeQuery);
			ArrayList<GroupDataSeries> volumeDataSeriesAmphiro = volumeQueryResponse.getDevices();

			if (volumeDataSeriesAmphiro == null || volumeDataSeriesAmphiro.isEmpty()) {
				return new SimpleEntry<>(false, null);
			}

			for (GroupDataSeries serie : volumeDataSeriesAmphiro) {
                                if (!serie.getPoints().isEmpty()) {
                                        DataPoint dataPoint = serie.getPoints().get(0);
                                        AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
                                        userThreeMonthsConsumption = amphiroDataPoint.getVolume().get(EnumMetric.SUM);
                                }
			}

			Double userAnnualConsumption = userThreeMonthsConsumption * 4;
			Double averageAnnualConsumption = aggregates.getAverageMonthlyAmphiro().getValue() * 12;
			if (userAnnualConsumption > (averageAnnualConsumption)) {
				Double literSavedAnnualy = userAnnualConsumption - averageAnnualConsumption;
				return new SimpleEntry<>(fireAlert, literSavedAnnualy.intValue());
			} else {
				return new SimpleEntry<>(false, null);
			}
		} else {
			return new SimpleEntry<>(false, null);
		}
	}

	// 5 recommendation - Have you considered changing your shampoo? {integer1}
	// percent
	public SimpleEntry<Boolean, Integer> recommendShampooChangeAmphiro(ConsumptionStats aggregates,
					UUID accountKey, DateTimeZone timezone) {
		if (aggregates.getAverageMonthlyAmphiro().getValue() == null) {
			return new SimpleEntry<>(false, null);
		}

		double userAverageMonthlyConsumption = 0;
		DataQueryBuilder durationQueryBuilder = new DataQueryBuilder();
		durationQueryBuilder.timezone(timezone).sliding(-3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
						.user("user", accountKey).amphiro().sum();

		DataQuery durationQuery = durationQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(durationQuery);
		ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

		if (dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}

		for (GroupDataSeries serie : dataSeriesAmphiro) {
                        if (!serie.getPoints().isEmpty()) {
                                DataPoint dataPoint = serie.getPoints().get(0);
                                AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
                                userAverageMonthlyConsumption = amphiroDataPoint.getVolume().get(EnumMetric.SUM) / 3;
                        }
		}

		Double averageMonthlyConsumptionAmphiro = aggregates.getAverageMonthlyAmphiro().getValue();
		if (userAverageMonthlyConsumption > averageMonthlyConsumptionAmphiro) {
			double userConsumptionPercent = (userAverageMonthlyConsumption * 100) / averageMonthlyConsumptionAmphiro;
			int consumptionExcessPercent = (int) (userConsumptionPercent - 100);

			return new SimpleEntry<>(true, consumptionExcessPercent);
		} else {
			return new SimpleEntry<>(false, null);
		}
	}

	// 6 recommendation - When showering, reduce the water flow when you do not
	// need it {integer1} {integer2}
	public SimpleEntry<Boolean, Integer> recommendReduceFlowWhenNotNeededAmphiro(
					ConsumptionStats aggregates, UUID accountKey, DateTimeZone timezone) {
		if (aggregates.getAverageSessionAmphiro().getValue() == null) {
			return new SimpleEntry<>(false, null);
		}

		boolean fireAlert = false;
		double averageSessionConsumption = 0;
		DataQueryBuilder durationQueryBuilder = new DataQueryBuilder();
		durationQueryBuilder.timezone(timezone).sliding(-3, EnumTimeUnit.MONTH, EnumTimeAggregation.ALL)
						.user("user", accountKey).amphiro().average();

		DataQuery durationQuery = durationQueryBuilder.build();
		DataQueryResponse queryResponse = dataService.execute(durationQuery);
		ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

		if (dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()) {
			return new SimpleEntry<>(false, null);
		}

		for (GroupDataSeries serie : dataSeriesAmphiro) {
                        if (!serie.getPoints().isEmpty()) {
                                DataPoint dataPoint = serie.getPoints().get(0);
                                AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
                                averageSessionConsumption = amphiroDataPoint.getVolume().get(EnumMetric.AVERAGE);
                        }
		}

		// TODO - calculate the number of sessions per year when available
		// TODO - add additional check for flow adjustments during the session
		// when available
		int numberOfSessionsPerYear = 100;
		Double averageSessionConsumptionAggregate = aggregates.getAverageSessionAmphiro().getValue();
		if (averageSessionConsumption > averageSessionConsumptionAggregate) {
			Double moreLitersThanOthersPerYear = (averageSessionConsumption - averageSessionConsumptionAggregate)
							* numberOfSessionsPerYear;

			return new SimpleEntry<>(fireAlert, moreLitersThanOthersPerYear.intValue());
		} else {
			return new SimpleEntry<>(false, null);
		}
	}

    private boolean isMeterInstalledForUser(UUID userKey){
        DeviceRegistrationQuery query = new DeviceRegistrationQuery(EnumDeviceType.METER);
        return !deviceRepository.getUserDevices(userKey, query).isEmpty(); 
    }
        
    private boolean isAmphiroInstalledForUser(UUID userKey){
        DeviceRegistrationQuery query = new DeviceRegistrationQuery(EnumDeviceType.AMPHIRO);
        return !deviceRepository.getUserDevices(userKey, query).isEmpty(); 
    }        
                
	private int computeConsecutiveZeroConsumptions(List<Double> values) {
		int maxLength = 0;
		int tempLength = 0;
		for (Double value : values) {

			if (value == 0) {
				tempLength++;
			} else {
				tempLength = 0;
			}

			if (tempLength > maxLength) {
				maxLength = tempLength;
			}
		}
		return maxLength;
	}

}
