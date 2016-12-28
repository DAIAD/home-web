package eu.daiad.web.model.message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageResolutionPerAccountStatus 
{
	private final UUID accountKey; 
    
    private IMessageResolutionStatus<Alert.Parameters> alertWaterLeakSWM;

	private IMessageResolutionStatus<Alert.Parameters> alertShowerStillOnAmphiro;

	private IMessageResolutionStatus<Alert.Parameters> alertWaterQualitySWM;

	private IMessageResolutionStatus<Alert.Parameters> alertHotTemperatureAmphiro;

	private IMessageResolutionStatus<Alert.Parameters> alertNearDailyBudgetSWM;

	private IMessageResolutionStatus<Alert.Parameters> alertNearWeeklyBudgetSWM;

	private IMessageResolutionStatus<Alert.Parameters> alertNearDailyBudgetAmphiro;

	private IMessageResolutionStatus<Alert.Parameters> alertNearWeeklyBudgetAmphiro;

	private IMessageResolutionStatus<Alert.Parameters> alertReachedDailyBudgetSWM;

	private IMessageResolutionStatus<Alert.Parameters> alertReachedDailyBudgetAmphiro;

	private IMessageResolutionStatus<Alert.Parameters> alertWaterChampionSWM;

	private IMessageResolutionStatus<Alert.Parameters> alertShowerChampionAmphiro;

	private IMessageResolutionStatus<Alert.Parameters> alertTooMuchWaterConsumptionSWM;

	private IMessageResolutionStatus<Alert.Parameters> alertTooMuchWaterConsumptionAmphiro;

	private IMessageResolutionStatus<Alert.Parameters> alertTooMuchEnergyAmphiro;
	
	private IMessageResolutionStatus<Alert.Parameters> alertReducedWaterUseSWM;

	private IMessageResolutionStatus<Alert.Parameters> alertReducedWaterUseAmphiro;

	private IMessageResolutionStatus<Alert.Parameters> alertWaterEfficiencyLeaderSWM;

	private IMessageResolutionStatus<Alert.Parameters> alertPromptGoodJobMonthlySWM;

	private IMessageResolutionStatus<Alert.Parameters> alertLitresSavedSWM;

	private IMessageResolutionStatus<Alert.Parameters> alertTop25SaverWeeklySWM;

	private IMessageResolutionStatus<Alert.Parameters> alertTop10SaverWeeklySWM;
	
	private IMessageResolutionStatus<Recommendation.Parameters> recommendLessShowerTimeAmphiro;

	private IMessageResolutionStatus<Recommendation.Parameters> recommendLowerTemperatureAmphiro;

	private IMessageResolutionStatus<Recommendation.Parameters> recommendLowerFlowAmphiro;

	private IMessageResolutionStatus<Recommendation.Parameters> recommendShowerHeadChangeAmphiro;

	private IMessageResolutionStatus<Recommendation.Parameters> recommendShampooChangeAmphiro;

	private IMessageResolutionStatus<Recommendation.Parameters> recommendReduceFlowWhenNotNeededAmphiro;
        
    private boolean initialStaticTips;

    // random static tip
	private boolean produceStaticTip;
        
    private boolean meterInstalled;

    private boolean amphiroInstalled;
    
    private List<IMessageResolutionStatus<Recommendation.Parameters>> insights = new ArrayList<>();
    
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
        
    public List<IMessageResolutionStatus<Recommendation.Parameters>> getInsights()
    {
        return insights;
    }
        
    public <P extends Insight.Parameters> void addInsight(MessageResolutionStatus<P> p)
    {
        if (p != null) { 
            insights.add(
                new MessageResolutionStatus<Recommendation.Parameters>(
                    p.getScore(),
                    p.getParameters())
                );
        }
    }
    
    public <P extends Insight.Parameters> void addInsights(List<MessageResolutionStatus<P>> l)
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
                
    public IMessageResolutionStatus<Alert.Parameters> getAlertWaterLeakSWM()
    {
        return alertWaterLeakSWM;
    }

    public void setAlertWaterLeakSWM(IMessageResolutionStatus<Alert.Parameters> alertWaterLeakSWM)
    {
        this.alertWaterLeakSWM = alertWaterLeakSWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertShowerStillOnAmphiro()
    {
        return alertShowerStillOnAmphiro;
    }

    public void setAlertShowerStillOnAmphiro(
        IMessageResolutionStatus<Alert.Parameters> alertShowerStillOnAmphiro)
    {
        this.alertShowerStillOnAmphiro = alertShowerStillOnAmphiro;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertWaterQualitySWM()
    {
        return alertWaterQualitySWM;
    }

    public void setAlertWaterQualitySWM(
        IMessageResolutionStatus<Alert.Parameters> alertWaterQualitySWM)
    {
        this.alertWaterQualitySWM = alertWaterQualitySWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertHotTemperatureAmphiro()
    {
        return alertHotTemperatureAmphiro;
    }

    public void setAlertHotTemperatureAmphiro(
        IMessageResolutionStatus<Alert.Parameters> alertHotTemperatureAmphiro)
    {
        this.alertHotTemperatureAmphiro = alertHotTemperatureAmphiro;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertNearDailyBudgetSWM()
    {
        return alertNearDailyBudgetSWM;
    }

    public void setAlertNearDailyBudgetSWM(
        IMessageResolutionStatus<Alert.Parameters> alertNearDailyBudgetSWM)
    {
        this.alertNearDailyBudgetSWM = alertNearDailyBudgetSWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertNearWeeklyBudgetSWM()
    {
        return alertNearWeeklyBudgetSWM;
    }

    public void setAlertNearWeeklyBudgetSWM(
        IMessageResolutionStatus<Alert.Parameters> alertNearWeeklyBudgetSWM)
    {
        this.alertNearWeeklyBudgetSWM = alertNearWeeklyBudgetSWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertNearDailyBudgetAmphiro()
    {
        return alertNearDailyBudgetAmphiro;
    }

    public void setAlertNearDailyBudgetAmphiro(
        IMessageResolutionStatus<Alert.Parameters> alertNearDailyBudgetAmphiro)
    {
        this.alertNearDailyBudgetAmphiro = alertNearDailyBudgetAmphiro;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertNearWeeklyBudgetAmphiro()
    {
        return alertNearWeeklyBudgetAmphiro;
    }

    public void setAlertNearWeeklyBudgetAmphiro(
        IMessageResolutionStatus<Alert.Parameters> alertNearWeeklyBudgetAmphiro)
    {
        this.alertNearWeeklyBudgetAmphiro = alertNearWeeklyBudgetAmphiro;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertReachedDailyBudgetSWM()
    {
        return alertReachedDailyBudgetSWM;
    }

    public void setAlertReachedDailyBudgetSWM(
        IMessageResolutionStatus<Alert.Parameters> alertReachedDailyBudgetSWM)
    {
        this.alertReachedDailyBudgetSWM = alertReachedDailyBudgetSWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertReachedDailyBudgetAmphiro()
    {
        return alertReachedDailyBudgetAmphiro;
    }

    public void setAlertReachedDailyBudgetAmphiro(
        IMessageResolutionStatus<Alert.Parameters> alertReachedDailyBudgetAmphiro)
    {
        this.alertReachedDailyBudgetAmphiro = alertReachedDailyBudgetAmphiro;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertWaterChampionSWM()
    {
        return alertWaterChampionSWM;
    }

    public void setAlertWaterChampionSWM(
        IMessageResolutionStatus<Alert.Parameters> alertWaterChampionSWM)
    {
        this.alertWaterChampionSWM = alertWaterChampionSWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertShowerChampionAmphiro()
    {
        return alertShowerChampionAmphiro;
    }

    public void setAlertShowerChampionAmphiro(
        IMessageResolutionStatus<Alert.Parameters> alertShowerChampionAmphiro)
    {
        this.alertShowerChampionAmphiro = alertShowerChampionAmphiro;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertTooMuchWaterConsumptionSWM()
    {
        return alertTooMuchWaterConsumptionSWM;
    }

    public void setAlertTooMuchWaterConsumptionSWM(
        IMessageResolutionStatus<Alert.Parameters> alertTooMuchWaterConsumptionSWM)
    {
        this.alertTooMuchWaterConsumptionSWM = alertTooMuchWaterConsumptionSWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertTooMuchWaterConsumptionAmphiro()
    {
        return alertTooMuchWaterConsumptionAmphiro;
    }

    public void setAlertTooMuchWaterConsumptionAmphiro(
        IMessageResolutionStatus<Alert.Parameters> alertTooMuchWaterConsumptionAmphiro)
    {
        this.alertTooMuchWaterConsumptionAmphiro = alertTooMuchWaterConsumptionAmphiro;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertTooMuchEnergyAmphiro()
    {
        return alertTooMuchEnergyAmphiro;
    }

    public void setAlertTooMuchEnergyAmphiro(
        IMessageResolutionStatus<Alert.Parameters> alertTooMuchEnergyAmphiro)
    {
        this.alertTooMuchEnergyAmphiro = alertTooMuchEnergyAmphiro;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertReducedWaterUseSWM()
    {
        return alertReducedWaterUseSWM;
    }

    public void setAlertReducedWaterUseSWM(
        IMessageResolutionStatus<Alert.Parameters> alertReducedWaterUseSWM)
    {
        this.alertReducedWaterUseSWM = alertReducedWaterUseSWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertReducedWaterUseAmphiro()
    {
        return alertReducedWaterUseAmphiro;
    }

    public void setAlertReducedWaterUseAmphiro(
        IMessageResolutionStatus<Alert.Parameters> alertReducedWaterUseAmphiro)
    {
        this.alertReducedWaterUseAmphiro = alertReducedWaterUseAmphiro;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertWaterEfficiencyLeaderSWM()
    {
        return alertWaterEfficiencyLeaderSWM;
    }

    public void setAlertWaterEfficiencyLeaderSWM(
        IMessageResolutionStatus<Alert.Parameters> alertWaterEfficiencyLeaderSWM)
    {
        this.alertWaterEfficiencyLeaderSWM = alertWaterEfficiencyLeaderSWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertPromptGoodJobMonthlySWM()
    {
        return alertPromptGoodJobMonthlySWM;
    }

    public void setAlertPromptGoodJobMonthlySWM(
        IMessageResolutionStatus<Alert.Parameters> alertPromptGoodJobMonthlySWM)
    {
        this.alertPromptGoodJobMonthlySWM = alertPromptGoodJobMonthlySWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertLitresSavedSWM()
    {
        return alertLitresSavedSWM;
    }

    public void setAlertLitresSavedSWM(
        IMessageResolutionStatus<Alert.Parameters> alertLitresSavedSWM)
    {
        this.alertLitresSavedSWM = alertLitresSavedSWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertTop25SaverWeeklySWM()
    {
        return alertTop25SaverWeeklySWM;
    }

    public void setAlertTop25SaverWeeklySWM(
        IMessageResolutionStatus<Alert.Parameters> alertTop25SaverWeeklySWM)
    {
        this.alertTop25SaverWeeklySWM = alertTop25SaverWeeklySWM;
    }

    public IMessageResolutionStatus<Alert.Parameters> getAlertTop10SaverWeeklySWM()
    {
        return alertTop10SaverWeeklySWM;
    }

    public boolean isStaticTipToBeProduced()
    {
        return produceStaticTip;
    }

    public void setAlertTop10SaverWeeklySWM(
        IMessageResolutionStatus<Alert.Parameters> alertTop10SaverSWM)
    {
        this.alertTop10SaverWeeklySWM = alertTop10SaverSWM;
    }

    public IMessageResolutionStatus<Recommendation.Parameters> getRecommendLessShowerTimeAmphiro()
    {
        return recommendLessShowerTimeAmphiro;
    }

    public void setRecommendLessShowerTimeAmphiro(
        IMessageResolutionStatus<Recommendation.Parameters> recommendLessShowerTimeAmphiro)
    {
        this.recommendLessShowerTimeAmphiro = recommendLessShowerTimeAmphiro;
    }

    public IMessageResolutionStatus<Recommendation.Parameters> getRecommendLowerTemperatureAmphiro()
    {
        return recommendLowerTemperatureAmphiro;
    }

    public void setRecommendLowerTemperatureAmphiro(
        IMessageResolutionStatus<Recommendation.Parameters> recommendLowerTemperatureAmphiro)
    {
        this.recommendLowerTemperatureAmphiro = recommendLowerTemperatureAmphiro;
    }

    public IMessageResolutionStatus<Recommendation.Parameters> getRecommendLowerFlowAmphiro()
    {
        return recommendLowerFlowAmphiro;
    }

    public void setRecommendLowerFlowAmphiro(
        IMessageResolutionStatus<Recommendation.Parameters> recommendLowerFlowAmphiro)
    {
        this.recommendLowerFlowAmphiro = recommendLowerFlowAmphiro;
    }

    public IMessageResolutionStatus<Recommendation.Parameters> getRecommendShowerHeadChangeAmphiro()
    {
        return recommendShowerHeadChangeAmphiro;
    }

    public void setRecommendShowerHeadChangeAmphiro(
        IMessageResolutionStatus<Recommendation.Parameters> recommendShowerHeadChangeAmphiro)
    {
        this.recommendShowerHeadChangeAmphiro = recommendShowerHeadChangeAmphiro;
    }

    public IMessageResolutionStatus<Recommendation.Parameters> getRecommendShampooChangeAmphiro()
    {
        return recommendShampooChangeAmphiro;
    }

    public void setRecommendShampooChangeAmphiro(
        IMessageResolutionStatus<Recommendation.Parameters> recommendShampooChangeAmphiro)
    {
        this.recommendShampooChangeAmphiro = recommendShampooChangeAmphiro;
    }

    public IMessageResolutionStatus<Recommendation.Parameters> getRecommendReduceFlowWhenNotNeededAmphiro()
    {
        return recommendReduceFlowWhenNotNeededAmphiro;
    }

    public void setRecommendReduceFlowWhenNotNeededAmphiro(
        IMessageResolutionStatus<Recommendation.Parameters> recommendReduceFlowWhenNotNeededAmphiro)
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
