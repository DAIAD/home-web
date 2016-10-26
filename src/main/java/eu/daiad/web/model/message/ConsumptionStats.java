package eu.daiad.web.model.message;

import eu.daiad.web.service.message.aggregates.ComputedNumber;

public class ConsumptionStats {
    
    private final int utilityId;
    
    // Todo Document units for various computed numbers
    
    private ComputedNumber averageMonthlySWM;
    private ComputedNumber averageWeeklySWM;
    private ComputedNumber top10BaseMonthSWM;
    private ComputedNumber top10BaseWeekSWM;
    private ComputedNumber top25BaseWeekSWM;
    
    private ComputedNumber averageMonthlyAmphiro;
    private ComputedNumber averageWeeklyAmphiro;
    private ComputedNumber top10BaseMonthAmphiro;
    private ComputedNumber averageTemperatureAmphiro;
    private ComputedNumber averageSessionAmphiro;
    private ComputedNumber averageFlowAmphiro;
    private ComputedNumber averageDurationAmphiro;

	public ConsumptionStats(int utilityId) {
	    this.utilityId = utilityId;
	}

	public ComputedNumber getAverageMonthlyAmphiro() {
		return averageMonthlyAmphiro;
	}

	public void setAverageMonthlyAmphiro(ComputedNumber averageMonthlyAmphiro) {
		this.averageMonthlyAmphiro = averageMonthlyAmphiro;
	}
    
	public ComputedNumber getAverageWeeklyAmphiro() {
		return averageWeeklyAmphiro;
	}

	public void setAverageWeeklyAmphiro(ComputedNumber averageWeeklyAmphiro) {
		this.averageWeeklyAmphiro = averageWeeklyAmphiro;
	}
    
	public ComputedNumber getAverageMonthlySWM() {
        return averageMonthlySWM;
	}    
    
	public void setAverageMonthlySWM(ComputedNumber averageMonthlySWM) {
        this.averageMonthlySWM = averageMonthlySWM;
	}  
    
	public ComputedNumber getAverageWeeklySWM() {
		return averageWeeklySWM;
	}

	public void setAverageWeeklySWM(ComputedNumber averageWeeklySWM) {
		this.averageWeeklySWM = averageWeeklySWM;
	}
    
	public ComputedNumber getTop10BaseMonthSWM() {
		return top10BaseMonthSWM;
	}

	public void setTop10BaseMonthSWM(ComputedNumber top10BaseMonthSWM) {
		this.top10BaseMonthSWM = top10BaseMonthSWM;
	}    
    
	public ComputedNumber getTop10BaseWeekSWM() {
		return top10BaseWeekSWM;
	}

	public void setTop10BaseWeekSWM(ComputedNumber top10BaseWeekSWM) {
		this.top10BaseWeekSWM = top10BaseWeekSWM;
	}
    
	public ComputedNumber getTop10BaseMonthAmphiro() {
		return top10BaseMonthAmphiro;
	}

	public void setTop10BaseMonthAmphiro(ComputedNumber top10BaseMonthAmphiro) {
		this.top10BaseMonthAmphiro = top10BaseMonthAmphiro;
	}
    
	public ComputedNumber getTop25BaseWeekSWM() {
		return top25BaseWeekSWM;
	}

	public void setTop25BaseWeekSWM(ComputedNumber top25BaseWeekSWM) {
		this.top25BaseWeekSWM = top25BaseWeekSWM;
	}

	public ComputedNumber getAverageTemperatureAmphiro() {
		return averageTemperatureAmphiro;
	}

	public void setAverageTemperatureAmphiro(ComputedNumber averageTemperatureAmphiro) {
		this.averageTemperatureAmphiro = averageTemperatureAmphiro;
	}

	public ComputedNumber getAverageFlowAmphiro() {
		return averageFlowAmphiro;
	}

	public void setAverageFlowAmphiro(ComputedNumber averageFlowAmphiro) {
		this.averageFlowAmphiro = averageFlowAmphiro;
	}

	public ComputedNumber getAverageDurationAmphiro() {
		return averageDurationAmphiro;
	}

	public void setAverageDurationAmphiro(ComputedNumber averageDurationAmphiro) {
		this.averageDurationAmphiro = averageDurationAmphiro;
	}

	public ComputedNumber getAverageSessionAmphiro() {
		return averageSessionAmphiro;
	}

	public void setAverageSessionAmphiro(ComputedNumber averageSessionAmphiro) {
		this.averageSessionAmphiro = averageSessionAmphiro;
	}

	public void resetValues() {

        if (averageMonthlySWM != null)
            averageMonthlySWM.reset();
        
        if (averageWeeklySWM != null)
            averageWeeklySWM.reset();
        
        if (top10BaseMonthSWM != null)
            top10BaseMonthSWM.reset(); 
       
        if (top10BaseWeekSWM != null)
            top10BaseWeekSWM.reset();

        if (top25BaseWeekSWM != null)
            top25BaseWeekSWM.reset();
        
        if (averageMonthlyAmphiro != null)
            averageMonthlyAmphiro.reset();

        if (averageWeeklyAmphiro != null)
            averageWeeklyAmphiro.reset();
        
        if (top10BaseMonthAmphiro != null)
            top10BaseMonthAmphiro.reset();

        if (averageTemperatureAmphiro != null) 
            averageTemperatureAmphiro.reset();
        
        if (averageSessionAmphiro != null)
            averageSessionAmphiro.reset();
          
        if (averageFlowAmphiro != null)
            averageFlowAmphiro.reset();      

        if (averageDurationAmphiro != null)
            averageDurationAmphiro.reset();
	}
    
    public int getUtilityId() {
        return utilityId;
    }

	@Override
	public String toString() {
		return "\nMessage-service stats: (Utility #" + utilityId + ")\n" 
						+ "\n    Average Monthly Consumption (SWM) = " + averageMonthlySWM.getValue()
						+ "\n    Average Weekly Consumption (SWM) = " + averageWeeklySWM.getValue()
						+ "\n    Top10 base month threshold (SWM) = " + top10BaseMonthSWM.getValue()
						+ "\n    Top10 base week threshold (SWM) = " + top10BaseWeekSWM.getValue() 
                        + "\n    Top25 base week threshold (SWM) = " + top25BaseWeekSWM.getValue()                
                        + "\n    Average Monthly Consumption (Amphiro) = " + averageMonthlyAmphiro.getValue()
						+ "\n    Average Weekly Consumption (Amphiro) = " + averageWeeklyAmphiro.getValue()
                        + "\n    Top10 base threshold (Amphiro) = " + top10BaseMonthAmphiro.getValue()
						+ "\n    Average temperature (Amphiro) = " + averageTemperatureAmphiro.getValue()
                        + "\n    Average session consumption (Amphiro) = " + averageSessionAmphiro.getValue()
                        + "\n    Average flow (Amphiro) = " + averageFlowAmphiro.getValue()
                        + "\n    Average duration (Amphiro) = " + averageDurationAmphiro.getValue()
						+ "\n";
	}
}
