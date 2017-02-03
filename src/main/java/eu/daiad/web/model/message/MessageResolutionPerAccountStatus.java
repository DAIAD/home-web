package eu.daiad.web.model.message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageResolutionPerAccountStatus 
{
	private final UUID accountKey; 
    
    private MessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterLeakSWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertShowerStillOnAmphiro;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterQualitySWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertHotTemperatureAmphiro;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertNearDailyBudgetSWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertNearWeeklyBudgetSWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertNearDailyBudgetAmphiro;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertNearWeeklyBudgetAmphiro;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertReachedDailyBudgetSWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertReachedDailyBudgetAmphiro;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterChampionSWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertShowerChampionAmphiro;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchWaterConsumptionSWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchWaterConsumptionAmphiro;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchEnergyAmphiro;
	
	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertReducedWaterUseSWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertReducedWaterUseAmphiro;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterEfficiencyLeaderSWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertPromptGoodJobMonthlySWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertLitresSavedSWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertTop25SaverWeeklySWM;

	private MessageResolutionStatus<Alert.ParameterizedTemplate> alertTop10SaverWeeklySWM;
	
	private MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLessShowerTimeAmphiro;

	private MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLowerTemperatureAmphiro;

	private MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLowerFlowAmphiro;

	private MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendShowerHeadChangeAmphiro;

	private MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendShampooChangeAmphiro;

	private MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendReduceFlowWhenNotNeededAmphiro;
        
    private boolean initialStaticTips;

    // random static tip
	private boolean produceStaticTip;
        
    private boolean meterInstalled;

    private boolean amphiroInstalled;
    
    private List<MessageResolutionStatus<? extends Recommendation.ParameterizedTemplate>> insights = new ArrayList<>();
    
    //
    // ~ Constructors
    //
    
    public MessageResolutionPerAccountStatus(UUID accountKey)
    {
        this.accountKey = accountKey;
    }
    
    //
    // ~ Getters/Setters
    //
    
    public UUID getAccountKey()
    {
        return accountKey;
    }
    
    public boolean isInitialStaticTips(){
        return initialStaticTips;
    }
        
    public List<MessageResolutionStatus<? extends Recommendation.ParameterizedTemplate>> getInsights()
    {
        return insights;
    }
        
    public <P extends Insight.ParameterizedTemplate> void addInsight(SimpleMessageResolutionStatus<P> p)
    {
        if (p != null)
            insights.add(p);
    }
    
    public <P extends Insight.ParameterizedTemplate> void addInsights(List<SimpleMessageResolutionStatus<P>> l)
    {
        insights.addAll(l);
    }
    
    public void clearInsights()
    {
        insights.clear();
    }
        
    public boolean isMeterInstalled() 
    {
		return meterInstalled;
	}
        
    public boolean isAmphiroInstalled() 
    {
		return amphiroInstalled;
	}        
                
    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertWaterLeakSWM()
    {
        return alertWaterLeakSWM;
    }

    public void setAlertWaterLeakSWM(MessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterLeakSWM)
    {
        this.alertWaterLeakSWM = alertWaterLeakSWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertShowerStillOnAmphiro()
    {
        return alertShowerStillOnAmphiro;
    }

    public void setAlertShowerStillOnAmphiro(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertShowerStillOnAmphiro)
    {
        this.alertShowerStillOnAmphiro = alertShowerStillOnAmphiro;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertWaterQualitySWM()
    {
        return alertWaterQualitySWM;
    }

    public void setAlertWaterQualitySWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterQualitySWM)
    {
        this.alertWaterQualitySWM = alertWaterQualitySWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertHotTemperatureAmphiro()
    {
        return alertHotTemperatureAmphiro;
    }

    public void setAlertHotTemperatureAmphiro(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertHotTemperatureAmphiro)
    {
        this.alertHotTemperatureAmphiro = alertHotTemperatureAmphiro;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertNearDailyBudgetSWM()
    {
        return alertNearDailyBudgetSWM;
    }

    public void setAlertNearDailyBudgetSWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertNearDailyBudgetSWM)
    {
        this.alertNearDailyBudgetSWM = alertNearDailyBudgetSWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertNearWeeklyBudgetSWM()
    {
        return alertNearWeeklyBudgetSWM;
    }

    public void setAlertNearWeeklyBudgetSWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertNearWeeklyBudgetSWM)
    {
        this.alertNearWeeklyBudgetSWM = alertNearWeeklyBudgetSWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertNearDailyBudgetAmphiro()
    {
        return alertNearDailyBudgetAmphiro;
    }

    public void setAlertNearDailyBudgetAmphiro(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertNearDailyBudgetAmphiro)
    {
        this.alertNearDailyBudgetAmphiro = alertNearDailyBudgetAmphiro;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertNearWeeklyBudgetAmphiro()
    {
        return alertNearWeeklyBudgetAmphiro;
    }

    public void setAlertNearWeeklyBudgetAmphiro(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertNearWeeklyBudgetAmphiro)
    {
        this.alertNearWeeklyBudgetAmphiro = alertNearWeeklyBudgetAmphiro;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertReachedDailyBudgetSWM()
    {
        return alertReachedDailyBudgetSWM;
    }

    public void setAlertReachedDailyBudgetSWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertReachedDailyBudgetSWM)
    {
        this.alertReachedDailyBudgetSWM = alertReachedDailyBudgetSWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertReachedDailyBudgetAmphiro()
    {
        return alertReachedDailyBudgetAmphiro;
    }

    public void setAlertReachedDailyBudgetAmphiro(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertReachedDailyBudgetAmphiro)
    {
        this.alertReachedDailyBudgetAmphiro = alertReachedDailyBudgetAmphiro;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertWaterChampionSWM()
    {
        return alertWaterChampionSWM;
    }

    public void setAlertWaterChampionSWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterChampionSWM)
    {
        this.alertWaterChampionSWM = alertWaterChampionSWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertShowerChampionAmphiro()
    {
        return alertShowerChampionAmphiro;
    }

    public void setAlertShowerChampionAmphiro(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertShowerChampionAmphiro)
    {
        this.alertShowerChampionAmphiro = alertShowerChampionAmphiro;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertTooMuchWaterConsumptionSWM()
    {
        return alertTooMuchWaterConsumptionSWM;
    }

    public void setAlertTooMuchWaterConsumptionSWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchWaterConsumptionSWM)
    {
        this.alertTooMuchWaterConsumptionSWM = alertTooMuchWaterConsumptionSWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertTooMuchWaterConsumptionAmphiro()
    {
        return alertTooMuchWaterConsumptionAmphiro;
    }

    public void setAlertTooMuchWaterConsumptionAmphiro(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchWaterConsumptionAmphiro)
    {
        this.alertTooMuchWaterConsumptionAmphiro = alertTooMuchWaterConsumptionAmphiro;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertTooMuchEnergyAmphiro()
    {
        return alertTooMuchEnergyAmphiro;
    }

    public void setAlertTooMuchEnergyAmphiro(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchEnergyAmphiro)
    {
        this.alertTooMuchEnergyAmphiro = alertTooMuchEnergyAmphiro;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertReducedWaterUseSWM()
    {
        return alertReducedWaterUseSWM;
    }

    public void setAlertReducedWaterUseSWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertReducedWaterUseSWM)
    {
        this.alertReducedWaterUseSWM = alertReducedWaterUseSWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertReducedWaterUseAmphiro()
    {
        return alertReducedWaterUseAmphiro;
    }

    public void setAlertReducedWaterUseAmphiro(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertReducedWaterUseAmphiro)
    {
        this.alertReducedWaterUseAmphiro = alertReducedWaterUseAmphiro;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertWaterEfficiencyLeaderSWM()
    {
        return alertWaterEfficiencyLeaderSWM;
    }

    public void setAlertWaterEfficiencyLeaderSWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterEfficiencyLeaderSWM)
    {
        this.alertWaterEfficiencyLeaderSWM = alertWaterEfficiencyLeaderSWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertPromptGoodJobMonthlySWM()
    {
        return alertPromptGoodJobMonthlySWM;
    }

    public void setAlertPromptGoodJobMonthlySWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertPromptGoodJobMonthlySWM)
    {
        this.alertPromptGoodJobMonthlySWM = alertPromptGoodJobMonthlySWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertLitresSavedSWM()
    {
        return alertLitresSavedSWM;
    }

    public void setAlertLitresSavedSWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertLitresSavedSWM)
    {
        this.alertLitresSavedSWM = alertLitresSavedSWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertTop25SaverWeeklySWM()
    {
        return alertTop25SaverWeeklySWM;
    }

    public void setAlertTop25SaverWeeklySWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertTop25SaverWeeklySWM)
    {
        this.alertTop25SaverWeeklySWM = alertTop25SaverWeeklySWM;
    }

    public MessageResolutionStatus<Alert.ParameterizedTemplate> getAlertTop10SaverWeeklySWM()
    {
        return alertTop10SaverWeeklySWM;
    }

    public boolean isStaticTipToBeProduced()
    {
        return produceStaticTip;
    }

    public void setAlertTop10SaverWeeklySWM(
        MessageResolutionStatus<Alert.ParameterizedTemplate> alertTop10SaverSWM)
    {
        this.alertTop10SaverWeeklySWM = alertTop10SaverSWM;
    }

    public MessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendLessShowerTimeAmphiro()
    {
        return recommendLessShowerTimeAmphiro;
    }

    public void setRecommendLessShowerTimeAmphiro(
        MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLessShowerTimeAmphiro)
    {
        this.recommendLessShowerTimeAmphiro = recommendLessShowerTimeAmphiro;
    }

    public MessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendLowerTemperatureAmphiro()
    {
        return recommendLowerTemperatureAmphiro;
    }

    public void setRecommendLowerTemperatureAmphiro(
        MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLowerTemperatureAmphiro)
    {
        this.recommendLowerTemperatureAmphiro = recommendLowerTemperatureAmphiro;
    }

    public MessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendLowerFlowAmphiro()
    {
        return recommendLowerFlowAmphiro;
    }

    public void setRecommendLowerFlowAmphiro(
        MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLowerFlowAmphiro)
    {
        this.recommendLowerFlowAmphiro = recommendLowerFlowAmphiro;
    }

    public MessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendShowerHeadChangeAmphiro()
    {
        return recommendShowerHeadChangeAmphiro;
    }

    public void setRecommendShowerHeadChangeAmphiro(
        MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendShowerHeadChangeAmphiro)
    {
        this.recommendShowerHeadChangeAmphiro = recommendShowerHeadChangeAmphiro;
    }

    public MessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendShampooChangeAmphiro()
    {
        return recommendShampooChangeAmphiro;
    }

    public void setRecommendShampooChangeAmphiro(
        MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendShampooChangeAmphiro)
    {
        this.recommendShampooChangeAmphiro = recommendShampooChangeAmphiro;
    }

    public MessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendReduceFlowWhenNotNeededAmphiro()
    {
        return recommendReduceFlowWhenNotNeededAmphiro;
    }

    public void setRecommendReduceFlowWhenNotNeededAmphiro(
        MessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendReduceFlowWhenNotNeededAmphiro)
    {
        this.recommendReduceFlowWhenNotNeededAmphiro = recommendReduceFlowWhenNotNeededAmphiro;
    }

    public void setInitialStaticTips(boolean initialStaticTips)
    {
        this.initialStaticTips = initialStaticTips;
    }

    public void setStaticTip(boolean produceStaticTip)
    {
        this.produceStaticTip = produceStaticTip;
    }

    public void setMeterInstalled(boolean meterInstalled)
    {
        this.meterInstalled = meterInstalled;
    }

    public void setAmphiroInstalled(boolean amphiroInstalled)
    {
        this.amphiroInstalled = amphiroInstalled;
    }
}
