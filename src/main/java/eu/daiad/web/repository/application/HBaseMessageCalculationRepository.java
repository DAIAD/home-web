package eu.daiad.web.repository.application;

import eu.daiad.web.model.query.AmphiroDataPoint;
import eu.daiad.web.model.query.DataPoint;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.EnumTimeAggregation;
import eu.daiad.web.model.query.EnumTimeUnit;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MessageAggregatesContainer;
import eu.daiad.web.model.query.MeterDataPoint;
import eu.daiad.web.service.IDataService;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.PropertySource;

/**
 * HBase repository for computing alerts/recommendations.
 * 
 * @author nkarag
 */

@Repository()
@Scope("prototype")
@PropertySource("${hbase.properties}")
public class HBaseMessageCalculationRepository implements IMessageCalculationRepository {

    @PersistenceContext(unitName="default")
    EntityManager entityManager;

    @Autowired
    IAmphiroMeasurementRepository iAmphiroMeasurementRepository;
    
    @Autowired
    MessageAggregatesContainer messageAggregatesContainer;
    
    @Autowired
    IDataService dataService;   

    private final Integer dailyBudget = 50;
    private final Integer weeklyBudget = 350;
    private final Integer monthlyBudget = 1500;

    private final Integer dailyBudgetAmphiro = 20;
    private final Integer weeklyBudgetAmphiro = 140;
    private final Integer monthlyBudgetAmphiro = 600;        

