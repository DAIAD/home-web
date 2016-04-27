
package eu.daiad.web.repository.application;

import eu.daiad.web.model.recommendation.MessageCalculationConfiguration;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.UUID;
import org.joda.time.DateTime;

/**
 *
 * @author nkarag
 */
public interface IMessageCalculationRepository {
    
    public void setConfig(MessageCalculationConfiguration config);
    public MessageCalculationConfiguration getConfig();
    public boolean alertWaterLeakSWM(UUID userKey);
    public boolean alertShowerStillOnAmphiro(UUID userKey);
    public boolean alertWaterQualitySWM(UUID userKey);
    public boolean alertHotTemperatureAmphiro(UUID userKey);
    //budget defined
    public Entry<Boolean, SimpleEntry<Integer, Integer>> alertNearDailyBudgetSWM(UUID userKey); //litres used, litres remaining
    public Entry<Boolean, SimpleEntry<Integer, Integer>> alertNearWeeklyBudgetSWM(UUID userKey); //litres used, litres remaining
    public Entry<Boolean, SimpleEntry<Integer, Integer>> alertNearDailyBudgetAmphiro(UUID userKey); //litres used, litres remaining
    public Entry<Boolean, SimpleEntry<Integer, Integer>> alertNearWeeklyBudgetAmphiro(UUID userKey); //litres used, litres remaining
    public Entry<Boolean, Integer> alertReachedDailyBudgetSWM(UUID userKey); //returns daily water budget
    public Entry<Boolean, Integer> alertReachedDailyBudgetAmphiro(UUID userKey); //returns daily shower budget     
    
    public boolean alertWaterChampionSWM(UUID userKey);     
    public boolean alertShowerChampionAmphiro(UUID userKey);
    public SimpleEntry<Boolean, Double> alertTooMuchWaterConsumptionSWM(UUID userKey);
    public SimpleEntry<Boolean, Double> alertTooMuchWaterConsumptionAmphiro(UUID userKey);
    public SimpleEntry<Boolean, Double> alertTooMuchEnergyAmphiro(UUID userKey);
    public SimpleEntry<Boolean, Integer> alertReducedWaterUseSWM(UUID userKey, DateTime startingWeek); //percent
    public SimpleEntry<Boolean, Integer> alertImprovedShowerEfficiencyAmphiro(UUID userKey, DateTime startingWeek);//percent
    public SimpleEntry<Boolean, Integer> alertWaterEfficiencyLeaderSWM(UUID userKey);
    
    public boolean alertPromptGoodJobMonthlySWM(UUID userKey);     
    //public boolean alertPromptGoodJobWeeklySWM(UUID userKey); //using monthly instead
    public SimpleEntry<Boolean, Integer> alertLitresSavedSWM(UUID userKey); //returns litres saved    
    public boolean alertTop25SaverWeeklySWM(UUID userKey);
    public boolean alertTop10SaverSWM(UUID userKey);    
    
    public SimpleEntry<Boolean, Integer> recommendLessShowerTimeAmphiro(UUID userKey); //returns litres above    
    public SimpleEntry<Boolean, Integer> recommendLowerTemperatureAmphiro(UUID userKey); //returns annual shower consumption(or guess annual from 1 month)
    public SimpleEntry<Boolean, Integer> recommendLowerFlowAmphiro(UUID userKey); //returns annual shower consumption (or guess annual from 1 month)
    public SimpleEntry<Boolean, Integer> recommendShowerHeadChangeAmphiro(UUID userKey); //returns annual shower consumption (or guess annual from 1 month)    
    public SimpleEntry<Boolean, Integer> recommendShampooChangeAmphiro(UUID userKey);//returns percent of usage above others
    public SimpleEntry<Boolean, Integer> recommendReduceFlowWhenNotNeededAmphiro(UUID userKey);//return litres more than average
}
