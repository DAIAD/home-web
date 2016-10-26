package eu.daiad.web.service.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.message.ConsumptionStats;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
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
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.message.aggregates.ComputedNumber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//TODO - define some sanity values for checking the produced results

@Service
public class DefaultConsumptionAggregationService implements IConsumptionAggregationService {

	@Autowired
	IUserRepository userRepository;

	@Autowired
	IGroupRepository groupRepository;

	@Autowired
	IDataService dataService;
    
    private static final Log logger = LogFactory.getLog(DefaultConsumptionAggregationService.class);

    /**
     * Should a ComputedNumber considered stale?
     * 
     * @param n
     * @param days The maximum age (in days)
     * @return
     */
    private boolean shouldRefresh(ComputedNumber n, MessageCalculationConfiguration config)
    {
        if (n == null || n.getValue() == null)
            return true;
        
        int maxAgeInDays = config.getAggregateComputationInterval();
        DateTime t1 = n.getTimestamp();
        DateTime t0 = DateTime.now().minusDays(maxAgeInDays);
        if (t1 == null || t1.isBefore(t0))
            return true;
        
        return false;
    }
    
    /**
     * Factory method for creating a DataQueryBuilder targeting a specific utility.
     * The timezone will be inferred from the utility's timezone.
     * 
     * @return
     */
    private DataQueryBuilder newQueryBuilder(UtilityInfo utility)
    {
        DataQueryBuilder qb = new DataQueryBuilder();
        
        // Fixme: Why perform a query with (thousands of) user IDs instead of utility ID
        List<UUID> uuids = groupRepository.getUtilityByIdMemberKeys(utility.getId());
        UUID[] uuids1 = ((List<UUID>) uuids).toArray(new UUID[uuids.size()]);
        qb.users("utility", uuids1);
        
        qb.timezone(DateTimeZone.forID(utility.getTimezone()));
        
        return qb;
    }
    
	@Override
	public ConsumptionStats compute(UtilityInfo utility) {
        
		ConsumptionStats stats = new ConsumptionStats(utility.getId());
        		
		// Meter
		
        stats.setAverageMonthlySWM(computeMeterAverage(utility, 30));
		
        stats.setAverageWeeklySWM(computeMeterAverage(utility, 7));
        
        stats.setTop10BaseMonthSWM(computeMeterTopThreshold(utility, 30, 10));
        
        stats.setTop10BaseWeekSWM(computeMeterTopThreshold(utility, 7, 10));
        
        stats.setTop25BaseWeekSWM(computeMeterTopThreshold(utility, 7, 25));
        
        // Amphiro B1
		
        stats.setAverageMonthlyAmphiro(computeAmphiroAverage(utility, 30));
		
        stats.setAverageWeeklyAmphiro(computeAmphiroAverage(utility, 7));					
		
        stats.setTop10BaseMonthAmphiro(computeAmphiroTopThreshold(utility, 30, 10));
		
        stats.setAverageTemperatureAmphiro(computeAmphiroAverageTemperature(utility, 30));
		
        stats.setAverageDurationAmphiro(computeAmphiroAverageDuration(utility, 30));
		
        stats.setAverageFlowAmphiro(computeAmphiroAverageFlow(utility, 30));
		
        stats.setAverageSessionAmphiro(computeAmphiroAverageSession(utility, 30));
        
        logger.info(stats.toString());
        
		return stats;
	}

	/**
	 * Compute average for meters, for a given time interval
	 * 
	 * @param utility
	 * @param days A sliding time interval (in days) 
	 * @return
	 */
	private ComputedNumber computeMeterAverage(UtilityInfo utility, int days)
	{
	    Double result = null;
        
	    DataQueryBuilder qb = newQueryBuilder(utility);
	    qb.sliding(-days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).meter().sum();
	    DataQuery query = qb.build();
	    DataQueryResponse queryResponse = dataService.execute(query);
	    ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

	    // Fixme: The result value is overwritten in each iteration!
	    for (GroupDataSeries series : dataSeriesMeter) {
	        if (series.getPopulation() == 0)
	            continue;
	        if (!series.getPoints().isEmpty()) {
	            ArrayList<DataPoint> points = series.getPoints();
	            DataPoint dataPoint = points.get(0);
	            MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
	            Map<EnumMetric, Double> ma = meterDataPoint.getVolume();
	            result = ma.get(EnumMetric.SUM) / series.getPopulation();
	        } else {
	            result = null;
	        }
	    }

	    return (result != null)? new ComputedNumber(result) : null;
	}
		
