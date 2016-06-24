package eu.daiad.web.model.message;

import eu.daiad.web.service.message.aggregates.AverageMonthlySWM;
import eu.daiad.web.service.message.aggregates.AverageWeeklySWM;
import eu.daiad.web.service.message.aggregates.Top10BaseMonthSWM;
import eu.daiad.web.service.message.aggregates.Top10BaseWeekSWM;
import eu.daiad.web.service.message.aggregates.Top25BaseWeekSWM;
import org.joda.time.DateTime;

public class ConsumptionAggregateContainer {
    
    private int population;
    private AverageMonthlySWM averageMonthlySWM = new AverageMonthlySWM();
    private AverageWeeklySWM averageWeeklySWM = new AverageWeeklySWM();
    private Top10BaseMonthSWM top10BaseMonthSWM = new Top10BaseMonthSWM();
    private Top10BaseWeekSWM top10BaseWeekSWM = new Top10BaseWeekSWM();
    private Top25BaseWeekSWM top25BaseWeekSWM = new Top25BaseWeekSWM();
    
    
	private Double averageMonthlyConsumptionAmphiro = null;    
	private Double averageWeeklyConsumptionAmphiro = null;	
	private Double top10BaseThresholdAmphiro = null;
	private Double averageTemperatureAmphiro = null;
	private Double averageFlowAmphiro = null;
	private Double averageDurationAmphiro = null;
	private Double averageSessionConsumptionAmphiro = null;

	private Integer showerDurationThresholdMinutes = 30;
	private Float temperatureThreshold = 45f;

	//private DateTime lastDateComputed;

	public ConsumptionAggregateContainer() {

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

	public AverageMonthlySWM getAverageMonthlySWM() {
        return averageMonthlySWM;
	}    
    
	public void setAverageMonthlySWM(AverageMonthlySWM averageMonthlySWM) {
        this.averageMonthlySWM = averageMonthlySWM;
	}  
    
	public AverageWeeklySWM getAverageWeeklySWM() {
		return averageWeeklySWM;
	}

	public void setAverageWeeklySWM(AverageWeeklySWM averageWeeklySWM) {
		this.averageWeeklySWM = averageWeeklySWM;
	}
    
	public Float getTemperatureThreshold() {
		return temperatureThreshold;
	}

	public void setTemperatureThreshold(Float temperatureThreshold) {
		this.temperatureThreshold = temperatureThreshold;
	}

	public Top10BaseMonthSWM getTop10BaseMonthSWM() {
		return top10BaseMonthSWM;
	}

	public void setTop10BaseMonthSWM(Top10BaseMonthSWM top10BaseMonthSWM) {
		this.top10BaseMonthSWM = top10BaseMonthSWM;
	}    
    
	public Top10BaseWeekSWM getTop10BaseWeekSWM() {
		return top10BaseWeekSWM;
	}

	public void setTop10BaseWeekSWM(Top10BaseWeekSWM top10BaseWeekSWM) {
		this.top10BaseWeekSWM = top10BaseWeekSWM;
	}
    
	public Double getTop10BaseThresholdAmphiro() {
		return top10BaseThresholdAmphiro;
	}

	public void setTop10BaseThresholdAmphiro(Double top10BaseThresholdAmphiro) {
		this.top10BaseThresholdAmphiro = top10BaseThresholdAmphiro;
	}
    
	public Top25BaseWeekSWM getTop25BaseWeekSWM() {
		return top25BaseWeekSWM;
	}

	public void setTop25BaseWeekSWM(Top25BaseWeekSWM top25BaseWeekSWM) {
		this.top25BaseWeekSWM = top25BaseWeekSWM;
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

	public Double getAverageSessionConsumptionAmphiro() {
		return averageSessionConsumptionAmphiro;
	}

	public void setAverageSessionConsumptionAmphiro(Double averageSessionConsumptionAmphiro) {
		this.averageSessionConsumptionAmphiro = averageSessionConsumptionAmphiro;
	}

//	public DateTime getLastDateComputed() {
//		return lastDateComputed;
//	}
//
//	public void setLastDateComputed(DateTime lastDateComputed) {
//		this.lastDateComputed = lastDateComputed;
//	}

	public void resetValues() {

        if(getAverageMonthlySWM() != null){
            getAverageMonthlySWM().setValue(null);
            getAverageMonthlySWM().setLastComputed(null);            
        }

        if(getAverageWeeklySWM() != null){
            getAverageWeeklySWM().setValue(null);
            getAverageWeeklySWM().setLastComputed(null);            
        }        
        
        if(getTop10BaseMonthSWM() != null){
            getTop10BaseMonthSWM().setValue(null);
            getTop10BaseMonthSWM().setLastComputed(null);            
        } 
        
        if(getTop10BaseWeekSWM() != null){
            getTop10BaseWeekSWM().setValue(null);
            getTop10BaseWeekSWM().setLastComputed(null);            
        } 
        
        if(getTop25BaseWeekSWM() != null){
            getTop25BaseWeekSWM().setValue(null);
            getTop25BaseWeekSWM().setLastComputed(null);            
        }         
        
		setAverageMonthlyConsumptionAmphiro(null);
		setAverageWeeklyConsumptionAmphiro(null);

		setTop10BaseThresholdAmphiro(null);

		setAverageTemperatureAmphiro(null);
		setAverageFlowAmphiro(null);
		setAverageDurationAmphiro(null);
		//setLastDateComputed(null);
	}

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }
    
	@Override
	public String toString() {
		return "MessageAggregatesContainer{" + "\naverageMonthlyConsumptionAmphiro=" + averageMonthlyConsumptionAmphiro
						+ ",\naverageWeeklyConsumptionAmphiro=" + averageWeeklyConsumptionAmphiro
						+ ",\naverageMonthlyConsumptionSWM=" + averageMonthlySWM.getValue()
						+ ",\naverageWeeklyConsumptionSWM=" + averageWeeklySWM.getValue()
						+ ",\ntop10BaseMonthThresholdSWM=" + top10BaseMonthSWM.getValue()
						+ ",\ntop10BaseWeekThresholdSWM=" + top10BaseWeekSWM.getValue() + ",\ntop10BaseThresholdAmphiro="
						+ top10BaseThresholdAmphiro + ",\ntop25BaseWeekThresholdSWM=" + top25BaseWeekSWM.getValue()
						+ ",\naverageTemperatureAmphiro=" + averageTemperatureAmphiro + ",\naverageFlowAmphiro="
						+ averageFlowAmphiro + ",\naverageDurationAmphiro=" + averageDurationAmphiro
						+ ",\nshowerDurationThresholdMinutes=" + showerDurationThresholdMinutes
						//+ ",\ntemperatureThreshold=" + temperatureThreshold + ",\nlastDateComputed=" + lastDateComputed
						+ "\n}";
	}
}
