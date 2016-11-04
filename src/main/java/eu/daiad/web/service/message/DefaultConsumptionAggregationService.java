package eu.daiad.web.service.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.message.ConsumptionStats;
import eu.daiad.web.model.message.ConsumptionStats.EnumStatistic;
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
 
import static eu.daiad.web.model.device.EnumDeviceType.AMPHIRO;
import static eu.daiad.web.model.device.EnumDeviceType.METER;
import static eu.daiad.web.model.query.EnumDataField.VOLUME;
import static eu.daiad.web.model.query.EnumDataField.TEMPERATURE;
import static eu.daiad.web.model.query.EnumDataField.DURATION;
import static eu.daiad.web.model.query.EnumDataField.FLOW;
import static eu.daiad.web.model.query.EnumDataField.ENERGY;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Service
@ConfigurationProperties(prefix = "daiad.services.consumption-aggregation")
public class DefaultConsumptionAggregationService implements IConsumptionAggregationService 
{
    private static final Log logger = LogFactory.getLog(DefaultConsumptionAggregationService.class);
    
	@Autowired
	IUserRepository userRepository;

	@Autowired
	IGroupRepository groupRepository;

	@Autowired
	IDataService dataService;
	
	private DateTimeZone defaultTimezone;
	
	@Value("${daiad.default-timezone:Europe/Athens}")
	public void setDefaultTimezone(String name) {
	    defaultTimezone = DateTimeZone.forID(name);
	}
	
	private class Aggregator
	{
	    private final UtilityInfo utility;
	    
	    private final DateTime refDate;
	    
	    public Aggregator(UtilityInfo utility, LocalDateTime refDate)
        {
            this.utility = utility;
            
            if (refDate == null)
                refDate = LocalDateTime.now();
            this.refDate = refDate.toDateTime(DateTimeZone.forID(utility.getTimezone()));
        }
	    
	    /**
	     * Factory method for creating a DataQueryBuilder inside a given utility.
	     */
	    public DataQueryBuilder newQueryBuilder()
	    {
	        DataQueryBuilder qb = new DataQueryBuilder();
	        qb.timezone(DateTimeZone.forID(utility.getTimezone()));
	        return qb;
	    }
	    