	/**
	 * Compute the threshold consumption of a top percentage of users for a given
	 * time interval 
	 * 
	 * @param utility
	 * @param days A sliding time interval (in days)
	 * @param percentage The top percentage of users in terms of consumption
	 * @return
	 */
	private ComputedNumber computeMeterTopThreshold(UtilityInfo utility, int days, int percentage) 
	{  
	    Double result = null;

	    // Compute average for all users.
	    
	    List<UUID> uuids = groupRepository.getUtilityByIdMemberKeys(utility.getId());
	    List<Double> averageConsumptions = new ArrayList<>();

	    DataQueryBuilder qb = new DataQueryBuilder();
	    qb.timezone(utility.getTimezone());
	    for (UUID uuid : uuids) {
	        qb.sliding(-days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).user("user", uuid).meter().average();    
	        DataQuery query = qb.build();
	        DataQueryResponse queryResponse = dataService.execute(query);
	        ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters(); 
	        
	        // Fixme: Adds multiple meters, hence we dont compute the per-user average! 
	        for (GroupDataSeries series : dataSeriesMeter) {
	            if (!series.getPoints().isEmpty()) {
	                ArrayList<DataPoint> userPoints = series.getPoints();
	                DataPoint dataPoint = userPoints.get(0);
	                MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
	                Map<EnumMetric, Double> ma = meterDataPoint.getVolume();
	                averageConsumptions.add(ma.get(EnumMetric.AVERAGE));
	            }
	        }                
	    }            
	    
	    // The base threshold is the consumption of the last user of the top K%
	          
	    if (!averageConsumptions.isEmpty()) {
	        Collections.sort(averageConsumptions);
	        int i = (int) ((averageConsumptions.size() * percentage) / 100);
	        result = averageConsumptions.get(i);
	    } else {
	        result = null;
	    }
	    
	    return (result != null)? new ComputedNumber(result) : null;
	}

