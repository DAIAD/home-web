package eu.daiad.web.model.message;

import eu.daiad.web.service.message.aggregates.AverageDurationAmphiro;
import eu.daiad.web.service.message.aggregates.AverageFlowAmphiro;
import eu.daiad.web.service.message.aggregates.AverageMonthlyAmphiro;
import eu.daiad.web.service.message.aggregates.AverageMonthlySWM;
import eu.daiad.web.service.message.aggregates.AverageSessionAmphiro;
import eu.daiad.web.service.message.aggregates.AverageTemperatureAmphiro;
import eu.daiad.web.service.message.aggregates.AverageWeeklyAmphiro;
import eu.daiad.web.service.message.aggregates.AverageWeeklySWM;
import eu.daiad.web.service.message.aggregates.Top10BaseMonthAmphiro;
import eu.daiad.web.service.message.aggregates.Top10BaseMonthSWM;
import eu.daiad.web.service.message.aggregates.Top10BaseWeekSWM;
import eu.daiad.web.service.message.aggregates.Top25BaseWeekSWM;

public class ConsumptionAggregateContainer {
    
    private int population;
    private AverageMonthlySWM averageMonthlySWM = new AverageMonthlySWM();
    private AverageWeeklySWM averageWeeklySWM = new AverageWeeklySWM();
    private Top10BaseMonthSWM top10BaseMonthSWM = new Top10BaseMonthSWM();
    private Top10BaseWeekSWM top10BaseWeekSWM = new Top10BaseWeekSWM();
    private Top25BaseWeekSWM top25BaseWeekSWM = new Top25BaseWeekSWM();
    
    private AverageMonthlyAmphiro averageMonthlyAmphiro = new AverageMonthlyAmphiro();
    private AverageWeeklyAmphiro averageWeeklyAmphiro = new AverageWeeklyAmphiro();
    private Top10BaseMonthAmphiro top10BaseMonthAmphiro = new Top10BaseMonthAmphiro();
    private AverageTemperatureAmphiro averageTemperatureAmphiro = new AverageTemperatureAmphiro();
    private AverageSessionAmphiro averageSessionAmphiro = new AverageSessionAmphiro();
    private AverageFlowAmphiro averageFlowAmphiro = new AverageFlowAmphiro();
    private AverageDurationAmphiro averageDurationAmphiro = new AverageDurationAmphiro();

	private Integer showerDurationThresholdMinutes = 30;
	private Float temperatureThreshold = 45f;

	//private DateTime lastDateComputed;

	public ConsumptionAggregateContainer() {

	}

	public AverageMonthlyAmphiro getAverageMonthlyAmphiro() {
		return averageMonthlyAmphiro;
	}

	public void setAverageMonthlyAmphiro(AverageMonthlyAmphiro averageMonthlyAmphiro) {
		this.averageMonthlyAmphiro = averageMonthlyAmphiro;
	}
    
	public AverageWeeklyAmphiro getAverageWeeklyAmphiro() {
		return averageWeeklyAmphiro;
	}

	public void setAverageWeeklyAmphiro(AverageWeeklyAmphiro averageWeeklyAmphiro) {
		this.averageWeeklyAmphiro = averageWeeklyAmphiro;
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
    
	public Top10BaseMonthAmphiro getTop10BaseMonthAmphiro() {
		return top10BaseMonthAmphiro;
	}

	public void setTop10BaseMonthAmphiro(Top10BaseMonthAmphiro top10BaseMonthAmphiro) {
		this.top10BaseMonthAmphiro = top10BaseMonthAmphiro;
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

	public AverageTemperatureAmphiro getAverageTemperatureAmphiro() {
		return averageTemperatureAmphiro;
	}

	public void setAverageTemperatureAmphiro(AverageTemperatureAmphiro averageTemperatureAmphiro) {
		this.averageTemperatureAmphiro = averageTemperatureAmphiro;
	}

	public AverageFlowAmphiro getAverageFlowAmphiro() {
		return averageFlowAmphiro;
	}

	public void setAverageFlowAmphiro(AverageFlowAmphiro averageFlowAmphiro) {
		this.averageFlowAmphiro = averageFlowAmphiro;
	}

	public AverageDurationAmphiro getAverageDurationAmphiro() {
		return averageDurationAmphiro;
	}

	public void setAverageDurationAmphiro(AverageDurationAmphiro averageDurationAmphiro) {
		this.averageDurationAmphiro = averageDurationAmphiro;
	}

	public AverageSessionAmphiro getAverageSessionAmphiro() {
		return averageSessionAmphiro;
	}

	public void setAverageSessionAmphiro(AverageSessionAmphiro averageSessionAmphiro) {
		this.averageSessionAmphiro = averageSessionAmphiro;
	}

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
        
        if(getAverageMonthlyAmphiro() != null){
            getAverageMonthlyAmphiro().setValue(null);
            getAverageMonthlyAmphiro().setLastComputed(null);            
        }          

        if(getAverageWeeklyAmphiro() != null){
            getAverageWeeklyAmphiro().setValue(null);
            getAverageWeeklyAmphiro().setLastComputed(null);            
        } 
        
        if(getTop10BaseMonthAmphiro() != null){
            getTop10BaseMonthAmphiro().setValue(null);
            getTop10BaseMonthAmphiro().setLastComputed(null);            
        } 

        if(getAverageTemperatureAmphiro() != null){
            getAverageTemperatureAmphiro().setValue(null);
            getAverageTemperatureAmphiro().setLastComputed(null);            
        }
        
        if(getAverageSessionAmphiro() != null){
            getAverageSessionAmphiro().setValue(null);
            getAverageSessionAmphiro().setLastComputed(null);            
        }

        if(getAverageFlowAmphiro() != null){
            getAverageFlowAmphiro().setValue(null);
            getAverageFlowAmphiro().setLastComputed(null);            
        }        

        if(getAverageDurationAmphiro() != null){
            getAverageDurationAmphiro().setValue(null);
            getAverageDurationAmphiro().setLastComputed(null);            
        }
	}

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }
    
	@Override
	public String toString() {
		return "MessageAggregatesContainer{" 
						+ "\naverageMonthlyConsumptionSWM=" + averageMonthlySWM.getValue()
						+ ",\naverageWeeklyConsumptionSWM=" + averageWeeklySWM.getValue()
						+ ",\ntop10BaseMonthThresholdSWM=" + top10BaseMonthSWM.getValue()
						+ ",\ntop10BaseWeekThresholdSWM=" + top10BaseWeekSWM.getValue() 
                        + ",\ntop25BaseWeekThresholdSWM=" + top25BaseWeekSWM.getValue()                
                        + "\naverageMonthlyConsumptionAmphiro=" + averageMonthlyAmphiro.getValue()
						+ ",\naverageWeeklyConsumptionAmphiro=" + averageWeeklyAmphiro.getValue()
                        + ",\ntop10BaseThresholdAmphiro=" + top10BaseMonthAmphiro.getValue()
						+ ",\naverageTemperatureAmphiro=" + averageTemperatureAmphiro.getValue()
                        + ",\naverageSessionConsumptionAmphiro=" + averageSessionAmphiro.getValue()
                        + ",\naverageFlowAmphiro=" + averageFlowAmphiro.getValue()
                        + ",\naverageDurationAmphiro=" + averageDurationAmphiro.getValue()
						+ ",\nshowerDurationThresholdMinutes=" + showerDurationThresholdMinutes
                        + ",\ntemperatureThresholdCelsius=" + temperatureThreshold
						+ "\n}";
	}
}