    //1 alert - Check for water leaks!
    @Override
    public boolean alertWaterLeakSWM(UUID userKey) {
        
        boolean fireAlert = true;
        DataQueryBuilder queryBuilder = new DataQueryBuilder();
        queryBuilder.sliding(-48, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
                .user("user", userKey).meter().min();             

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

        for (GroupDataSeries serie : dataSeriesMeter) {
            if(!serie.getPoints().isEmpty()){ //check for non existent data 
                ArrayList<DataPoint> points = serie.getPoints();
                for(DataPoint point : points){
                    MeterDataPoint meterPoint = (MeterDataPoint) point;
                    if(meterPoint.getVolume().get(EnumMetric.MIN) == 0){
                        fireAlert = false;
                    }
                }
            }
        }       
        return fireAlert;
    }

    //2 alert - Shower still on!
    @Override
    public boolean alertShowerStillOnAmphiro(UUID userKey) {  

        boolean fireAlert = false;
        Integer durationThresholdMinutes = messageAggregatesContainer.getShowerDurationThresholdMinutes();
        DataQueryBuilder queryBuilder = new DataQueryBuilder();
        queryBuilder.sliding(-24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
                .user("user", userKey).amphiro().max();             

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

        for (GroupDataSeries serie : dataSeriesAmphiro) {
            
            ArrayList<DataPoint> points = serie.getPoints();
            for(DataPoint point : points){
                AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) point;
                if(amphiroPoint.getDuration().get(EnumMetric.MAX) > durationThresholdMinutes){
                    fireAlert = true;
                }
            }            
        }       
        return fireAlert;
    }
    
    //5 alert - Water quality not assured!
    @Override
    public boolean alertWaterQualitySWM(UUID userKey) {

        boolean fireAlert = false;
        DataQueryBuilder queryBuilder = new DataQueryBuilder();
        queryBuilder.sliding(-24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
                .user("user", userKey).meter().min();             

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();

        for (GroupDataSeries serie : dataSeriesMeter) {
            if(!serie.getPoints().isEmpty()){ //check for non existent data 
                ArrayList<DataPoint> points = serie.getPoints();
                for(DataPoint point : points){
                    MeterDataPoint meterPoint = (MeterDataPoint) point;
                    if(meterPoint.getVolume().get(EnumMetric.COUNT) == 0){
                        fireAlert = true;
                    }
                }
            }
            else{
                fireAlert = true;
            }
        }       
        return fireAlert;
    }
    
    //6 alert - Water too hot!
    @Override
    public boolean alertHotTemperatureAmphiro(UUID userKey) {
        boolean fireAlert = false;
        Float temperatureThreshold = messageAggregatesContainer.getTemperatureThreshold();
        DataQueryBuilder queryBuilder = new DataQueryBuilder();
        queryBuilder.sliding(-24, EnumTimeUnit.HOUR, EnumTimeAggregation.HOUR)
                .user("user", userKey).amphiro().max();             

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

        for (GroupDataSeries serie : dataSeriesAmphiro) {
            if(!serie.getPoints().isEmpty()){ //check for non existent data 
                ArrayList<DataPoint> points = serie.getPoints();
                for(DataPoint point : points){                    
                    AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) point;
                    if(amphiroPoint.getTemperature().get(EnumMetric.MAX) > temperatureThreshold){
                        fireAlert = true;
                    }
                }
            }
        }       
        return fireAlert;
    }

    //7 alert - Reached 80% of your daily water budget {integer1} {integer2}
    @Override
    public Entry<Boolean, SimpleEntry<Integer, Integer>> alertNearDailyBudgetSWM(UUID userKey) {
     
        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).meter().user("user", userKey).sum();
        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        GroupDataSeries dataSeriesMeter = queryResponse.getMeters().get(0);
        ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
        if(dataPoints == null || dataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        DataPoint dataPoint = dataPoints.get(0);
        MeterDataPoint meterPoint = (MeterDataPoint) dataPoint;
        Double lastDaySum = meterPoint.getVolume().get(EnumMetric.SUM);

        double percentUsed = (dailyBudget * lastDaySum) / 100;
        SimpleEntry<Boolean, SimpleEntry<Integer, Integer>> entry;

        if (percentUsed > 80) {
            entry = new SimpleEntry<>(true, new SimpleEntry<>(lastDaySum.intValue(), dailyBudget));
        } else {
            entry = new SimpleEntry<>(false, null);
        }
        return entry;
    } 

    //8 alert - Reached 80% of your daily water budget {integer1} {integer2}    
    @Override
    public Entry<Boolean, SimpleEntry<Integer, Integer>> alertNearWeeklyBudgetSWM(UUID userKey){

        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).meter().user("user", userKey).sum();
        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        GroupDataSeries dataSeriesMeter = queryResponse.getMeters().get(0);
        ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
        if(dataPoints == null || dataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        DataPoint dataPoint = dataPoints.get(0);
        MeterDataPoint meterPoint = (MeterDataPoint) dataPoint;
        Double lastWeekSum = meterPoint.getVolume().get(EnumMetric.SUM);

        double percentUsed = (dailyBudget * lastWeekSum) / 100;
        SimpleEntry<Boolean, SimpleEntry<Integer, Integer>> entry;

        if (percentUsed > 80) {
            entry = new SimpleEntry<>(true, new SimpleEntry<>(lastWeekSum.intValue(), weeklyBudget));
        } else {
            entry = new SimpleEntry<>(false, null);
        }
        return entry;
    } 

    //9 alert - Reached 80% of your daily shower budget {integer1} {integer2}
    @Override
    public Entry<Boolean, SimpleEntry<Integer, Integer>> alertNearDailyBudgetAmphiro(UUID userKey){

        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro().user("user", userKey).sum();
        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        GroupDataSeries dataSeriesMeter = queryResponse.getDevices().get(0);
        ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
        if(dataPoints == null || dataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        DataPoint dataPoint = dataPoints.get(0);
        AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) dataPoint;
        Double lastDaySum = amphiroPoint.getVolume().get(EnumMetric.SUM);

        double percentUsed = (dailyBudgetAmphiro * lastDaySum) / 100;
        SimpleEntry<Boolean, SimpleEntry<Integer, Integer>> entry;

        if (percentUsed > 80) {
            entry = new SimpleEntry<>(true, new SimpleEntry<>(lastDaySum.intValue(), dailyBudgetAmphiro));
        } else {
            entry = new SimpleEntry<>(false, null);
        }
        return entry;
    }     

    //10 alert - Reached 80% of your weekly shower budget {integer1} {integer2}    
    @Override
    public Entry<Boolean, SimpleEntry<Integer, Integer>> alertNearWeeklyBudgetAmphiro(UUID userKey){

        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro().user("user", userKey).sum();
        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        GroupDataSeries dataSeriesMeter = queryResponse.getDevices().get(0);
        ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
        if(dataPoints == null || dataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        DataPoint dataPoint = dataPoints.get(0);
        AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) dataPoint;
        Double lastWeekSum = amphiroPoint.getVolume().get(EnumMetric.SUM);

        double percentUsed = (weeklyBudgetAmphiro * lastWeekSum) / 100;
        SimpleEntry<Boolean, SimpleEntry<Integer, Integer>> entry;

        if (percentUsed > 80) {
            entry = new SimpleEntry<>(true, new SimpleEntry<>(lastWeekSum.intValue(), weeklyBudgetAmphiro));
        } else {
            entry = new SimpleEntry<>(false, null);
        }
        return entry;
    }     
    
    //11 alert - Reached daily Water Budget {integer1}
    @Override
    public Entry<Boolean, Integer> alertReachedDailyBudgetSWM(UUID userKey){

        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).meter().user("user", userKey).sum();
        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        GroupDataSeries dataSeriesMeter = queryResponse.getMeters().get(0);
        ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
        if(dataPoints == null || dataPoints.isEmpty()){
            return new SimpleEntry<>(false, dailyBudget);
        }
        DataPoint dataPoint = dataPoints.get(0);
        MeterDataPoint meterPoint = (MeterDataPoint) dataPoint;
        Double lastDaySum = meterPoint.getVolume().get(EnumMetric.SUM);       
        SimpleEntry<Boolean, Integer> entry;

        if (lastDaySum > dailyBudget*1.2) {
            entry = new SimpleEntry<>(true, dailyBudget);
        } else {
            entry = new SimpleEntry<>(false, dailyBudget);
        }
        return entry;
    } 
    
