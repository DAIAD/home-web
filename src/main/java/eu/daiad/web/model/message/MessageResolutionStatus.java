package eu.daiad.web.model.message;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import eu.daiad.web.model.device.EnumDeviceType;

public class MessageResolutionStatus {

	private boolean alertWaterLeakSWM;

	private boolean alertShowerStillOnAmphiro;

	private boolean alertWaterQualitySWM;

	private boolean alertHotTemperatureAmphiro;

	// liters used, liters remaining
	private SimpleEntry<Integer, Integer> alertNearDailyBudgetSWM;

	// liters used, liters remaining
	private SimpleEntry<Integer, Integer> alertNearWeeklyBudgetSWM;

	// liters used, liters remaining
	private SimpleEntry<Integer, Integer> alertNearDailyBudgetAmphiro;

	// liters used, liters remaining
	private SimpleEntry<Integer, Integer> alertNearWeeklyBudgetAmphiro;

	// returns daily water budget
	private Entry<Boolean, Integer> alertReachedDailyBudgetSWM;

	// returns daily shower budget
	private Entry<Boolean, Integer> alertReachedDailyBudgetAmphiro;

	private boolean alertWaterChampionSWM;

	private boolean alertShowerChampionAmphiro;

	private SimpleEntry<Boolean, Double> alertTooMuchWaterConsumptionSWM;

	private SimpleEntry<Boolean, Double> alertTooMuchWaterConsumptionAmphiro;

	private SimpleEntry<Boolean, Double> alertTooMuchEnergyAmphiro;

	// percent
	private SimpleEntry<Boolean, Integer> alertReducedWaterUseSWM;

	// percent
	private SimpleEntry<Boolean, Integer> alertImprovedShowerEfficiencyAmphiro;

	private SimpleEntry<Boolean, Integer> alertWaterEfficiencyLeaderSWM;

	private boolean alertPromptGoodJobMonthlySWM;

	private SimpleEntry<Boolean, Integer> alertLitresSavedSWM;

	private boolean alertTop25SaverWeeklySWM;

	private boolean alertTop10SaverSWM;

	// liters above
	private SimpleEntry<Boolean, Integer> recommendLessShowerTimeAmphiro;

	// annual shower consumption(or guess annual from 1 month)
	private SimpleEntry<Boolean, Integer> recommendLowerTemperatureAmphiro;

	// annual shower consumption (or guess annual from 1 month)
	private SimpleEntry<Boolean, Integer> recommendLowerFlowAmphiro;

	// annual shower consumption (or guess annual from 1 month)
	private SimpleEntry<Boolean, Integer> recommendShowerHeadChangeAmphiro;

	// percent of usage above others
	private SimpleEntry<Boolean, Integer> recommendShampooChangeAmphiro;

	// liters more than average
	private SimpleEntry<Boolean, Integer> recommendReduceFlowWhenNotNeededAmphiro;
        
    private boolean initialStaticTips;

    // random static tip
	private boolean produceStaticTip;
        
    private boolean meterInstalled;

    private boolean amphiroInstalled;
           
    private final static int NumDeviceTypes = EnumDeviceType.values().length;
    
    private final static int NumPartsOfDay = EnumPartOfDay.values().length;
    
    private List<IMessageParameters> insights = new ArrayList<>();
    
    //
    // ~ Getters/Setters
    //
    
    public boolean isInitialStaticTips(){
        return initialStaticTips;
    }
        
    public List<IMessageParameters> getInsights()
    {
        return insights;
    }
        
    public <P extends InsightBasicParameters> void addInsight(P p)
    {
        if (p != null) 
            insights.add(p);
    }
    
    public void clearInsights()
    {
        insights.clear();
    }
        
    public boolean isMeterInstalled() {
		return meterInstalled;
	}
        
        public boolean isAmphiroInstalled() {
		return amphiroInstalled;
	}        
                
	public boolean isAlertWaterLeakSWM() {
		return alertWaterLeakSWM;
	}

	public void setAlertWaterLeakSWM(boolean alertWaterLeakSWM) {
		this.alertWaterLeakSWM = alertWaterLeakSWM;
	}

	public boolean isAlertShowerStillOnAmphiro() {
		return alertShowerStillOnAmphiro;
	}

	public void setAlertShowerStillOnAmphiro(boolean alertShowerStillOnAmphiro) {
		this.alertShowerStillOnAmphiro = alertShowerStillOnAmphiro;
	}

	public boolean isAlertWaterQualitySWM() {
		return alertWaterQualitySWM;
	}

	public void setAlertWaterQualitySWM(boolean alertWaterQualitySWM) {
		this.alertWaterQualitySWM = alertWaterQualitySWM;
	}

	public boolean isAlertHotTemperatureAmphiro() {
		return alertHotTemperatureAmphiro;
	}

	public void setAlertHotTemperatureAmphiro(boolean alertHotTemperatureAmphiro) {
		this.alertHotTemperatureAmphiro = alertHotTemperatureAmphiro;
	}

