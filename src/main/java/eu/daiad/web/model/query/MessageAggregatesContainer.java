package eu.daiad.web.model.query;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 *
 * @author nkarag
 */
@Component
public class MessageAggregatesContainer {

    private Double averageMonthlyConsumptionAmphiro = null;
    private Double averageWeeklyConsumptionAmphiro = null;
    private Double averageMonthlyConsumptionSWM = null;
    private Double averageWeeklyConsumptionSWM = null;
    private Double top10BaseMonthThresholdSWM = null;
    private Double top10BaseWeekThresholdSWM = null;

    private Double top10BaseThresholdAmphiro = null;
    private Double top25BaseWeekThresholdSWM = null;
    private Double averageTemperatureAmphiro = null;
    private Double averageFlowAmphiro = null;
    private Double averageDurationAmphiro = null;
    
    private Integer showerDurationThresholdMinutes = 30;
    private Float temperatureThreshold = 45f;
    
    private DateTime lastDateComputed;

    public MessageAggregatesContainer(){
        lastDateComputed = DateTime.now();
    }   

    public Double getAverageMonthlyConsumptionAmphiro() {
        return averageMonthlyConsumptionAmphiro;
    }

    public void setAverageMonthlyConsumptionAmphiro(Double averageMonthlyConsumptionAmphiro) {
        this.averageMonthlyConsumptionAmphiro = averageMonthlyConsumptionAmphiro;
    }
    
    public Double getAverageWeeklyConsumptionAmphiro() {
        return averageWeeklyConsumptionAmphiro;
    }

    public void setAverageWeeklyConsumptionAmphiro(Double averageWeeklyConsumptionAmphiro) {
        this.averageWeeklyConsumptionAmphiro = averageWeeklyConsumptionAmphiro;
    }    

    public Double getAverageMonthlyConsumptionSWM() {
        return averageMonthlyConsumptionSWM;
    }

    public void setAverageMonthlyConsumptionSWM(Double averageMonthlyConsumptionSWM) {
        this.averageMonthlyConsumptionSWM = averageMonthlyConsumptionSWM;
    }

    public Double getAverageWeeklyConsumptionSWM() {
        return averageWeeklyConsumptionSWM;
    }

    public void setAverageWeeklyConsumptionSWM(Double averageWeeklyConsumptionSWM) {
        this.averageWeeklyConsumptionSWM = averageWeeklyConsumptionSWM;
    }

    public Float getTemperatureThreshold() {
        return temperatureThreshold;
    }

    public void setTemperatureThreshold(Float temperatureThreshold) {
        this.temperatureThreshold = temperatureThreshold;
    }    

    public Double getTop10BaseMonthThresholdSWM() {
        return top10BaseMonthThresholdSWM;
    }

    public void setTop10BaseMonthThresholdSWM(Double top10BaseMonthThresholdSWM) {
        this.top10BaseMonthThresholdSWM = top10BaseMonthThresholdSWM;
    }

    public Double getTop10BaseWeekThresholdSWM() {
        return top10BaseWeekThresholdSWM;
    }

    public void setTop10BaseWeekThresholdSWM(Double top10BaseWeekThresholdSWM) {
        this.top10BaseWeekThresholdSWM = top10BaseWeekThresholdSWM;
    }

    public Double getTop10BaseThresholdAmphiro() {
        return top10BaseThresholdAmphiro;
    }

    public void setTop10BaseThresholdAmphiro(Double top10BaseThresholdAmphiro) {
        this.top10BaseThresholdAmphiro = top10BaseThresholdAmphiro;
    }
    
    public Double getTop25BaseWeekThresholdSWM() {
        return top25BaseWeekThresholdSWM;
    }

    public void setTop25BaseWeekThresholdSWM(Double top25BaseWeekThresholdSWM) {
        this.top25BaseWeekThresholdSWM = top25BaseWeekThresholdSWM;
    }
    
    public Integer getShowerDurationThresholdMinutes() {
        return showerDurationThresholdMinutes;
    }

    public void setShowerDurationThresholdMinutes(Integer showerDurationThresholdMinutes) {
        this.showerDurationThresholdMinutes = showerDurationThresholdMinutes;
    }    

    public Double getAverageTemperatureAmphiro() {
        return averageTemperatureAmphiro;
    }

    public void setAverageTemperatureAmphiro(Double averageTemperatureAmphiro) {
        this.averageTemperatureAmphiro = averageTemperatureAmphiro;
    }

    public Double getAverageFlowAmphiro() {
        return averageFlowAmphiro;
    }

    public void setAverageFlowAmphiro(Double averageFlowAmphiro) {
        this.averageFlowAmphiro = averageFlowAmphiro;
    }

    public Double getAverageDurationAmphiro() {
        return averageDurationAmphiro;
    }

    public void setAverageDurationAmphiro(Double averageDurationAmphiro) {
        this.averageDurationAmphiro = averageDurationAmphiro;
    }

    public DateTime getLastDateComputed(){
        return lastDateComputed;
    }
    
    public void setLastDateComputed(DateTime lastDateComputed){
        this.lastDateComputed = lastDateComputed;
    }
    
    public void resetValues(){
        
        setAverageMonthlyConsumptionAmphiro(null);
        setAverageWeeklyConsumptionAmphiro(null);
        setAverageMonthlyConsumptionSWM(null);
        setTop10BaseMonthThresholdSWM(null);
        setTop10BaseWeekThresholdSWM(null);
        setTop10BaseThresholdAmphiro(null);
        setTop25BaseWeekThresholdSWM(null);        
        setAverageTemperatureAmphiro(null);
        setAverageFlowAmphiro(null);
        setAverageDurationAmphiro(null);
        setLastDateComputed(null);

    }
    
    @Override
    public String toString() {
        return "MessageAggregatesContainer{" 
                + "\naverageMonthlyConsumptionAmphiro=" + averageMonthlyConsumptionAmphiro 
                + ",\naverageWeeklyConsumptionAmphiro=" + averageWeeklyConsumptionAmphiro 
                + ",\naverageMonthlyConsumptionSWM=" + averageMonthlyConsumptionSWM 
                + ",\naverageWeeklyConsumptionSWM=" + averageWeeklyConsumptionSWM 
                + ",\ntop10BaseMonthThresholdSWM=" + top10BaseMonthThresholdSWM 
                + ",\ntop10BaseWeekThresholdSWM=" + top10BaseWeekThresholdSWM 
                + ",\ntop10BaseThresholdAmphiro=" + top10BaseThresholdAmphiro 
                + ",\ntop25BaseWeekThresholdSWM=" + top25BaseWeekThresholdSWM 
                + ",\naverageTemperatureAmphiro=" + averageTemperatureAmphiro 
                + ",\naverageFlowAmphiro=" + averageFlowAmphiro 
                + ",\naverageDurationAmphiro=" + averageDurationAmphiro 
                + ",\nshowerDurationThresholdMinutes=" + showerDurationThresholdMinutes 
                + ",\ntemperatureThreshold=" + temperatureThreshold 
                + ",\nlastDateComputed=" + lastDateComputed + "\n}";
    }   
}