    //12 alert - Reached daily Shower Budget {integer1}
    @Override
    public Entry<Boolean, Integer> alertReachedDailyBudgetAmphiro(UUID userKey){
        
        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-1, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro().user("user", userKey).sum();
        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        GroupDataSeries dataSeriesMeter = queryResponse.getDevices().get(0);
        ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
        if(dataPoints == null || dataPoints.isEmpty()){
            return new SimpleEntry<>(false, dailyBudgetAmphiro);
        }
        DataPoint dataPoint = dataPoints.get(0);
        AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) dataPoint;
        Double lastDaySum = amphiroPoint.getVolume().get(EnumMetric.SUM);
        SimpleEntry<Boolean, Integer> entry;

        if (lastDaySum > dailyBudgetAmphiro*1.2) {
            entry = new SimpleEntry<>(true, dailyBudgetAmphiro);
        } else {
            entry = new SimpleEntry<>(false, dailyBudgetAmphiro);
        }
        return entry;
    } 
    
    //13 alert - You are a real water champion!
    @Override
    public boolean alertWaterChampionSWM(UUID userKey){
        
        boolean fireAlert = true;       
        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY).meter().user("user", userKey).sum();
        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        GroupDataSeries dataSeriesMeter = queryResponse.getMeters().get(0);
        ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
        if(dataPoints == null || dataPoints.isEmpty()){
            return false;
        }
        
        List<Double> values = new ArrayList<>(); 
        for(DataPoint point : dataPoints){
            MeterDataPoint meterPoint = (MeterDataPoint) point;
            Double daySum = meterPoint.getVolume().get(EnumMetric.SUM);
            values.add(daySum);
            if(daySum > dailyBudget){
                fireAlert = false;
            }            
        }

        if(computeConsecutiveZeroConsumptions(values) > 10){
            fireAlert = false;
        }

        return fireAlert;
    }    
    
    //14 alert - You are a real shower champion!
    @Override
    public boolean alertShowerChampionAmphiro(UUID userKey){
      
        boolean fireAlert = true;       
        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY).amphiro().user("user", userKey).sum();
        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        GroupDataSeries dataSeriesMeter = queryResponse.getDevices().get(0);
        ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
        if(dataPoints == null || dataPoints.isEmpty()){
            return false;
        }
        
        List<Double> values = new ArrayList<>(); 
        for(DataPoint point : dataPoints){
            AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) point;
            Double daySum = amphiroPoint.getVolume().get(EnumMetric.SUM);
            values.add(daySum);
            if(daySum > dailyBudgetAmphiro){
                fireAlert = false;
            }            
        }

        if(computeConsecutiveZeroConsumptions(values) > 10){
            fireAlert = false;
        }

        return fireAlert;
    }     
    
    //15 alert - You are using too much water {integer1}
    @Override
    public SimpleEntry<Boolean, Double> alertTooMuchWaterConsumptionSWM(UUID userKey) {
        
        if(messageAggregatesContainer.getAverageWeeklyConsumptionSWM() == null){
            return new SimpleEntry<>(false, null);
        }
        
        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).meter().user("user", userKey).sum();
        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        GroupDataSeries dataSeriesMeter = queryResponse.getMeters().get(0);
        ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
        if(dataPoints == null || dataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        DataPoint dataPoint = dataPoints.get(0);
        MeterDataPoint meterPoint = (MeterDataPoint) dataPoint;
        Double lastWeekSum = meterPoint.getVolume().get(EnumMetric.SUM);
        SimpleEntry<Boolean, Double> entry;

        if (lastWeekSum > 2* messageAggregatesContainer.getAverageWeeklyConsumptionSWM()) {
            
            //return annual savings if average behaviour is adopted. Multiply with 52 weeks for annual value (liters).
            entry = new SimpleEntry<>
                    (true, (lastWeekSum - messageAggregatesContainer.getAverageWeeklyConsumptionSWM())*52);                       
        } else {
            entry = new SimpleEntry<>(false, null);
        }
        return entry;                
    }

    //16 alert - You are using too much water in the shower {integer1}
    @Override
    public SimpleEntry<Boolean, Double> alertTooMuchWaterConsumptionAmphiro(UUID userKey) {
        
        if(messageAggregatesContainer.getAverageWeeklyConsumptionAmphiro() == null){
            return new SimpleEntry<>(false, null);
        }
        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL).amphiro().user("user", userKey).sum();
        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);

        GroupDataSeries dataSeriesMeter = queryResponse.getDevices().get(0);
        ArrayList<DataPoint> dataPoints = dataSeriesMeter.getPoints();
        if(dataPoints == null || dataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        DataPoint dataPoint = dataPoints.get(0);
        AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) dataPoint;
        Double lastWeekSum = amphiroPoint.getVolume().get(EnumMetric.SUM);
        SimpleEntry<Boolean, Double> entry;

        if (lastWeekSum > 2* messageAggregatesContainer.getAverageWeeklyConsumptionAmphiro()) {
            
            //return annual savings if average behaviour is adopted. Multiply with 52 weeks for annual value.
            entry = new SimpleEntry<>
                    (true, (lastWeekSum - messageAggregatesContainer.getAverageWeeklyConsumptionAmphiro())*52);                       
        } else {
            entry = new SimpleEntry<>(false, null);
        }
        return entry;  
    }

    //17 alert - You are spending too much energy for showering {integer1} {currency}
    @Override
    public SimpleEntry<Boolean, Double> alertTooMuchEnergyAmphiro(UUID userKey) {
        
        boolean fireAlert = true;
        double monthlyShowerConsumption = 0;
        Float temperatureThreshold = messageAggregatesContainer.getTemperatureThreshold();
        DataQueryBuilder queryBuilder = new DataQueryBuilder();
        queryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.DAY)
                .user("user", userKey).amphiro().sum().average();             

        DataQuery query = queryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(query);
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();
        if(dataSeriesAmphiro == null){
            return new SimpleEntry<>(false, null);
        }
  
        for (GroupDataSeries serie : dataSeriesAmphiro) {
            if(!serie.getPoints().isEmpty()){ //check for non existent data 
                ArrayList<DataPoint> points = serie.getPoints();
                for(DataPoint point : points){    
                    
                    AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) point;
                    monthlyShowerConsumption = monthlyShowerConsumption + amphiroPoint.getVolume().get(EnumMetric.SUM);                   
                    
                    
                    if(amphiroPoint.getTemperature().get(EnumMetric.AVERAGE) > temperatureThreshold){
                        fireAlert = true && fireAlert; 
                    }
                    else{
                        fireAlert = false; //if one average temp is below threshold donnot alert
                    }
                }
            }
        }       
    
        return new SimpleEntry<>(fireAlert, monthlyShowerConsumption*12);
    }

    //18 alert - Well done! You have greatly reduced your water use {integer1} percent
    @Override
    public SimpleEntry<Boolean, Integer> alertReducedWaterUseSWM(UUID userKey, DateTime startingWeek) {
                       
        DataQueryBuilder firstWeekDataQueryBuilder = new DataQueryBuilder();
        firstWeekDataQueryBuilder.absolute(startingWeek, startingWeek.plusDays(7), EnumTimeAggregation.ALL)
                .meter().user("user", userKey).sum();              
        DataQuery firstWeekQuery = firstWeekDataQueryBuilder.build();
        DataQueryResponse firstWeekQueryResponse = dataService.execute(firstWeekQuery);
        GroupDataSeries firstWeekDataSeriesMeter = firstWeekQueryResponse.getMeters().get(0);
        ArrayList<DataPoint> firstWeekDataPoints = firstWeekDataSeriesMeter.getPoints();
        if(firstWeekDataPoints == null || firstWeekDataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        DataPoint firstWeekDataPoint = firstWeekDataPoints.get(0);
        MeterDataPoint firstWeekMeterDataPoint = (MeterDataPoint) firstWeekDataPoint;
        Double firstWeekSum = firstWeekMeterDataPoint.getVolume().get(EnumMetric.SUM);
                      
        DataQueryBuilder lastWeekDataQueryBuilder = new DataQueryBuilder();
        lastWeekDataQueryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .meter().user("user", userKey).sum();              
        DataQuery lastWeekQuery = lastWeekDataQueryBuilder.build();
        DataQueryResponse lastWeekQueryResponse = dataService.execute(lastWeekQuery);
        GroupDataSeries lastWeekDataSeriesMeter = lastWeekQueryResponse.getMeters().get(0);
        ArrayList<DataPoint> lastWeekDataPoints = lastWeekDataSeriesMeter.getPoints();
        
        if(lastWeekDataPoints == null || lastWeekDataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        } 
        
        DataPoint lastWeekDataPoint = lastWeekDataPoints.get(0);  
        MeterDataPoint lastWeekMeterDataPoint = (MeterDataPoint) lastWeekDataPoint;
        Double lastWeekSum = lastWeekMeterDataPoint.getVolume().get(EnumMetric.SUM);        
        
        SimpleEntry<Boolean, Integer> entry;
        Double percentDifference = 100 - ((lastWeekSum*100)/firstWeekSum);
        
        if(percentDifference >= 20){
            entry = new SimpleEntry<>(true, percentDifference.intValue());
        }
        else{
            entry = new SimpleEntry<>(false, percentDifference.intValue());
        }
        return entry;      
    }
    
    //19 alert - Well done! You have greatly improved your shower efficiency {integer1} percent
    @Override
    public SimpleEntry<Boolean, Integer> alertImprovedShowerEfficiencyAmphiro(UUID userKey, DateTime startingWeek){  
        
        DataQueryBuilder firstWeekDataQueryBuilder = new DataQueryBuilder();
        firstWeekDataQueryBuilder.absolute(startingWeek, startingWeek.plusDays(7), EnumTimeAggregation.ALL)
                .amphiro().user("user", userKey).sum();              
        DataQuery firstWeekQuery = firstWeekDataQueryBuilder.build();
        DataQueryResponse firstWeekQueryResponse = dataService.execute(firstWeekQuery);
        GroupDataSeries firstWeekDataSeriesMeter = firstWeekQueryResponse.getDevices().get(0);
        ArrayList<DataPoint> firstWeekDataPoints = firstWeekDataSeriesMeter.getPoints();  
        
        if(firstWeekDataPoints == null || firstWeekDataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        
        DataPoint firstWeekDataPoint = firstWeekDataPoints.get(0);  
        AmphiroDataPoint firstWeekAmphiroPoint = (AmphiroDataPoint) firstWeekDataPoint;
        Double firstWeekSum = firstWeekAmphiroPoint.getVolume().get(EnumMetric.SUM);
                      
        DataQueryBuilder lastWeekDataQueryBuilder = new DataQueryBuilder();
        lastWeekDataQueryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .amphiro().user("user", userKey).sum();              
        DataQuery lastWeekQuery = lastWeekDataQueryBuilder.build();
        DataQueryResponse lastWeekQueryResponse = dataService.execute(lastWeekQuery);
        GroupDataSeries lastWeekDataSeriesMeter = lastWeekQueryResponse.getDevices().get(0);
        ArrayList<DataPoint> lastWeekDataPoints = lastWeekDataSeriesMeter.getPoints();
        
        if(lastWeekDataPoints == null || lastWeekDataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        }        
        
        DataPoint lastWeekDataPoint = lastWeekDataPoints.get(0);       
        AmphiroDataPoint lastWeekAmphiroPoint = (AmphiroDataPoint) lastWeekDataPoint;
        Double lastWeekSum = lastWeekAmphiroPoint.getVolume().get(EnumMetric.SUM);        
        
        SimpleEntry<Boolean, Integer> entry;
        Double percentDifference = 100 - ((lastWeekSum*100)/firstWeekSum);
        
        if(percentDifference >= 20){
            entry = new SimpleEntry<>(true, percentDifference.intValue());
        }
        else{
            entry = new SimpleEntry<>(false, percentDifference.intValue());
        }
        return entry;  
    }      
    
    //20 alert - Congratulations! You are a water efficiency leader {integer1} litres
    @Override
    public SimpleEntry<Boolean, Integer> alertWaterEfficiencyLeaderSWM(UUID userKey){
        
        if(messageAggregatesContainer.getTop10BaseMonthThresholdSWM() == null
                || messageAggregatesContainer.getAverageMonthlyConsumptionSWM() == null){
            
            return new SimpleEntry<>(false, null);
        }
        
        Double baseTop10Consumption = messageAggregatesContainer.getTop10BaseMonthThresholdSWM();
        Double averageMonthlyAllUsers = messageAggregatesContainer.getAverageMonthlyConsumptionSWM();

        DataQueryBuilder dataQueryBuilder = new DataQueryBuilder();
        dataQueryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).meter().average();

        DataQuery query = dataQueryBuilder.build();
        DataQueryResponse result = dataService.execute(query);
        GroupDataSeries meter = result.getMeters().get(0);
        ArrayList<DataPoint> dataPoints = meter.getPoints();
        
        if(dataPoints == null || dataPoints.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        
        DataPoint dataPoint = dataPoints.get(0);
        MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
        Map<EnumMetric, Double> m = meterDataPoint.getVolume();
        Double currentMonthAverage = m.get(EnumMetric.AVERAGE);  
        
        SimpleEntry<Boolean, Integer> entry;
        if(currentMonthAverage < baseTop10Consumption){            
            int litersSavedInYear = (int)(averageMonthlyAllUsers - currentMonthAverage)*12;
            entry = new SimpleEntry<>(true, litersSavedInYear);
        }
        else{
            entry = new SimpleEntry<>(false, null);
        }

        return entry;
    }     
    
    //21 alert does not need a computation here.
       
    //22 alert - You are doing a great job!
    @Override
    public boolean alertPromptGoodJobMonthlySWM(UUID userKey) {   
        
        if(messageAggregatesContainer.getAverageMonthlyConsumptionSWM() == null){
            return false;
        }
        
        boolean fireAlert;
        Double currentMonthConsumptionSWM = null;
        Double previousMonthConsumptionSWM = null;
        DataQueryBuilder currentMonthQueryBuilder = new DataQueryBuilder();
        currentMonthQueryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).meter().sum();             

        DataQuery currentMonthQuery = currentMonthQueryBuilder.build();
        DataQueryResponse currentMonthQueryResponse = dataService.execute(currentMonthQuery);
        ArrayList<GroupDataSeries> currentMonthDataSeriesMeter = currentMonthQueryResponse.getMeters();

        for (GroupDataSeries serie : currentMonthDataSeriesMeter) {            
            if(!serie.getPoints().isEmpty()){
                ArrayList<DataPoint> points = serie.getPoints();
                DataPoint dataPoint = points.get(0);
                MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                currentMonthConsumptionSWM = metricsMap.get(EnumMetric.SUM);            
            }
        }     
        
        DataQueryBuilder previousMonthQueryBuilder = new DataQueryBuilder();
        previousMonthQueryBuilder.absolute
            (DateTime.now().minusDays(60),DateTime.now().minusDays(30), EnumTimeAggregation.ALL)
                .user("user", userKey).meter().sum();             

        DataQuery previousMonthQuery = previousMonthQueryBuilder.build();
        DataQueryResponse previousMonthQueryResponse = dataService.execute(previousMonthQuery);
        ArrayList<GroupDataSeries> previousMonthDataSeriesMeter = previousMonthQueryResponse.getMeters();

        for (GroupDataSeries serie : previousMonthDataSeriesMeter) {            
            if(!serie.getPoints().isEmpty()){
                ArrayList<DataPoint> points = serie.getPoints();
                DataPoint dataPoint = points.get(0);
                MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                Map<EnumMetric, Double> ma = meterDataPoint.getVolume();
                previousMonthConsumptionSWM = ma.get(EnumMetric.SUM);            
            }
        }                   
             
        if (currentMonthConsumptionSWM < previousMonthConsumptionSWM) {
            double percentDifferenceFromPreviousMonth = 
                    100 - (currentMonthConsumptionSWM * 100) / previousMonthConsumptionSWM;

            if (percentDifferenceFromPreviousMonth > 25) {                
                fireAlert = true;
            } 
            else{                
                
                fireAlert = percentDifferenceFromPreviousMonth > 6 
                    && currentMonthConsumptionSWM < messageAggregatesContainer.getAverageMonthlyConsumptionSWM();
            }
        } 
        else {
            fireAlert = false;
        }
        
        return fireAlert;
    }

    //23 alert - You have already saved {integer1} litres of water!
    @Override
    public SimpleEntry<Boolean, Integer> alertLitresSavedSWM(UUID userKey){
        
        Double currentWeekConsumptionSWM = null;
        Double previousWeekConsumptionSWM = null;
        DataQueryBuilder currentWeekQueryBuilder = new DataQueryBuilder();
        currentWeekQueryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).meter().sum();             

        DataQuery currentWeekQuery = currentWeekQueryBuilder.build();
        DataQueryResponse currentWeekQueryResponse = dataService.execute(currentWeekQuery);
        ArrayList<GroupDataSeries> currentWeekDataSeriesMeter = currentWeekQueryResponse.getMeters();

        for (GroupDataSeries serie : currentWeekDataSeriesMeter) {            
            if(!serie.getPoints().isEmpty()){
                ArrayList<DataPoint> points = serie.getPoints();
                DataPoint dataPoint = points.get(0);
                MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                currentWeekConsumptionSWM = metricsMap.get(EnumMetric.SUM);            
            }
        }     
        
        DataQueryBuilder previousWeekQueryBuilder = new DataQueryBuilder();
        previousWeekQueryBuilder.absolute
            (DateTime.now().minusDays(14),DateTime.now().minusDays(7), EnumTimeAggregation.ALL)
                .user("user", userKey).meter().sum();             

        DataQuery previousWeekQuery = previousWeekQueryBuilder.build();
        DataQueryResponse previousWeekQueryResponse = dataService.execute(previousWeekQuery);
        ArrayList<GroupDataSeries> previousWeekDataSeriesMeter = previousWeekQueryResponse.getMeters();

        for (GroupDataSeries serie : previousWeekDataSeriesMeter) {            
            if(!serie.getPoints().isEmpty()){
                ArrayList<DataPoint> points = serie.getPoints();
                DataPoint dataPoint = points.get(0);
                MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                Map<EnumMetric, Double> ma = meterDataPoint.getVolume();
                previousWeekConsumptionSWM = ma.get(EnumMetric.SUM);            
            }
        } 
        
        if(previousWeekConsumptionSWM - currentWeekConsumptionSWM > 100){
            Double litresSavedThisWeek = previousWeekConsumptionSWM - currentWeekConsumptionSWM;
            return new SimpleEntry<>(true, litresSavedThisWeek.intValue());
        }
        else{
            return new SimpleEntry<>(false, null);
        }
    }     
    
    //24 alert - Congratulations! You are one of the top 25% savers in your region.
    @Override
    public boolean alertTop25SaverWeeklySWM(UUID userKey){
        
        if(messageAggregatesContainer.getTop25BaseWeekThresholdSWM() == null){
            return false;
        }
        
        Double currentWeekConsumptionSWM = null;
        DataQueryBuilder currentWeekQueryBuilder = new DataQueryBuilder();
        currentWeekQueryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).meter().average();             

        DataQuery currentWeekQuery = currentWeekQueryBuilder.build();
        DataQueryResponse currentWeekQueryResponse = dataService.execute(currentWeekQuery);
        ArrayList<GroupDataSeries> currentWeekDataSeriesMeter = currentWeekQueryResponse.getMeters();

        for (GroupDataSeries serie : currentWeekDataSeriesMeter) {            
            if(!serie.getPoints().isEmpty()){
                ArrayList<DataPoint> points = serie.getPoints();
                DataPoint dataPoint = points.get(0);
                MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                currentWeekConsumptionSWM = metricsMap.get(EnumMetric.AVERAGE);            
            }
        }         
        
        return currentWeekConsumptionSWM < messageAggregatesContainer.getTop25BaseWeekThresholdSWM();
    } 

    //25 alert - Congratulations! You are among the top group of savers in your city.    
    @Override
    public boolean alertTop10SaverSWM(UUID userKey){
        
        if(messageAggregatesContainer.getTop10BaseWeekThresholdSWM() == null){
            return false;
        }
        
        Double currentWeekConsumptionSWM = null;
        DataQueryBuilder currentWeekQueryBuilder = new DataQueryBuilder();
        currentWeekQueryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).meter().average();             

        DataQuery currentWeekQuery = currentWeekQueryBuilder.build();
        DataQueryResponse currentWeekQueryResponse = dataService.execute(currentWeekQuery);
        ArrayList<GroupDataSeries> currentWeekDataSeriesMeter = currentWeekQueryResponse.getMeters();

        if(currentWeekDataSeriesMeter == null || currentWeekDataSeriesMeter.isEmpty()){
            return false;
        }
        
        for (GroupDataSeries serie : currentWeekDataSeriesMeter) {            
            if(!serie.getPoints().isEmpty()){
                ArrayList<DataPoint> points = serie.getPoints();
                DataPoint dataPoint = points.get(0);
                MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                currentWeekConsumptionSWM = metricsMap.get(EnumMetric.AVERAGE);            
            }
        }                 
        return currentWeekConsumptionSWM < messageAggregatesContainer.getTop10BaseWeekThresholdSWM();
    }        

    //1 recommendation - Spend 1 less minute in the shower and save {integer1} {integer2}
    @Override
    public SimpleEntry<Boolean, Integer> recommendLessShowerTimeAmphiro(UUID userKey) {    
        
        if(messageAggregatesContainer.getAverageDurationAmphiro() == null 
                || messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro() == null){
            return new SimpleEntry<>(false, null);
        }
        
        boolean fireAlert = false;
        double averageMonthlyConsumption = 0;
        DataQueryBuilder durationQueryBuilder = new DataQueryBuilder();
        durationQueryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).amphiro().average();             

        DataQuery durationQuery = durationQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(durationQuery);
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

        if(dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        
        for (GroupDataSeries serie : dataSeriesAmphiro) {           
            DataPoint dataPoint = serie.getPoints().get(0); 
            AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
            averageMonthlyConsumption = amphiroDataPoint.getVolume().get(EnumMetric.AVERAGE);                        
            if(amphiroDataPoint.getDuration().get(EnumMetric.AVERAGE) 
                    > messageAggregatesContainer.getAverageDurationAmphiro()){
                fireAlert = true;
            }                        
        }             

        Double averageMonthlyConsumptionAggregate = messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro();        
        if(averageMonthlyConsumption > averageMonthlyConsumptionAggregate){
            Double annualSavings 
                = (averageMonthlyConsumption - averageMonthlyConsumptionAggregate)*12;            
            return new SimpleEntry<>(fireAlert, annualSavings.intValue());
        }
        else{
            return new SimpleEntry<>(false, null);
        }
    } 

    //2 recommendation - You could save {currency1}  euros if you used a bit less hot water in the shower. {currency2}
    @Override
    public SimpleEntry<Boolean, Integer> recommendLowerTemperatureAmphiro(UUID userKey) {    
        if(messageAggregatesContainer.getAverageTemperatureAmphiro() == null){
            return new SimpleEntry<>(false, null);
        }
        
        boolean fireAlert = false;
        double averageMonthlyConsumption = 0;
        DataQueryBuilder durationQueryBuilder = new DataQueryBuilder();
        durationQueryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).amphiro().average();             

        DataQuery durationQuery = durationQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(durationQuery);
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

        if(dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        
        for (GroupDataSeries serie : dataSeriesAmphiro) {           
            DataPoint p = serie.getPoints().get(0);   
            AmphiroDataPoint amphiroPoint = (AmphiroDataPoint) p;
            averageMonthlyConsumption = amphiroPoint.getVolume().get(EnumMetric.AVERAGE);                        
            
            if(amphiroPoint.getTemperature().get(EnumMetric.AVERAGE) 
                    > messageAggregatesContainer.getAverageTemperatureAmphiro()){
                fireAlert = true;
            }                        
        }             
   
        Double annualConsumption = averageMonthlyConsumption*12;
        return new SimpleEntry<>(fireAlert, annualConsumption.intValue());
    }     
    
    //3 recommendation - Reduce the water flow in the shower and gain {integer1} {integer2}
    @Override
    public SimpleEntry<Boolean, Integer> recommendLowerFlowAmphiro(UUID userKey) { 
        
        if(messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro() == null){
            return new SimpleEntry<>(false, null);
        }
        
        boolean fireAlert = false;
        double averageMonthlyConsumption = 0;
        DataQueryBuilder durationQueryBuilder = new DataQueryBuilder();
        durationQueryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).amphiro().average();             

        DataQuery durationQuery = durationQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(durationQuery);
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

        if(dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        
        for (GroupDataSeries serie : dataSeriesAmphiro) {           
            DataPoint dataPoint = serie.getPoints().get(0); 
            AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
            averageMonthlyConsumption = amphiroDataPoint.getVolume().get(EnumMetric.AVERAGE);                                               
        }             
   
        //TODO - change condition using average flow, if available from aggregates
        if(averageMonthlyConsumption > messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro()){
            Double annualConsumption = averageMonthlyConsumption*12;
            return new SimpleEntry<>(fireAlert, annualConsumption.intValue());
        }
        else{
            return new SimpleEntry<>(false, null);
        }        
    } 
    
    //4 recommendation - Change your showerhead and save {integer1} {integer2}
    @Override
    public SimpleEntry<Boolean, Integer> recommendShowerHeadChangeAmphiro(UUID userKey) {    
        if(messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro() == null){
            return new SimpleEntry<>(false, null);
        }
        
        boolean fireAlert = false;
        double averageMonthlyConsumption = 0;
        DataQueryBuilder durationQueryBuilder = new DataQueryBuilder();
        durationQueryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).amphiro().average();             

        DataQuery durationQuery = durationQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(durationQuery);
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

        if(dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        
        for (GroupDataSeries serie : dataSeriesAmphiro) {           
            DataPoint dataPoint = serie.getPoints().get(0); 
            AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
            averageMonthlyConsumption = amphiroDataPoint.getVolume().get(EnumMetric.AVERAGE);                                               
        }             
   
        //TODO - change condition using average flow, if available from aggregates
        if(averageMonthlyConsumption > messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro()){
            Double annualLitresSaved = 
                (averageMonthlyConsumption - messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro())*12;
            return new SimpleEntry<>(fireAlert, annualLitresSaved.intValue());
        }
        else{
            return new SimpleEntry<>(false, null);
        } 
    }           
        
    //5 recommendation - Have you considered changing your shampoo? {integer1} percent    
    @Override
    public SimpleEntry<Boolean, Integer> recommendShampooChangeAmphiro(UUID userKey) {
        if (messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro() == null) {
            return new SimpleEntry<>(false, null);
        }

        double userAverageMonthlyConsumption = 0;
        DataQueryBuilder durationQueryBuilder = new DataQueryBuilder();
        durationQueryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).amphiro().average();

        DataQuery durationQuery = durationQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(durationQuery);
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

        if (dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()) {
            return new SimpleEntry<>(false, null);
        }

        for (GroupDataSeries serie : dataSeriesAmphiro) {
            DataPoint dataPoint = serie.getPoints().get(0);
            AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
            userAverageMonthlyConsumption = amphiroDataPoint.getVolume().get(EnumMetric.AVERAGE);
        }

        Double averageMonthlyConsumptionAmphiro = messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro();
        if (userAverageMonthlyConsumption > averageMonthlyConsumptionAmphiro) {
            double userConsumptionPercent = (userAverageMonthlyConsumption * 100) / averageMonthlyConsumptionAmphiro;
            int consumptionExcessPercent = (int) (userConsumptionPercent - 100);

            return new SimpleEntry<>(true, consumptionExcessPercent);
        } 
        else {
            return new SimpleEntry<>(false, null);
        }
    }   
    
    //6 recommendation - When showering, reduce the water flow when you do not need it {integer1} {integer2}
    @Override
    public SimpleEntry<Boolean, Integer> recommendReduceFlowWhenNotNeededAmphiro(UUID userKey) {    
        if(messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro() == null){
            return new SimpleEntry<>(false, null);
        }
        
        boolean fireAlert = false;
        double averageMonthlyConsumption = 0;
        DataQueryBuilder durationQueryBuilder = new DataQueryBuilder();
        durationQueryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                .user("user", userKey).amphiro().average();             

        DataQuery durationQuery = durationQueryBuilder.build();
        DataQueryResponse queryResponse = dataService.execute(durationQuery);
        ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();

        if(dataSeriesAmphiro == null || dataSeriesAmphiro.isEmpty()){
            return new SimpleEntry<>(false, null);
        }
        
        for (GroupDataSeries serie : dataSeriesAmphiro) {           
            DataPoint dataPoint = serie.getPoints().get(0);   
            AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
            averageMonthlyConsumption = amphiroDataPoint.getVolume().get(EnumMetric.AVERAGE);                                               
        }             
   
        //TODO - change condition using average flow, if available from aggregates
        if(averageMonthlyConsumption > messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro()){
            Double moreLitersThanOtherPerYear 
                = (averageMonthlyConsumption - messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro())*12;

            return new SimpleEntry<>(fireAlert, moreLitersThanOtherPerYear.intValue());
        }
        else{
            return new SimpleEntry<>(false, null);
        } 
    }            
    
    private int computeConsecutiveZeroConsumptions(List<Double> values) {
        int maxLength = 0;
        int tempLength = 0;
        for(Double value : values){
            
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