	public SimpleEntry<Integer, Integer> getAlertNearDailyBudgetSWM() {
		return alertNearDailyBudgetSWM;
	}

	public void setAlertNearDailyBudgetSWM(SimpleEntry<Integer, Integer> alertNearDailyBudgetSWM) {
		this.alertNearDailyBudgetSWM = alertNearDailyBudgetSWM;
	}

	public SimpleEntry<Integer, Integer> getAlertNearWeeklyBudgetSWM() {
		return alertNearWeeklyBudgetSWM;
	}

	public void setAlertNearWeeklyBudgetSWM(SimpleEntry<Integer, Integer> alertNearWeeklyBudgetSWM) {
		this.alertNearWeeklyBudgetSWM = alertNearWeeklyBudgetSWM;
	}

	public SimpleEntry<Integer, Integer> getAlertNearDailyBudgetAmphiro() {
		return alertNearDailyBudgetAmphiro;
	}

	public void setAlertNearDailyBudgetAmphiro(SimpleEntry<Integer, Integer> alertNearDailyBudgetAmphiro) {
		this.alertNearDailyBudgetAmphiro = alertNearDailyBudgetAmphiro;
	}

	public SimpleEntry<Integer, Integer> getAlertNearWeeklyBudgetAmphiro() {
		return alertNearWeeklyBudgetAmphiro;
	}

	public void setAlertNearWeeklyBudgetAmphiro(SimpleEntry<Integer, Integer> alertNearWeeklyBudgetAmphiro) {
		this.alertNearWeeklyBudgetAmphiro = alertNearWeeklyBudgetAmphiro;
	}

	public Entry<Boolean, Integer> getAlertReachedDailyBudgetSWM() {
		return alertReachedDailyBudgetSWM;
	}

	public void setAlertReachedDailyBudgetSWM(Entry<Boolean, Integer> alertReachedDailyBudgetSWM) {
		this.alertReachedDailyBudgetSWM = alertReachedDailyBudgetSWM;
	}

	public Entry<Boolean, Integer> getAlertReachedDailyBudgetAmphiro() {
		return alertReachedDailyBudgetAmphiro;
	}

	public void setAlertReachedDailyBudgetAmphiro(Entry<Boolean, Integer> alertReachedDailyBudgetAmphiro) {
		this.alertReachedDailyBudgetAmphiro = alertReachedDailyBudgetAmphiro;
	}

	public boolean isAlertWaterChampionSWM() {
		return alertWaterChampionSWM;
	}

	public void setAlertWaterChampionSWM(boolean alertWaterChampionSWM) {
		this.alertWaterChampionSWM = alertWaterChampionSWM;
	}

	public boolean isAlertShowerChampionAmphiro() {
		return alertShowerChampionAmphiro;
	}

	public void setAlertShowerChampionAmphiro(boolean alertShowerChampionAmphiro) {
		this.alertShowerChampionAmphiro = alertShowerChampionAmphiro;
	}

	public SimpleEntry<Boolean, Double> getAlertTooMuchWaterConsumptionSWM() {
		return alertTooMuchWaterConsumptionSWM;
	}

	public void setAlertTooMuchWaterConsumptionSWM(SimpleEntry<Boolean, Double> alertTooMuchWaterConsumptionSWM) {
		this.alertTooMuchWaterConsumptionSWM = alertTooMuchWaterConsumptionSWM;
	}

	public SimpleEntry<Boolean, Double> getAlertTooMuchWaterConsumptionAmphiro() {
		return alertTooMuchWaterConsumptionAmphiro;
	}

	public void setAlertTooMuchWaterConsumptionAmphiro(SimpleEntry<Boolean, Double> alertTooMuchWaterConsumptionAmphiro) {
		this.alertTooMuchWaterConsumptionAmphiro = alertTooMuchWaterConsumptionAmphiro;
	}

	public SimpleEntry<Boolean, Double> getAlertTooMuchEnergyAmphiro() {
		return alertTooMuchEnergyAmphiro;
	}

	public void setAlertTooMuchEnergyAmphiro(SimpleEntry<Boolean, Double> alertTooMuchEnergyAmphiro) {
		this.alertTooMuchEnergyAmphiro = alertTooMuchEnergyAmphiro;
	}

	public SimpleEntry<Boolean, Integer> getAlertReducedWaterUseSWM() {
		return alertReducedWaterUseSWM;
	}

	public void setAlertReducedWaterUseSWM(SimpleEntry<Boolean, Integer> alertReducedWaterUseSWM) {
		this.alertReducedWaterUseSWM = alertReducedWaterUseSWM;
	}

	public SimpleEntry<Boolean, Integer> getAlertImprovedShowerEfficiencyAmphiro() {
		return alertImprovedShowerEfficiencyAmphiro;
	}

