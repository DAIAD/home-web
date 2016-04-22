package eu.daiad.web.service;

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
import eu.daiad.web.model.recommendation.MessageCalculationConfiguration;
import eu.daiad.web.repository.application.IUserRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Aggregates computation service. 
 * 
 * @author nkarag
 */
//TODO - define some sanity values for checking the produced results

@Service
public class MessageAggregatesService implements IAggregatesService {

    @Autowired
    MessageAggregatesContainer messageAggregatesContainer;

    @Autowired
    IUserRepository iUserRepository;
    
    @Autowired
    IDataService dataService;
    
    private MessageCalculationConfiguration config;
    private boolean cancelled = false;
    private boolean isRunning = true;
    
    @Override
    public MessageAggregatesContainer execute(MessageCalculationConfiguration config) {
        isRunning = true;
        this.config = config;
        computeAverageMonthlyConsumptionAmphiro(); 
        computeAverageWeeklyConsumptionAmphiro();
        computeAverageMonthlyConsumptionSWM();
        computeAverageWeeklyConsumptionSWM();
        computeTop10MonthlyConsumptionThresholdSWM();
        computeTop10WeeklyConsumptionThresholdSWM();
        computeTop25WeeklyConsumptionThresholdSWM();
        computeTop10MonthlyConsumptionThresholdAmphiro();
        computeAverageTemperatureAmphiro();
        computeAverageDurationAmphiro();
        
        isRunning = false;
        return messageAggregatesContainer;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public boolean isRunning() {
        return isRunning;
    }    
    
    private void computeAverageMonthlyConsumptionAmphiro() {
        if(isCancelled()){
            return;
        }
        if (messageAggregatesContainer.getAverageMonthlyConsumptionAmphiro() == null 
                || messageAggregatesContainer.getLastDateComputed()
                        .isBefore(DateTime.now().minusDays(config.getAggregateComputationDaysInterval()))) {
 
            List<UUID> uuidList = iUserRepository.getUserKeysForUtility();
            UUID[] userUUIDs = ((List<UUID>)uuidList).toArray(new UUID[uuidList.size()]);            
  
            DataQueryBuilder queryBuilder = new DataQueryBuilder();
            queryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                    .users("utility", userUUIDs).amphiro().sum();             
                       
            DataQuery query = queryBuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();
            
            for (GroupDataSeries serie : dataSeriesAmphiro) {
                if(serie.getPopulation() == 0){
                    return;
                }
                
                if(!serie.getPoints().isEmpty()){ //check for non existent data 
                    ArrayList<DataPoint> points = serie.getPoints();
                    DataPoint dataPoint = points.get(0);
                    AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
                    Map<EnumMetric, Double> ma = amphiroDataPoint.getVolume();
                    Double tempAverageMonthlyConsumptionAmphiro = ma.get(EnumMetric.SUM);
                    messageAggregatesContainer.setAverageMonthlyConsumptionAmphiro
                                    (tempAverageMonthlyConsumptionAmphiro/serie.getPopulation());    
                    messageAggregatesContainer.setLastDateComputed(DateTime.now()); 
                }
                else{
                    messageAggregatesContainer.setAverageMonthlyConsumptionAmphiro(null);
                }
            }
        }
    }    
    
    private void computeAverageWeeklyConsumptionAmphiro() {
        if(isCancelled()){
            return;
        }
        if (messageAggregatesContainer.getAverageWeeklyConsumptionAmphiro() == null 
                || messageAggregatesContainer.getLastDateComputed()
                        .isBefore(DateTime.now().minusDays(config.getAggregateComputationDaysInterval()))) {

            List<UUID> uuidList = iUserRepository.getUserKeysForUtility();  
            UUID[] userUUIDs = ((List<UUID>)uuidList).toArray(new UUID[uuidList.size()]);            
  
            DataQueryBuilder queryBuilder = new DataQueryBuilder();
            queryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                    .users("utility", userUUIDs).amphiro().sum();                         
            
            DataQuery query = queryBuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();
            
            for (GroupDataSeries serie : dataSeriesAmphiro) {
                if(serie.getPopulation() == 0){
                    return;
                }
                
                if(!serie.getPoints().isEmpty()){ //check for non existent data 
                    ArrayList<DataPoint> points = serie.getPoints();
                    DataPoint dataPoint = points.get(0);
                    AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;
                    Map<EnumMetric, Double> ma = amphiroDataPoint.getVolume();
                    Double averageWeeklyAmphiro = ma.get(EnumMetric.SUM);
                    
                    messageAggregatesContainer
                            .setAverageWeeklyConsumptionAmphiro(averageWeeklyAmphiro/serie.getPopulation());    
                    messageAggregatesContainer.setLastDateComputed(DateTime.now()); 
                }
                else{
                    messageAggregatesContainer.setAverageWeeklyConsumptionAmphiro(null);
                }
            }
        }
    }     
    
    private void computeAverageWeeklyConsumptionSWM() {
        if(isCancelled()){
            return;
        }
        if (messageAggregatesContainer.getAverageWeeklyConsumptionSWM() == null 
                || messageAggregatesContainer.getLastDateComputed()
                        .isBefore(DateTime.now().minusDays(config.getAggregateComputationDaysInterval()))) {

            List<UUID> uuidList = iUserRepository.getUserKeysForUtility();  
            UUID[] userUUIDs = ((List<UUID>)uuidList).toArray(new UUID[uuidList.size()]);            

            DataQueryBuilder queryBuilder = new DataQueryBuilder();
            queryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                    .users("utility", userUUIDs).meter().sum();         
                        
            DataQuery query = queryBuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();
            
            for (GroupDataSeries serie : dataSeriesMeter) {
                if(serie.getPopulation() == 0){
                    return;
                }                       
                if(!serie.getPoints().isEmpty()){ //check for non existent data 
                    ArrayList<DataPoint> points = serie.getPoints();
                    DataPoint dataPoint = points.get(0);
                    MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                    Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                    messageAggregatesContainer.setAverageWeeklyConsumptionSWM
                                        (metricsMap.get(EnumMetric.SUM)/serie.getPopulation());                    
                    messageAggregatesContainer.setLastDateComputed(DateTime.now());                 
                }
                else{
                    messageAggregatesContainer.setAverageWeeklyConsumptionSWM(null);
                }
            }
        }
    }

    private void computeAverageMonthlyConsumptionSWM() {
        if(isCancelled()){
            return;
        }
        if (messageAggregatesContainer.getAverageMonthlyConsumptionSWM() == null 
                || messageAggregatesContainer.getLastDateComputed()
                        .isBefore(DateTime.now().minusDays(config.getAggregateComputationDaysInterval()))) {

            List<UUID> uuidList = iUserRepository.getUserKeysForUtility();  
            UUID[] userUUIDs = ((List<UUID>)uuidList).toArray(new UUID[uuidList.size()]);            

            DataQueryBuilder queryBuilder = new DataQueryBuilder();
            queryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                    .users("utility", userUUIDs).meter().sum();             
            
            DataQuery query = queryBuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();
            
            for (GroupDataSeries serie : dataSeriesMeter) {
                if(serie.getPopulation() == 0){
                    return;
                }        
                if(!serie.getPoints().isEmpty()){ //check for non existent data 
                    ArrayList<DataPoint> points = serie.getPoints();
                    DataPoint dataPoint = points.get(0);
                    MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                    Map<EnumMetric, Double> ma = meterDataPoint.getVolume();
                    messageAggregatesContainer
                            .setAverageMonthlyConsumptionSWM(ma.get(EnumMetric.SUM)/serie.getPopulation());   
                    messageAggregatesContainer.setLastDateComputed(DateTime.now()); 
                }
                else{
                    messageAggregatesContainer.setAverageMonthlyConsumptionSWM(null);
                }
            }
        }
    }
    
    private void computeTop10MonthlyConsumptionThresholdSWM() {
        if(isCancelled()){
            return;
        }        
        //Compute average for all users.
        //Sort consumptions. The base threshold is the consumption of the last user of the top10%.
        if (messageAggregatesContainer.getTop10BaseMonthThresholdSWM() == null 
                || messageAggregatesContainer.getLastDateComputed()
                        .isBefore(DateTime.now().minusDays(config.getAggregateComputationDaysInterval()))) {
            
            List<UUID> uuidList = iUserRepository.getUserKeysForUtility();  
            UUID[] userUUIDs = ((List<UUID>)uuidList).toArray(new UUID[uuidList.size()]);
            List<Double> averageConsumptions = new ArrayList<>();
                
            DataQueryBuilder queryBuilder = new DataQueryBuilder();
            queryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                    .users("utility", userUUIDs).meter().sum();            

            DataQuery query = queryBuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();  
            
            for (GroupDataSeries serie : dataSeriesMeter) {
                if(serie.getPopulation() == 0){
                    return;
                }                
                if(!serie.getPoints().isEmpty()){ //check for non existent data 
                    ArrayList<DataPoint> userPoints = serie.getPoints();
                    DataPoint dataPoint = userPoints.get(0);
                    MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                    Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                    averageConsumptions.add(metricsMap.get(EnumMetric.SUM)/serie.getPopulation());   
                }
            }                
            if(!averageConsumptions.isEmpty()){
                Collections.sort(averageConsumptions);
                int top10BaseIndex = (int) (averageConsumptions.size()*10)/100;
                Double consumptionThresholdTop10 = averageConsumptions.get(top10BaseIndex);
                messageAggregatesContainer.setTop10BaseMonthThresholdSWM(consumptionThresholdTop10);
                messageAggregatesContainer.setLastDateComputed(DateTime.now());                
            }
            else{
                messageAggregatesContainer.setTop10BaseMonthThresholdSWM(null);
            }           
        }
    }    

    private void computeTop10WeeklyConsumptionThresholdSWM() {
        if(isCancelled()){
            return;
        }        
        if (messageAggregatesContainer.getTop10BaseWeekThresholdSWM() == null 
                || messageAggregatesContainer.getLastDateComputed()
                        .isBefore(DateTime.now().minusDays(config.getAggregateComputationDaysInterval()))) {
            
            List<UUID> uuidList = iUserRepository.getUserKeysForUtility();  
            UUID[] userUUIDs = ((List<UUID>)uuidList).toArray(new UUID[uuidList.size()]);
            List<Double> averageConsumptions = new ArrayList<>();
                
            DataQueryBuilder queryBuilder = new DataQueryBuilder();
            queryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                    .users("utility", userUUIDs).meter().sum();            

            DataQuery query = queryBuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();  
            
            for (GroupDataSeries serie : dataSeriesMeter) {
                if(serie.getPopulation() == 0){
                    return;
                }                
                if(!serie.getPoints().isEmpty()){ //check for non existent data 
                    ArrayList<DataPoint> userPoints = serie.getPoints();
                    DataPoint dataPoint = userPoints.get(0);
                    MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                    Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                    averageConsumptions.add(metricsMap.get(EnumMetric.SUM)/serie.getPopulation());   
                }
            }                
            if(!averageConsumptions.isEmpty()){
                Collections.sort(averageConsumptions);
                int top10BaseIndex = (int) (averageConsumptions.size()*10)/100;
                Double consumptionThresholdTop10 = averageConsumptions.get(top10BaseIndex);
                messageAggregatesContainer.setTop10BaseWeekThresholdSWM(consumptionThresholdTop10);
                messageAggregatesContainer.setLastDateComputed(DateTime.now());                
            }
            else{
                messageAggregatesContainer.setTop10BaseWeekThresholdSWM(null);
            }           
        }
    }
        
    private void computeTop10MonthlyConsumptionThresholdAmphiro() { 
        if(isCancelled()){
            return;
        }        
        if (messageAggregatesContainer.getTop10BaseThresholdAmphiro() == null 
                || messageAggregatesContainer.getLastDateComputed()
                        .isBefore(DateTime.now().minusDays(config.getAggregateComputationDaysInterval()))) {
            
            List<UUID> uuidList = iUserRepository.getUserKeysForUtility();  
            UUID[] userUUIDs = ((List<UUID>)uuidList).toArray(new UUID[uuidList.size()]);
            List<Double> averageConsumptions = new ArrayList<>();

            DataQueryBuilder queryBuilder = new DataQueryBuilder();
            queryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                    .users("utility", userUUIDs).amphiro().sum();            

            DataQuery query = queryBuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();  
            
            for (GroupDataSeries serie : dataSeriesAmphiro) {
                if(serie.getPopulation() == 0){
                    return;
                }                
                if(!serie.getPoints().isEmpty()){
                    ArrayList<DataPoint> userPoints = serie.getPoints();
                    DataPoint dataPoint = userPoints.get(0);
                    AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) dataPoint;                    
                    Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getVolume();                   
                    Double tempAverageMonthlyConsumptionAmphiro 
                            = metricsMap.get(EnumMetric.SUM)/serie.getPopulation();
                    averageConsumptions.add(tempAverageMonthlyConsumptionAmphiro);   
                }
            }                
            if(!averageConsumptions.isEmpty()){
                Collections.sort(averageConsumptions);
                int top10BaseIndex = (int) (averageConsumptions.size()*10)/100;
                Double consumptionThresholdTop10 = averageConsumptions.get(top10BaseIndex);
                messageAggregatesContainer.setTop10BaseThresholdAmphiro(consumptionThresholdTop10);
                messageAggregatesContainer.setLastDateComputed(DateTime.now());
            }
            else{
                messageAggregatesContainer.setTop10BaseThresholdAmphiro(null);
            }            
        }
    }    
 