	/**
     * Compute average for Amphiro B1 devices, for a given time interval
     * 
     * @param utility
     * @param days A sliding time interval (in days) 
     * @return
     */
	private ComputedNumber computeAmphiroAverage(UtilityInfo utility, int days) 
	{
	    Double result = null;
	    
	    DataQueryBuilder qb = newQueryBuilder(utility);
	    qb.sliding(-days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro().sum();

	    DataQuery query = qb.build();
	    DataQueryResponse queryResponse = dataService.execute(query);
	    ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

	    // Fixme: The result value is overwritten in each iteration!
	    for (GroupDataSeries series : dataSeriesAmphiro) {
	        if (series.getPopulation() == 0) 
	            continue;
	        if (!series.getPoints().isEmpty()) {
	            ArrayList<DataPoint> points = series.getPoints();
	            DataPoint dataPoint = points.get(0);
	            AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
	            Map<EnumMetric, Double> ma = amphiroDataPoint.getVolume();
	            result = ma.get(EnumMetric.SUM) / series.getPopulation();
	        } else {
	            result = null;
	        }
	    }
	    
	    return (result != null)? new ComputedNumber(result) : null;
	}
    
	private ComputedNumber computeAmphiroTopThreshold(UtilityInfo utility, int days, int percentage) 
	{
	    Double result = null;
            
	    List<UUID> uuids = groupRepository.getUtilityByIdMemberKeys(utility.getId());
	    List<Double> averageConsumptions = new ArrayList<>();

	    DataQueryBuilder qb = new DataQueryBuilder();
	    qb.timezone(utility.getTimezone());
	    for (UUID uuid : uuids) {
	        qb.sliding(-days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).user("user", uuid).amphiro().average();    
	        DataQuery query = qb.build();
	        DataQueryResponse queryResponse = dataService.execute(query);
	        ArrayList<GroupDataSeries> amphiroDataSeries = queryResponse.getDevices(); 

	        for (GroupDataSeries series : amphiroDataSeries) {
	            if (!series.getPoints().isEmpty()) {
	                ArrayList<DataPoint> userPoints = series.getPoints();
	                DataPoint dataPoint = userPoints.get(0);
	                AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
	                Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getVolume();
	                averageConsumptions.add(metricsMap.get(EnumMetric.AVERAGE));
	            }
	        }                
	    }             

	    if (!averageConsumptions.isEmpty()) {
	        Collections.sort(averageConsumptions);
	        int i = (int) (averageConsumptions.size() * percentage) / 100;
	        result = averageConsumptions.get(i);                 
	    } else {
	        result = null;
	    }
	    
	    return (result != null)? new ComputedNumber(result) : null;
	}

	private ComputedNumber computeAmphiroAverageTemperature(UtilityInfo utility, int days)
	{
	    Double result = null;
	         
	    DataQueryBuilder qb = newQueryBuilder(utility);
	    qb.sliding(-days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro().average();

	    DataQuery query = qb.build();
	    DataQueryResponse queryResponse = dataService.execute(query);
	    ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

	    // Fixme: The result value is overwritten in each iteration!
	    for (GroupDataSeries series : dataSeriesAmphiro) {
	        if (series.getPopulation() == 0)
	            continue;
	        if (!series.getPoints().isEmpty()) {
	            ArrayList<DataPoint> point = series.getPoints();
	            AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) point.get(0);
	            Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getTemperature();
	            result = metricsMap.get(EnumMetric.AVERAGE);;
	        } else {
	           result = null;
	        }
	    }
	    
	    return (result != null)? new ComputedNumber(result) : null;
	}

	private ComputedNumber computeAmphiroAverageSession(UtilityInfo utility, int days) 
	{
	    Double result = null;

	    DataQueryBuilder qb = newQueryBuilder(utility);
	    qb.sliding(-days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro().average();

	    DataQuery query = qb.build();
	    DataQueryResponse queryResponse = dataService.execute(query);
	    ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

	    // Fixme: The result value is overwritten in each iteration!
	    for (GroupDataSeries series : dataSeriesAmphiro) {
	        if (series.getPopulation() == 0)
	            continue;
	        if (!series.getPoints().isEmpty()) {
	            ArrayList<DataPoint> point = series.getPoints();
	            AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) point.get(0);
	            Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getVolume();
	            result = metricsMap.get(EnumMetric.AVERAGE);
	        } else {
	            result = null;
	        }
	    }
	    
	    return (result != null)? new ComputedNumber(result) : null;
	}
    
	private ComputedNumber computeAmphiroAverageFlow(UtilityInfo utility, int days) 
	{
	    Double result = null;
  
	    DataQueryBuilder qb = newQueryBuilder(utility);
	    qb.sliding(-days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro().average();

	    DataQuery query = qb.build();
	    DataQueryResponse queryResponse = dataService.execute(query);
	    ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();
	    
	    // Fixme: The result value is overwritten in each iteration!
	    for (GroupDataSeries series : dataSeriesAmphiro) {
	        if (series.getPopulation() == 0) 
	            continue;
	        if (!series.getPoints().isEmpty()) {
	            ArrayList<DataPoint> point = series.getPoints();
	            AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) point.get(0);
	            Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getFlow();
	            result = metricsMap.get(EnumMetric.AVERAGE);                     
	        } else {
	            result = null;
	        }
	    }
		
	    return (result != null)? new ComputedNumber(result) : null;
	}
    
	private ComputedNumber computeAmphiroAverageDuration(UtilityInfo utility, int days) 
	{
	    Double result = null;

	    DataQueryBuilder qb = newQueryBuilder(utility);
	    qb.sliding(-days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro().average();

	    DataQuery query = qb.build();
	    DataQueryResponse queryResponse = dataService.execute(query);
	    ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

	    for (GroupDataSeries series : dataSeriesAmphiro) {
	        if (series.getPopulation() == 0) 
	            continue;
	        if (!series.getPoints().isEmpty()) {
	            ArrayList<DataPoint> point = series.getPoints();
	            AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) point.get(0);
	            Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getDuration();
	            result = metricsMap.get(EnumMetric.AVERAGE);                     
	        } else {
	            result = null;
	        }
	    }
	    
	    return (result != null)? new ComputedNumber(result) : null;
	}

}