	public void setAlertImprovedShowerEfficiencyAmphiro(
					SimpleEntry<Boolean, Integer> alertImprovedShowerEfficiencyAmphiro) {
		this.alertImprovedShowerEfficiencyAmphiro = alertImprovedShowerEfficiencyAmphiro;
	}

	public SimpleEntry<Boolean, Integer> getAlertWaterEfficiencyLeaderSWM() {
		return alertWaterEfficiencyLeaderSWM;
	}

	public void setAlertWaterEfficiencyLeaderSWM(SimpleEntry<Boolean, Integer> alertWaterEfficiencyLeaderSWM) {
		this.alertWaterEfficiencyLeaderSWM = alertWaterEfficiencyLeaderSWM;
	}

	public boolean isAlertPromptGoodJobMonthlySWM() {
		return alertPromptGoodJobMonthlySWM;
	}

	public void setAlertPromptGoodJobMonthlySWM(boolean alertPromptGoodJobMonthlySWM) {
		this.alertPromptGoodJobMonthlySWM = alertPromptGoodJobMonthlySWM;
	}

	public SimpleEntry<Boolean, Integer> getAlertLitresSavedSWM() {
		return alertLitresSavedSWM;
	}

	public void setAlertLitresSavedSWM(SimpleEntry<Boolean, Integer> alertLitresSavedSWM) {
		this.alertLitresSavedSWM = alertLitresSavedSWM;
	}

	public boolean isAlertTop25SaverWeeklySWM() {
		return alertTop25SaverWeeklySWM;
	}

	public void setAlertTop25SaverWeeklySWM(boolean alertTop25SaverWeeklySWM) {
		this.alertTop25SaverWeeklySWM = alertTop25SaverWeeklySWM;
	}

	public boolean isAlertTop10SaverSWM() {
		return alertTop10SaverSWM;
	}
        
    public boolean isStaticTipToBeProduced() {
		return produceStaticTip;
	}

	public void setAlertTop10SaverSWM(boolean alertTop10SaverSWM) {
		this.alertTop10SaverSWM = alertTop10SaverSWM;
	}

	public SimpleEntry<Boolean, Integer> getRecommendLessShowerTimeAmphiro() {
		return recommendLessShowerTimeAmphiro;
	}

	public void setRecommendLessShowerTimeAmphiro(SimpleEntry<Boolean, Integer> recommendLessShowerTimeAmphiro) {
		this.recommendLessShowerTimeAmphiro = recommendLessShowerTimeAmphiro;
	}

	public SimpleEntry<Boolean, Integer> getRecommendLowerTemperatureAmphiro() {
		return recommendLowerTemperatureAmphiro;
	}

	public void setRecommendLowerTemperatureAmphiro(SimpleEntry<Boolean, Integer> recommendLowerTemperatureAmphiro) {
		this.recommendLowerTemperatureAmphiro = recommendLowerTemperatureAmphiro;
	}

	public SimpleEntry<Boolean, Integer> getRecommendLowerFlowAmphiro() {
		return recommendLowerFlowAmphiro;
	}

	public void setRecommendLowerFlowAmphiro(SimpleEntry<Boolean, Integer> recommendLowerFlowAmphiro) {
		this.recommendLowerFlowAmphiro = recommendLowerFlowAmphiro;
	}

	public SimpleEntry<Boolean, Integer> getRecommendShowerHeadChangeAmphiro() {
		return recommendShowerHeadChangeAmphiro;
	}

	public void setRecommendShowerHeadChangeAmphiro(SimpleEntry<Boolean, Integer> recommendShowerHeadChangeAmphiro) {
		this.recommendShowerHeadChangeAmphiro = recommendShowerHeadChangeAmphiro;
	}

	public SimpleEntry<Boolean, Integer> getRecommendShampooChangeAmphiro() {
		return recommendShampooChangeAmphiro;
	}

	public void setRecommendShampooChangeAmphiro(SimpleEntry<Boolean, Integer> recommendShampooChangeAmphiro) {
		this.recommendShampooChangeAmphiro = recommendShampooChangeAmphiro;
	}

	public SimpleEntry<Boolean, Integer> getRecommendReduceFlowWhenNotNeededAmphiro() {
		return recommendReduceFlowWhenNotNeededAmphiro;
	}

	public void setRecommendReduceFlowWhenNotNeededAmphiro(
					SimpleEntry<Boolean, Integer> recommendReduceFlowWhenNotNeededAmphiro) {
		this.recommendReduceFlowWhenNotNeededAmphiro = recommendReduceFlowWhenNotNeededAmphiro;
	}
        
    public void setInitialStaticTips(boolean initialStaticTips){
        this.initialStaticTips = initialStaticTips;
    }

    public void setStaticTip(boolean produceStaticTip){
            this.produceStaticTip = produceStaticTip;
    }

    public void setMeterInstalled(boolean meterInstalled){
            this.meterInstalled = meterInstalled;
    }

    public void setAmphiroInstalled(boolean amphiroInstalled){
            this.amphiroInstalled = amphiroInstalled;
    }        
}