    private void computeTop25WeeklyConsumptionThresholdSWM(){
        if(isCancelled()){
            return;
        }        
        if (messageAggregatesContainer.getTop10BaseWeekThresholdSWM() == null 
                || messageAggregatesContainer.getLastDateComputed()
                        .isBefore(DateTime.now().minusDays(config.getAggregateComputationDaysInterval()))) {
            
            List<UUID> uuidList = iUserRepository.getUserKeysForUtility();  
            UUID[] userUUIDs = ((List<UUID>)uuidList).toArray(new UUID[uuidList.size()]);
            List<Double> averageConsumptions = new ArrayList<>();
                
            DataQueryBuilder queryBuilder = new DataQueryBuilder();
            queryBuilder.sliding(-7, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                    .users("utility", userUUIDs).meter().sum();            

            DataQuery query = queryBuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            ArrayList<GroupDataSeries> dataSeriesMeter = queryResponse.getMeters();  
            
            for (GroupDataSeries serie : dataSeriesMeter) {
                if(serie.getPopulation() == 0){
                    return;
                }                
                if(!serie.getPoints().isEmpty()){ //check for non existent data 
                    ArrayList<DataPoint> userPoints = serie.getPoints();
                    DataPoint dataPoint = userPoints.get(0);
                    MeterDataPoint meterDataPoint = (MeterDataPoint) dataPoint;
                    Map<EnumMetric, Double> metricsMap = meterDataPoint.getVolume();
                    averageConsumptions.add(metricsMap.get(EnumMetric.SUM)/serie.getPopulation());   
                }
            }                
            if(!averageConsumptions.isEmpty()){
                Collections.sort(averageConsumptions);
                int top25BaseIndex = (int) (averageConsumptions.size()*25)/100;
                Double consumptionThresholdTop25 = averageConsumptions.get(top25BaseIndex);
                messageAggregatesContainer.setTop25BaseWeekThresholdSWM(consumptionThresholdTop25);
                messageAggregatesContainer.setLastDateComputed(DateTime.now());                
            }
            else{
                messageAggregatesContainer.setTop25BaseWeekThresholdSWM(null);
            }
            
        }        
    }
    
    private void computeAverageTemperatureAmphiro(){
        if(isCancelled()){
            return;
        }        
        if (messageAggregatesContainer.getAverageTemperatureAmphiro() == null 
                || messageAggregatesContainer.getLastDateComputed()
                        .isBefore(DateTime.now().minusDays(config.getAggregateComputationDaysInterval()))) {  
            
            List<UUID> uuidList = iUserRepository.getUserKeysForUtility();  
            UUID[] userUUIDs = ((List<UUID>)uuidList).toArray(new UUID[uuidList.size()]);            
  
            DataQueryBuilder queryBuilder = new DataQueryBuilder();
            queryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                    .users("utility", userUUIDs).amphiro().average();             
                       
            DataQuery query = queryBuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();
            
            for (GroupDataSeries serie : dataSeriesAmphiro) {
                if(serie.getPopulation() == 0){
                    return;
                }
                
                if(!serie.getPoints().isEmpty()){ //check for non existent data 
                    ArrayList<DataPoint> point = serie.getPoints();                   
                    AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) point.get(0);                   
                    Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getTemperature();                    
                    Double averageTemperatureAmphiro = metricsMap.get(EnumMetric.AVERAGE);
                    
                    messageAggregatesContainer.setAverageTemperatureAmphiro(averageTemperatureAmphiro);    
                    messageAggregatesContainer.setLastDateComputed(DateTime.now()); 
                }
                else{
                    messageAggregatesContainer.setAverageTemperatureAmphiro(null);
                }
            }                   
        }
    }
    
