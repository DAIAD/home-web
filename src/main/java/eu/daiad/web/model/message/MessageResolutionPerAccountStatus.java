package eu.daiad.web.model.message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageResolutionPerAccountStatus 
{
	private final UUID accountKey; 
    
    private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterLeakSWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertShowerStillOnAmphiro;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterQualitySWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertHotTemperatureAmphiro;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertNearDailyBudgetSWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertNearWeeklyBudgetSWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertNearDailyBudgetAmphiro;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertNearWeeklyBudgetAmphiro;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertReachedDailyBudgetSWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertReachedDailyBudgetAmphiro;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterChampionSWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertShowerChampionAmphiro;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchWaterConsumptionSWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchWaterConsumptionAmphiro;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchEnergyAmphiro;
	
	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertReducedWaterUseSWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertReducedWaterUseAmphiro;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterEfficiencyLeaderSWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertPromptGoodJobMonthlySWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertLitresSavedSWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTop25SaverWeeklySWM;

	private IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTop10SaverWeeklySWM;
	
	private IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLessShowerTimeAmphiro;

	private IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLowerTemperatureAmphiro;

	private IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLowerFlowAmphiro;

	private IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendShowerHeadChangeAmphiro;

	private IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendShampooChangeAmphiro;

	private IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendReduceFlowWhenNotNeededAmphiro;
        
    private boolean initialStaticTips;

    // random static tip
	private boolean produceStaticTip;
        
    private boolean meterInstalled;

    private boolean amphiroInstalled;
    
    private List<IMessageResolutionStatus<Recommendation.ParameterizedTemplate>> insights = new ArrayList<>();
    
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
        
    public List<IMessageResolutionStatus<Recommendation.ParameterizedTemplate>> getInsights()
    {
        return insights;
    }
        
    public <P extends Insight.ParameterizedTemplate> void addInsight(MessageResolutionStatus<P> p)
    {
        if (p != null) { 
            insights.add(
                new MessageResolutionStatus<Recommendation.ParameterizedTemplate>(
                    p.getScore(),
                    p.getMessage())
                );
        }
    }
    
    public <P extends Insight.ParameterizedTemplate> void addInsights(List<MessageResolutionStatus<P>> l)
    {
        for (MessageResolutionStatus<P> p: l)
            addInsight(p);
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
                
    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertWaterLeakSWM()
    {
        return alertWaterLeakSWM;
    }

    public void setAlertWaterLeakSWM(IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterLeakSWM)
    {
        this.alertWaterLeakSWM = alertWaterLeakSWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertShowerStillOnAmphiro()
    {
        return alertShowerStillOnAmphiro;
    }

    public void setAlertShowerStillOnAmphiro(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertShowerStillOnAmphiro)
    {
        this.alertShowerStillOnAmphiro = alertShowerStillOnAmphiro;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertWaterQualitySWM()
    {
        return alertWaterQualitySWM;
    }

    public void setAlertWaterQualitySWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterQualitySWM)
    {
        this.alertWaterQualitySWM = alertWaterQualitySWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertHotTemperatureAmphiro()
    {
        return alertHotTemperatureAmphiro;
    }

    public void setAlertHotTemperatureAmphiro(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertHotTemperatureAmphiro)
    {
        this.alertHotTemperatureAmphiro = alertHotTemperatureAmphiro;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertNearDailyBudgetSWM()
    {
        return alertNearDailyBudgetSWM;
    }

    public void setAlertNearDailyBudgetSWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertNearDailyBudgetSWM)
    {
        this.alertNearDailyBudgetSWM = alertNearDailyBudgetSWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertNearWeeklyBudgetSWM()
    {
        return alertNearWeeklyBudgetSWM;
    }

    public void setAlertNearWeeklyBudgetSWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertNearWeeklyBudgetSWM)
    {
        this.alertNearWeeklyBudgetSWM = alertNearWeeklyBudgetSWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertNearDailyBudgetAmphiro()
    {
        return alertNearDailyBudgetAmphiro;
    }

    public void setAlertNearDailyBudgetAmphiro(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertNearDailyBudgetAmphiro)
    {
        this.alertNearDailyBudgetAmphiro = alertNearDailyBudgetAmphiro;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertNearWeeklyBudgetAmphiro()
    {
        return alertNearWeeklyBudgetAmphiro;
    }

    public void setAlertNearWeeklyBudgetAmphiro(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertNearWeeklyBudgetAmphiro)
    {
        this.alertNearWeeklyBudgetAmphiro = alertNearWeeklyBudgetAmphiro;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertReachedDailyBudgetSWM()
    {
        return alertReachedDailyBudgetSWM;
    }

    public void setAlertReachedDailyBudgetSWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertReachedDailyBudgetSWM)
    {
        this.alertReachedDailyBudgetSWM = alertReachedDailyBudgetSWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertReachedDailyBudgetAmphiro()
    {
        return alertReachedDailyBudgetAmphiro;
    }

    public void setAlertReachedDailyBudgetAmphiro(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertReachedDailyBudgetAmphiro)
    {
        this.alertReachedDailyBudgetAmphiro = alertReachedDailyBudgetAmphiro;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertWaterChampionSWM()
    {
        return alertWaterChampionSWM;
    }

    public void setAlertWaterChampionSWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterChampionSWM)
    {
        this.alertWaterChampionSWM = alertWaterChampionSWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertShowerChampionAmphiro()
    {
        return alertShowerChampionAmphiro;
    }

    public void setAlertShowerChampionAmphiro(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertShowerChampionAmphiro)
    {
        this.alertShowerChampionAmphiro = alertShowerChampionAmphiro;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertTooMuchWaterConsumptionSWM()
    {
        return alertTooMuchWaterConsumptionSWM;
    }

    public void setAlertTooMuchWaterConsumptionSWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchWaterConsumptionSWM)
    {
        this.alertTooMuchWaterConsumptionSWM = alertTooMuchWaterConsumptionSWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertTooMuchWaterConsumptionAmphiro()
    {
        return alertTooMuchWaterConsumptionAmphiro;
    }

    public void setAlertTooMuchWaterConsumptionAmphiro(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchWaterConsumptionAmphiro)
    {
        this.alertTooMuchWaterConsumptionAmphiro = alertTooMuchWaterConsumptionAmphiro;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertTooMuchEnergyAmphiro()
    {
        return alertTooMuchEnergyAmphiro;
    }

    public void setAlertTooMuchEnergyAmphiro(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTooMuchEnergyAmphiro)
    {
        this.alertTooMuchEnergyAmphiro = alertTooMuchEnergyAmphiro;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertReducedWaterUseSWM()
    {
        return alertReducedWaterUseSWM;
    }

    public void setAlertReducedWaterUseSWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertReducedWaterUseSWM)
    {
        this.alertReducedWaterUseSWM = alertReducedWaterUseSWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertReducedWaterUseAmphiro()
    {
        return alertReducedWaterUseAmphiro;
    }

    public void setAlertReducedWaterUseAmphiro(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertReducedWaterUseAmphiro)
    {
        this.alertReducedWaterUseAmphiro = alertReducedWaterUseAmphiro;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertWaterEfficiencyLeaderSWM()
    {
        return alertWaterEfficiencyLeaderSWM;
    }

    public void setAlertWaterEfficiencyLeaderSWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertWaterEfficiencyLeaderSWM)
    {
        this.alertWaterEfficiencyLeaderSWM = alertWaterEfficiencyLeaderSWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertPromptGoodJobMonthlySWM()
    {
        return alertPromptGoodJobMonthlySWM;
    }

    public void setAlertPromptGoodJobMonthlySWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertPromptGoodJobMonthlySWM)
    {
        this.alertPromptGoodJobMonthlySWM = alertPromptGoodJobMonthlySWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertLitresSavedSWM()
    {
        return alertLitresSavedSWM;
    }

    public void setAlertLitresSavedSWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertLitresSavedSWM)
    {
        this.alertLitresSavedSWM = alertLitresSavedSWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertTop25SaverWeeklySWM()
    {
        return alertTop25SaverWeeklySWM;
    }

    public void setAlertTop25SaverWeeklySWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTop25SaverWeeklySWM)
    {
        this.alertTop25SaverWeeklySWM = alertTop25SaverWeeklySWM;
    }

    public IMessageResolutionStatus<Alert.ParameterizedTemplate> getAlertTop10SaverWeeklySWM()
    {
        return alertTop10SaverWeeklySWM;
    }

    public boolean isStaticTipToBeProduced()
    {
        return produceStaticTip;
    }

    public void setAlertTop10SaverWeeklySWM(
        IMessageResolutionStatus<Alert.ParameterizedTemplate> alertTop10SaverSWM)
    {
        this.alertTop10SaverWeeklySWM = alertTop10SaverSWM;
    }

    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendLessShowerTimeAmphiro()
    {
        return recommendLessShowerTimeAmphiro;
    }

    public void setRecommendLessShowerTimeAmphiro(
        IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLessShowerTimeAmphiro)
    {
        this.recommendLessShowerTimeAmphiro = recommendLessShowerTimeAmphiro;
    }

    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendLowerTemperatureAmphiro()
    {
        return recommendLowerTemperatureAmphiro;
    }

    public void setRecommendLowerTemperatureAmphiro(
        IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLowerTemperatureAmphiro)
    {
        this.recommendLowerTemperatureAmphiro = recommendLowerTemperatureAmphiro;
    }

    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendLowerFlowAmphiro()
    {
        return recommendLowerFlowAmphiro;
    }

    public void setRecommendLowerFlowAmphiro(
        IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendLowerFlowAmphiro)
    {
        this.recommendLowerFlowAmphiro = recommendLowerFlowAmphiro;
    }

    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendShowerHeadChangeAmphiro()
    {
        return recommendShowerHeadChangeAmphiro;
    }

    public void setRecommendShowerHeadChangeAmphiro(
        IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendShowerHeadChangeAmphiro)
    {
        this.recommendShowerHeadChangeAmphiro = recommendShowerHeadChangeAmphiro;
    }

    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendShampooChangeAmphiro()
    {
        return recommendShampooChangeAmphiro;
    }

    public void setRecommendShampooChangeAmphiro(
        IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendShampooChangeAmphiro)
    {
        this.recommendShampooChangeAmphiro = recommendShampooChangeAmphiro;
    }

    public IMessageResolutionStatus<Recommendation.ParameterizedTemplate> getRecommendReduceFlowWhenNotNeededAmphiro()
    {
        return recommendReduceFlowWhenNotNeededAmphiro;
    }

    public void setRecommendReduceFlowWhenNotNeededAmphiro(
        IMessageResolutionStatus<Recommendation.ParameterizedTemplate> recommendReduceFlowWhenNotNeededAmphiro)
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