	    /**
	     * Compute average consumption (lit) for meters, for a given time interval
	     * @param days A sliding time interval (in days) 
	     */
	    private ComputedNumber computeMeterAverage(int days)
	    {
	        DataQueryBuilder querybuilder = newQueryBuilder()
	                .utility("utility", utility.getKey())
	                .sliding(refDate, -days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
	                .meter()
	                .sum();   
	        DataQuery query = querybuilder.build();
	        DataQueryResponse queryResponse = dataService.execute(query);
	        ArrayList<GroupDataSeries> meterSeries = queryResponse.getMeters();

	        if (meterSeries.size() > 1)
	            logger.warn("computeMeterAverage: Received > 1 series!");
	        
	        Double result = null;       
	        GroupDataSeries series = meterSeries.get(0);
	        if (series.getPopulation() > 0 && series.getPoints().size() > 0) {
	            MeterDataPoint datapoint = (MeterDataPoint) series.getPoints().get(0);
	            Map<EnumMetric, Double> ma = datapoint.getVolume();
	            result = ma.get(EnumMetric.SUM) / series.getPopulation();
	        }

	        logger.info("Meter - Computed utility average of last " + days + " days: " + 
	                ((result == null)? "NULL" : result.toString()));
	        
	        return (result != null)? new ComputedNumber(result) : null;
	    }
	    
	    /**
	     * Compute the threshold consumption (lit) of a bottom percentage of users for a given
	     * time interval.
	     * @param days A sliding time interval (in days)
	     * @param percentage The percentage of users
	     */
	    private ComputedNumber computeMeterBottomThreshold(int days, int percentage) 
	    {  
	        // Compute average for all users.
	        
	        List<UUID> uuids = groupRepository.getUtilityByIdMemberKeys(utility.getId());
	        List<Double> averages = new ArrayList<>();
	        for (UUID uuid: uuids) {
	            DataQueryBuilder querybuilder = newQueryBuilder()
	                .user("user", uuid)    
	                .sliding(refDate, -days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
	                .meter()
	                .average();  
	            DataQuery query = querybuilder.build();
	            DataQueryResponse queryResponse = dataService.execute(query);
	            ArrayList<GroupDataSeries> meterSeries = queryResponse.getMeters(); 
	             
	            double s = .0; // sum of averages of meters belonging to a specific user
	            int n = 0;     // number of active meters for a specific user  
	            for (GroupDataSeries series : meterSeries) {
	                if (!series.getPoints().isEmpty()) {
	                    MeterDataPoint datapoint = (MeterDataPoint) series.getPoints().get(0);
	                    Map<EnumMetric, Double> ma = datapoint.getVolume();
	                    n++;
	                    s += ma.get(EnumMetric.AVERAGE);
	                }
	            }
	            if (n > 0)
	                averages.add(s / n);
	        }            
	        
	        // The base threshold is the consumption of the last user of the top K%
            
	        Double result = null;       
	        if (!averages.isEmpty()) {
	            Collections.sort(averages);
	            int i = (int) ((averages.size() * percentage) / 100);
	            result = averages.get(i);
	        }
	        
	        logger.info("Meter - Computed utility threshold of top " + percentage + "% of last " + days + " days: " + 
	                ((result == null)? "NULL" : result.toString()));
	        
	        return (result != null)? new ComputedNumber(result) : null;
	    }
	
	    /**
	     * Compute average consumption (lit) for Amphiro B1 devices, for the given time interval
	     * @param days A sliding time interval (in days)
	     */
	    private ComputedNumber computeAmphiroAverage(int days) 
	    {
	        DataQueryBuilder querybuilder = newQueryBuilder()
	                .utility("utility", utility.getKey())
	                .sliding(refDate, -days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
	                .amphiro()
	                .sum();
	        DataQuery query = querybuilder.build();
	        DataQueryResponse queryResponse = dataService.execute(query);
	        ArrayList<GroupDataSeries> amphiroSeries = queryResponse.getDevices();

	        if (amphiroSeries.size() > 1)
	            logger.warn("computeAmphiroAverage: Received > 1 series!");
	        
	        Double result = null;
	        GroupDataSeries series = amphiroSeries.get(0);
	        if (series.getPopulation() > 0 && series.getPoints().size() > 0) {
	            AmphiroDataPoint datapoint = (AmphiroDataPoint) series.getPoints().get(0);
	            Map<EnumMetric, Double> ma = datapoint.getVolume();
	            result = ma.get(EnumMetric.SUM) / series.getPopulation();
	        }

	        logger.info("Amphiro - Computed average of last " + days + " days: " + 
	                ((result == null)? "NULL" : result.toString()));
	        
	        return (result != null)? new ComputedNumber(result) : null;
	    }
	    
	    /**
	     * Compute average per-session consumption (lit) for Amphiro B1 devices, for the given time interval
	     * @param days days A sliding time interval (in days)
	     */
	    private ComputedNumber computeAmphiroAveragePerSession(int days)
	    {
	        DataQueryBuilder querybuilder = newQueryBuilder()
	                .utility("utility", utility.getKey())
	                .sliding(refDate, -days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
	                .amphiro()
	                .average();
	        DataQuery query = querybuilder.build();
	        DataQueryResponse queryResponse = dataService.execute(query);
	        ArrayList<GroupDataSeries> amphiroSeries = queryResponse.getDevices();

	        if (amphiroSeries.size() > 1)
	            logger.warn("computeAmphiroAveragePerSession: Received > 1 series!");
	        
	        Double result = null;
	        GroupDataSeries series = amphiroSeries.get(0);
	        if (series.getPopulation() > 0 && series.getPoints().size() > 0) {
	            AmphiroDataPoint datapoint = (AmphiroDataPoint) series.getPoints().get(0);
	            Map<EnumMetric, Double> ma = datapoint.getVolume();
	            result = ma.get(EnumMetric.AVERAGE);
	        }

	        logger.info("Amphiro - Computed average of last " + days + " days: " + 
	                ((result == null)? "NULL" : result.toString()));

	        return (result != null)? new ComputedNumber(result) : null;
	    }
	    
	    private ComputedNumber computeAmphiroBottomThreshold(int days, int percentage) 
	    {
	        List<UUID> uuids = groupRepository.getUtilityByIdMemberKeys(utility.getId());
	        List<Double> averages = new ArrayList<>();
	        for (UUID uuid : uuids) {
	            DataQueryBuilder querybuilder = newQueryBuilder()
	                .user("user", uuid)
	                .sliding(refDate, -days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
	                .amphiro()
	                .average();    
	            DataQuery query = querybuilder.build();
	            DataQueryResponse queryResponse = dataService.execute(query);
	            ArrayList<GroupDataSeries> amphiroSeries = queryResponse.getDevices(); 
	            
	            int n = 0;
	            double s = .0;
	            for (GroupDataSeries series : amphiroSeries) {
	                if (!series.getPoints().isEmpty()) {
	                    AmphiroDataPoint datapoint = (AmphiroDataPoint) series.getPoints().get(0);
	                    Map<EnumMetric, Double> ma = datapoint.getVolume();
	                    n++; 
	                    s += ma.get(EnumMetric.AVERAGE);
	                }
	            }
	            if (n > 0)
	                averages.add(s / n);
	        }             
	        
	        Double result = null;
	        if (!averages.isEmpty()) {
	            Collections.sort(averages);
	            int i = (int) (averages.size() * percentage) / 100;
	            result = averages.get(i);                 
	        }
	        
	        logger.info("Amphiro - Computed threshold of top " + percentage + "% of last " + days + " days: " + 
	                ((result == null)? "NULL" : result.toString()));
	        
	        return (result != null)? new ComputedNumber(result) : null;
	    }
	    
	    private ComputedNumber computeAmphiroAverageTemperature(int days)
	    {     
	        DataQueryBuilder querybuilder = newQueryBuilder()
	                .utility("utility", utility.getKey())
	                .sliding(refDate, -days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
	                .amphiro()
	                .average();

	        DataQuery query = querybuilder.build();
	        DataQueryResponse queryResponse = dataService.execute(query);
	        ArrayList<GroupDataSeries> amphiroSeries = queryResponse.getDevices();
	        
	        if (amphiroSeries.size() > 1)
	            logger.warn("computeAmphiroAverageTemperature: Received > 1 series!");
	        
	        Double result = null;
	        GroupDataSeries series = amphiroSeries.get(0);
	        if (series.getPopulation() > 0 && series.getPoints().size() > 0) {
	            AmphiroDataPoint datapoint = (AmphiroDataPoint) series.getPoints().get(0);
	            Map<EnumMetric, Double> ma = datapoint.getTemperature();
	            result = ma.get(EnumMetric.AVERAGE);
	        }
	        
	        logger.info(
	                "Amphiro - Computed average temperature of last " + days + " days: " + 
	                ((result == null)? "NULL" : result.toString()));
	        
	        return (result != null)? new ComputedNumber(result) : null;
	    }
	    
	    private ComputedNumber computeAmphiroAverageDuration(int days) 
	    {
	        DataQueryBuilder querybuilder = newQueryBuilder()
	                .utility("utility", utility.getKey())
	                .sliding(refDate, -days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
	                .amphiro()
	                .average();

	        DataQuery query = querybuilder.build();
	        DataQueryResponse queryResponse = dataService.execute(query);
	        ArrayList<GroupDataSeries> amphiroSeries = queryResponse.getDevices();
	     
	        if (amphiroSeries.size() > 1)
                logger.warn("computeAmphiroAverageDuration: Received > 1 series!");
	        
	        Double result = null;
	        GroupDataSeries series = amphiroSeries.get(0);
	        if (series.getPopulation() > 0 && series.getPoints().size() > 0) {
                AmphiroDataPoint datapoint = (AmphiroDataPoint) series.getPoints().get(0);
                Map<EnumMetric, Double> ma = datapoint.getDuration();
                result = ma.get(EnumMetric.AVERAGE);
            }

	        logger.info(
	                "Amphiro - Computed average duration of last " + days + " days: " + 
	                ((result == null)? "NULL" : result.toString()));
	        
	        return (result != null)? new ComputedNumber(result) : null;
	    }
	    
	    private ComputedNumber computeAmphiroAverageFlow(int days) 
	    {
	        DataQueryBuilder querybuilder = newQueryBuilder()
	                .utility("utility", utility.getKey())
	                .sliding(refDate, -days, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
	                .amphiro()
	                .average();

	        DataQuery query = querybuilder.build();
	        DataQueryResponse queryResponse = dataService.execute(query);
	        ArrayList<GroupDataSeries> amphiroSeries = queryResponse.getDevices();
	        
	        if (amphiroSeries.size() > 1)
	            logger.warn("computeAmphiroAverageFlow: Received > 1 series!");
	        
	        Double result = null;
	        GroupDataSeries series = amphiroSeries.get(0);
            if (series.getPopulation() > 0 && series.getPoints().size() > 0) {
                AmphiroDataPoint datapoint = (AmphiroDataPoint) series.getPoints().get(0);
                Map<EnumMetric, Double> ma = datapoint.getFlow();
                result = ma.get(EnumMetric.AVERAGE);
            }
	        	        
	        logger.info(
	                "Amphiro - Computed average flow of last " + days + " days: " + 
	                ((result == null)? "NULL" : result.toString()));
	        
	        return (result != null)? new ComputedNumber(result) : null;
	    }
	}
	
    /**
     * Should a ComputedNumber be considered stale?
     * @param n
     * @param days The maximum age (in days)
     */
    private static boolean shouldRefresh(ComputedNumber n, int maxAgeInDays)
    {
        if (n == null || n.getValue() == null)
            return true;       
        DateTime t1 = n.getTimestamp();
        DateTime t0 = DateTime.now().minusDays(maxAgeInDays);
        if (t1 == null || t1.isBefore(t0))
            return true;
        return false;
    }
    
	@Override
	public ConsumptionStats compute(UtilityInfo utility, LocalDateTime refDate) {
        
		ConsumptionStats stats = new ConsumptionStats(utility.getId());
        
		Aggregator aggregator = this.new Aggregator(utility, refDate);
		
		// Meter
		
		stats.set(EnumStatistic.AVERAGE_MONTHLY, METER, VOLUME,
		        aggregator.computeMeterAverage(30));
		
		stats.set(EnumStatistic.AVERAGE_WEEKLY, METER, VOLUME,
		        aggregator.computeMeterAverage(7));
		
        stats.set(EnumStatistic.THRESHOLD_BOTTOM_10P_MONTHLY, METER, VOLUME,
                aggregator.computeMeterBottomThreshold(30, 10));
        
        stats.set(EnumStatistic.THRESHOLD_BOTTOM_10P_WEEKLY, METER, VOLUME,
                aggregator.computeMeterBottomThreshold(7, 10));
        
        stats.set(EnumStatistic.THRESHOLD_BOTTOM_25P_WEEKLY, METER, VOLUME,
                aggregator.computeMeterBottomThreshold(7, 25));
        
        // Amphiro B1
		
        stats.set(EnumStatistic.AVERAGE_MONTHLY, AMPHIRO, VOLUME,
                aggregator.computeAmphiroAverage(30));
		
        stats.set(EnumStatistic.AVERAGE_MONTHLY_PER_SESSION, AMPHIRO, VOLUME,
                aggregator.computeAmphiroAveragePerSession(30));
        
        stats.set(EnumStatistic.AVERAGE_WEEKLY, AMPHIRO, VOLUME,
                aggregator.computeAmphiroAverage(7));					
		
        stats.set(EnumStatistic.THRESHOLD_BOTTOM_10P_MONTHLY, AMPHIRO, VOLUME,
                aggregator.computeAmphiroBottomThreshold(30, 10));
		
        stats.set(EnumStatistic.AVERAGE_MONTHLY, AMPHIRO, TEMPERATURE, 
                aggregator.computeAmphiroAverageTemperature(30));
		
        stats.set(EnumStatistic.AVERAGE_MONTHLY, AMPHIRO, DURATION, 
                aggregator.computeAmphiroAverageDuration(30));
		
        stats.set(EnumStatistic.AVERAGE_MONTHLY, AMPHIRO, FLOW,
                aggregator.computeAmphiroAverageFlow(30));
	    
        logger.info(stats.toString());
        
		return stats;
	}
}