    private void computeAverageDurationAmphiro(){
        if(isCancelled()){
            return;
        }        
        if (messageAggregatesContainer.getAverageDurationAmphiro() == null 
                || messageAggregatesContainer.getLastDateComputed()
                        .isBefore(DateTime.now().minusDays(config.getAggregateComputationDaysInterval()))) {  
            
            List<UUID> uuidList = iUserRepository.getUserKeysForUtility();  
            UUID[] userUUIDs = ((List<UUID>)uuidList).toArray(new UUID[uuidList.size()]);            
  
            DataQueryBuilder queryBuilder = new DataQueryBuilder();
            queryBuilder.sliding(-30, EnumTimeUnit.DAY, EnumTimeAggregation.ALL)
                    .users("utility", userUUIDs).amphiro().average();             
                       
            DataQuery query = queryBuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            ArrayList<GroupDataSeries> dataSeriesAmphiro = queryResponse.getDevices();
            
            for (GroupDataSeries serie : dataSeriesAmphiro) {
                if(serie.getPopulation() == 0){
                    return;
                }
                
                if(!serie.getPoints().isEmpty()){ //check for non existent data 
                    ArrayList<DataPoint> point = serie.getPoints();                   
                    AmphiroDataPoint amphiroDataPoint = (AmphiroDataPoint) point.get(0);                   
                    Map<EnumMetric, Double> metricsMap = amphiroDataPoint.getDuration();                    
                    Double averageDurationAmphiro = metricsMap.get(EnumMetric.AVERAGE);
                    
                    messageAggregatesContainer.setAverageDurationAmphiro(averageDurationAmphiro);    
                    messageAggregatesContainer.setLastDateComputed(DateTime.now()); 
                }
                else{
                    messageAggregatesContainer.setAverageDurationAmphiro(null);
                }
            }                   
        }
    }       
}
