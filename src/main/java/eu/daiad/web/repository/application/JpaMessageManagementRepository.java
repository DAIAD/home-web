package eu.daiad.web.repository.application;

import eu.daiad.web.domain.application.Account;
import eu.daiad.web.domain.application.AccountAlert;
import eu.daiad.web.domain.application.AccountAlertProperty;
import eu.daiad.web.domain.application.AccountDynamicRecommendation;
import eu.daiad.web.domain.application.AccountDynamicRecommendationProperty;
import eu.daiad.web.domain.application.AccountRole;
import eu.daiad.web.domain.application.AlertTranslation;
import eu.daiad.web.domain.application.DynamicRecommendationTranslation;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.recommendation.EnumAlerts;
import eu.daiad.web.model.recommendation.EnumDynamicRecommendations;
import eu.daiad.web.model.recommendation.MessageCalculationConfiguration;
import eu.daiad.web.model.recommendation.Recommendation;
import eu.daiad.web.model.security.EnumRole;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA Repository to manage alerts/recommendations/static tips production.
 * 
 * @author nkarag
 */

@Repository
@Transactional("transactionManager")
@Scope("prototype") 
public class JpaMessageManagementRepository implements IMessageManagementRepository{
    
    @PersistenceContext(unitName="default")
    EntityManager entityManager;

    @Autowired
    IMessageCalculationRepository iMessagesRepository;     
         
    private MessageCalculationConfiguration config;
    private boolean cancelled = false;
    
    @Override
    public void execute(MessageCalculationConfiguration config) {
        this.config = config;
        for (Integer groupId : getAllUtilities()) {
            execute(groupId);    
            if(isCancelled()){
                return;
            }
        }
    }    

    @Override
    public void execute(int utilityId) {

        for (Account account : getUsersOfUtility(utilityId)) {
            execute(account);
            if(isCancelled()){
                return;
            }
        }
    }

    @Override
    public void execute(Account account) {
        if(isCancelled()){
            return;
        }
        computeAmphiroMessagesForUser(account);
        computeSmartWaterMeterMessagesForUser(account);
        
    }  
    @Override
    public void cancel() {
        cancelled = true;
    } 
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    } 
    
    private void computeAmphiroMessagesForUser(Account account) {
                
        //alertHotTemperatureAmphiro(account);        //inactive
        //alertShowerStillOnAmphiro(account);         //inactive
        alertTooMuchWaterConsumptionAmphiro(account);
        alertTooMuchEnergyAmphiro(account);               
        //alertNearDailyBudgetAmphiro(account);     //inactive
        //alertNearWeeklyBudgetAmphiro(account);    //inactive
        //alertReachedDailyBudgetAmphiro(account);
        //alertShowerChampionAmphiro(account);      //inactive       
        alertImprovedShowerEfficiencyAmphiro(account);
        
        recommendLessShowerTimeAmphiro(account); //TODO - change time intervals of computing. Use 3 months data
        recommendLowerTemperatureAmphiro(account);
        recommendLowerFlowAmphiro(account);//TODO - change time intervals of computing. Use 3 months data
        recommendShowerHeadChangeAmphiro(account);//TODO - change time intervals of computing. Use 3 months data
        recommendShampooAmphiro(account);//TODO - change time intervals of computing. Use 3 months data
        //recommendReduceFlowWhenNotNeededAmphiro(account); //inactive, moblile only.
        
    }

    private void computeSmartWaterMeterMessagesForUser(Account account) {
        alertWaterLeakSWM(account);
        alertWaterQualitySWM(account);
        alertPromptGoodJobMonthlySWM(account);
        //promptGoodJobWeeklySWM(account); using monthly for now.
        alertTooMuchWaterConsumptionSWM(account);
        alertReducedWaterUseSWM(account);
        
        alertNearDailyBudgetSWM(account);
        alertNearWeeklyBudgetSWM(account);                
        alertReachedDailyBudgetSWM(account);        
        alertWaterChampionSWM(account);        

        alertWaterEfficiencyLeaderSWM(account);       
        alertKeepUpSavingWaterSWM(account);
        
        alertLitresSavedSWM(account);
        alertTop25SaverSWM(account);
        alertTop10SaverSWM(account);
    }  
    
    //1 alert - Check for water leaks!
    private void alertWaterLeakSWM(Account account) {
        if(DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek() 
                || DateTime.now().getDayOfWeek() == DateTimeConstants.WEDNESDAY){
            boolean waterLeakFound = iMessagesRepository.alertWaterLeakSWM(account.getKey());        
            if (waterLeakFound) {
                createAccountAlert(account.getId(), EnumAlerts.WATER_LEAK.getValue(), DateTime.now());
            }
        }
    }
    
    //2 alert - Shower still on!
    private void alertShowerStillOnAmphiro(Account account) {
        boolean showerStillOn = iMessagesRepository.alertShowerStillOnAmphiro(account.getKey());
        if (showerStillOn) {
            createAccountAlert(account.getId(), EnumAlerts.SHOWER_ON.getValue(), DateTime.now());    
        }
    }
    
    //3 alert - water fixtures ignored
    //4 alert - unusual activity, no consumption patterns available yet, ignored
    
    //5 alert - Water quality not assured!
    private void alertWaterQualitySWM(Account account) {
        boolean badWaterQuality
                = iMessagesRepository.alertWaterQualitySWM(account.getKey());
        if (badWaterQuality) {
            createAccountAlert(account.getId(), EnumAlerts.WATER_QUALITY.getValue(), DateTime.now());
        }
    }

    //6 alert - Water too hot!
    private void alertHotTemperatureAmphiro(Account account) {
        boolean alertHotTemperature        
                = iMessagesRepository.alertHotTemperatureAmphiro(account.getKey());
        if (alertHotTemperature) {    
            createAccountAlert(account.getId(), EnumAlerts.HOT_TEMPERATURE.getValue(), DateTime.now());         
        }
    }

    //7 alert - Reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearDailyBudgetSWM(Account account){
        
        Map.Entry<Boolean, AbstractMap.SimpleEntry<Integer, Integer>> nearDailyBudget 
                = iMessagesRepository.alertNearDailyBudgetSWM(account.getKey());
        
        if (nearDailyBudget.getKey()) {
            int accountAlertId = createAccountAlert
                (account.getId(), EnumAlerts.NEAR_DAILY_WATER_BUDGET.getValue(), DateTime.now());

            setAccountAlertProperty
                (accountAlertId, config.getIntKey1(), nearDailyBudget.getValue().getKey().toString());
            setAccountAlertProperty
                (accountAlertId, config.getIntKey2(), nearDailyBudget.getValue().getValue().toString());           
        }           
    }
    
    //8 alert - Reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearWeeklyBudgetSWM(Account account){
        
        //compute daily
        Map.Entry<Boolean, AbstractMap.SimpleEntry<Integer, Integer>> nearWeeklyBudget 
                = iMessagesRepository.alertNearWeeklyBudgetSWM(account.getKey());

        if (nearWeeklyBudget.getKey()) {
            int accountAlertId = createAccountAlert
                (account.getId(), EnumAlerts.NEAR_WEEKLY_WATER_BUDGET.getValue(), DateTime.now());

            setAccountAlertProperty
                (accountAlertId, config.getIntKey1(), nearWeeklyBudget.getValue().getKey().toString());
            setAccountAlertProperty
                (accountAlertId, config.getIntKey2(), nearWeeklyBudget.getValue().getValue().toString());
        }        
    }

    //9 alert - Reached 80% of your daily shower budget {integer1} {integer2}
    private void alertNearDailyBudgetAmphiro(Account account){
        
        Map.Entry<Boolean, AbstractMap.SimpleEntry<Integer, Integer>> nearDailyShowerBudget 
                = iMessagesRepository.alertNearDailyBudgetAmphiro(account.getKey());
        
        if (nearDailyShowerBudget.getKey()) {

            int accountAlertId = createAccountAlert
                (account.getId(), EnumAlerts.NEAR_DAILY_SHOWER_BUDGET.getValue(), DateTime.now());

            setAccountAlertProperty
                (accountAlertId, config.getIntKey1(), nearDailyShowerBudget.getValue().getKey().toString());
            setAccountAlertProperty
                (accountAlertId, config.getIntKey2(), nearDailyShowerBudget.getValue().getValue().toString());           
        }          
    }   
    
    //10 alert - Reached 80% of your weekly shower budget {integer1} {integer2}
    private void alertNearWeeklyBudgetAmphiro(Account account){
        if(DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek() ){
            
            Map.Entry<Boolean, AbstractMap.SimpleEntry<Integer, Integer>> nearWeeklyShowerBudget 
                    = iMessagesRepository.alertNearWeeklyBudgetAmphiro(account.getKey());

            if (nearWeeklyShowerBudget.getKey()) {
                int accountAlertId = createAccountAlert
                    (account.getId(), EnumAlerts.NEAR_WEEKLY_SHOWER_BUDGET.getValue(), DateTime.now());

                setAccountAlertProperty
                    (accountAlertId, config.getIntKey1(), nearWeeklyShowerBudget.getValue().getKey().toString());
                setAccountAlertProperty
                    (accountAlertId, config.getIntKey2(), nearWeeklyShowerBudget.getValue().getValue().toString());
            }    
        }
    }     
    
    //11 alert - Reached daily Water Budget {integer1}
    private void alertReachedDailyBudgetSWM(Account account){
        
        Map.Entry<Boolean, Integer> reachedDailyWaterBudget 
                = iMessagesRepository.alertReachedDailyBudgetSWM(account.getKey());
        
        if (reachedDailyWaterBudget.getKey()) {

            int accountAlertId = createAccountAlert
                (account.getId(), EnumAlerts.REACHED_DAILY_WATER_BUDGET.getValue(), DateTime.now());

            setAccountAlertProperty
                (accountAlertId, config.getIntKey1(), reachedDailyWaterBudget.getValue().toString());            
        }          
    }    
    
    //12 alert - Reached daily Shower Budget {integer1}
    private void alertReachedDailyBudgetAmphiro(Account account){
        
        Map.Entry<Boolean, Integer> reachedDailyShowerBudget 
                = iMessagesRepository.alertReachedDailyBudgetSWM(account.getKey());
        
        if (reachedDailyShowerBudget.getKey()) {
            int accountAlertId = createAccountAlert
                (account.getId(), EnumAlerts.REACHED_DAILY_SHOWER_BUDGET.getValue(), DateTime.now());

            setAccountAlertProperty(accountAlertId, config.getIntKey1(), 
                reachedDailyShowerBudget.getValue().toString());
            
        }         
    }     
    
    //13 alert - You are a real water champion!
    private void alertWaterChampionSWM(Account account){
        //compute monthly
        if(DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth() ){
            boolean waterChampion = iMessagesRepository.alertWaterChampionSWM(account.getKey());

            if (waterChampion) {
                createAccountAlert(account.getId(), EnumAlerts.WATER_CHAMPION.getValue(), DateTime.now());
            }  
        }        
    }     
    
    //14 alert - You are a real shower champion!
    private void alertShowerChampionAmphiro(Account account){
        //compute monthly
        if(DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth() ){        
            boolean showerChampion = iMessagesRepository.alertShowerChampionAmphiro(account.getKey());

            if (showerChampion) {
                createAccountAlert(account.getId(), EnumAlerts.SHOWER_CHAMPION.getValue(), DateTime.now());
            }   
        }
    } 

    //15 alert - You are using too much water {integer1}
    private void alertTooMuchWaterConsumptionSWM(Account account) {
        if(DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek() ){
            AbstractMap.SimpleEntry<Boolean, Double> tooMuchWater
                    = iMessagesRepository.alertTooMuchWaterConsumptionSWM(account.getKey());
            if (tooMuchWater.getKey()) {
                
                int accountAlertId = createAccountAlert
                    (account.getId(), EnumAlerts.TOO_MUCH_WATER_SWM.getValue(), DateTime.now());

                setAccountAlertProperty(accountAlertId, config.getIntKey1(), tooMuchWater.getValue().toString());
            }
        }
    }

    //16 alert - You are using too much water in the shower {integer1}
    private void alertTooMuchWaterConsumptionAmphiro(Account account) {
        if(DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek() ){
            AbstractMap.SimpleEntry<Boolean, Double> tooMuchWater
                    = iMessagesRepository.alertTooMuchWaterConsumptionAmphiro(account.getKey());
            if (tooMuchWater.getKey()) {

                int accountAlertId = createAccountAlert(
                        account.getId(), EnumAlerts.TOO_MUCH_WATER_AMPHIRO.getValue(), DateTime.now());

                setAccountAlertProperty(accountAlertId, config.getIntKey1(), tooMuchWater.getValue().toString());
            }
        }
    }

    //17 alert - You are spending too much energy for showering {integer1} {currency}
    private void alertTooMuchEnergyAmphiro(Account account) {
        if(DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()){
            AbstractMap.SimpleEntry<Boolean, Double> tooMuchEnergy
                    = iMessagesRepository.alertTooMuchEnergyAmphiro(account.getKey());
            if (tooMuchEnergy.getKey()) {

                int accountAlertId = createAccountAlert
                    (account.getId(), EnumAlerts.TOO_MUCH_ENERGY.getValue(), DateTime.now());
                
                Double pricePerKWH;
                switch (account.getCountry()){
                    case "United Kingdom":
                        pricePerKWH = config.getAverageGbpPerKwh();
                        break;
                    case "Spain":
                        pricePerKWH = config.getEurosPerKwh();
                        break;
                    default:
                        pricePerKWH = config.getEurosPerKwh();
                        break;
                }
                
                Double annualShowerConsumption = tooMuchEnergy.getValue();
                Double eurosSavedPerYear = 
                        ((2 * annualShowerConsumption * 1000 * 1.163 * pricePerKWH) / 1000000);
                                   
                setAccountAlertProperty
                    (accountAlertId, config.getCurrencyKey1(), eurosSavedPerYear.toString());
            }
        }
    }

    //18 alert - Well done! You have greatly reduced your water use {integer1} percent
    private void alertReducedWaterUseSWM(Account account) {
        
        if(!isAlertAlreadyProducedForUser(EnumAlerts.REDUCED_WATER_USE.getValue(), account)){
            if(DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek() ){

                AbstractMap.SimpleEntry<Boolean, Integer> reducedWaterUseResult
                        = iMessagesRepository.alertReducedWaterUseSWM(account.getKey(), account.getCreatedOn());

                if (reducedWaterUseResult.getKey()) {

                    int accountAlertId = createAccountAlert
                        (account.getId(), EnumAlerts.REDUCED_WATER_USE.getValue(), DateTime.now());
                    setAccountAlertProperty
                        (accountAlertId, config.getIntKey1(), reducedWaterUseResult.getValue().toString());
                }
            }
        }
    }

    //19 alert - Well done! You have greatly improved your shower efficiency {integer1} percent   
    private void alertImprovedShowerEfficiencyAmphiro(Account account){
        if(!isAlertAlreadyProducedForUser(EnumAlerts.IMPROVED_SHOWER_EFFICIENCY.getValue(), account)){
            if(DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek() ){
                AbstractMap.SimpleEntry<Boolean, Integer> improvedShowerEfficiency 
                        = iMessagesRepository.alertImprovedShowerEfficiencyAmphiro
                            (account.getKey(), account.getCreatedOn());

                if (improvedShowerEfficiency.getKey()) {
                    int accountAlertId = createAccountAlert
                        (account.getId(), EnumAlerts.IMPROVED_SHOWER_EFFICIENCY.getValue(), DateTime.now());
                    setAccountAlertProperty
                        (accountAlertId, config.getIntKey1(), improvedShowerEfficiency.getValue().toString());
                }           
            }
        }
    }    
 
    //20 alert - Congratulations! You are a water efficiency leader {integer1} litres
    private void alertWaterEfficiencyLeaderSWM(Account account){

        if(DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth() ){
            AbstractMap.SimpleEntry<Boolean, Integer> waterEfficiencyLeader
                        = iMessagesRepository.alertWaterEfficiencyLeaderSWM(account.getKey());
            if(waterEfficiencyLeader.getKey()){
                int accountAlertId = createAccountAlert
                    (account.getId(), EnumAlerts.WATER_EFFICIENCY_LEADER.getValue(), DateTime.now());
                setAccountAlertProperty
                    (accountAlertId, config.getIntKey1(), waterEfficiencyLeader.getValue().toString());            
            }
        }
    }    
    
    //21 alert - Keep up saving water!
    private void alertKeepUpSavingWaterSWM(Account account){

        //compute only if the message list is empty
        if(!isAlertAlreadyProducedForUser(EnumAlerts.KEEP_UP_SAVING_WATER.getValue(), account)){
            createAccountAlert(account.getId(), EnumAlerts.KEEP_UP_SAVING_WATER.getValue(), DateTime.now());             
        }
    }    
    
    //22 alert - You are doing a great job!
    private void alertPromptGoodJobMonthlySWM(Account account) {   
        if(DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth() ){
            //SimpleEntry<Boolean, Integer> promptGoodJob
            boolean promptGoodJob        
                    = iMessagesRepository.alertPromptGoodJobMonthlySWM(account.getKey());
            if (promptGoodJob) {    
                createAccountAlert(account.getId(), EnumAlerts.GOOD_JOB_MONTHLY.getValue(), DateTime.now());                          
            }
        }
    }   
    
    //23 alert - You have already saved {integer1} litres of water!
    private void alertLitresSavedSWM(Account account) {       
        if(DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek() ){
            AbstractMap.SimpleEntry<Boolean, Integer> litresSaved     
                    = iMessagesRepository.alertLitresSavedSWM(account.getKey());

            if (litresSaved.getKey()) {    
                int accountAlertId = createAccountAlert
                    (account.getId(), EnumAlerts.LITERS_ALREADY_SAVED.getValue(), DateTime.now());
                
                setAccountAlertProperty(accountAlertId, config.getIntKey1(), litresSaved.getValue().toString());                           
            }
        }
    }     
    
    //24 alert - Congratulations! You are one of the top 25% savers in your region.
    private void alertTop25SaverSWM(Account account){
        if(DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek() ){
            boolean top25Saver
                        = iMessagesRepository.alertTop25SaverWeeklySWM(account.getKey());
            if(top25Saver){
                createAccountAlert(account.getId(), EnumAlerts.TOP_25_PERCENT_OF_SAVERS.getValue(), DateTime.now());            
            }
        }
    }    
    
    //25 alert - Congratulations! You are among the top group of savers in your city.
    private void alertTop10SaverSWM(Account account){
        if(DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek() ){
            boolean top10Saver
                        = iMessagesRepository.alertTop10SaverSWM(account.getKey());
            if(top10Saver){
                createAccountAlert
                    (account.getId(), EnumAlerts.TOP_10_PERCENT_OF_SAVERS.getValue(), DateTime.now());            
            }
        }
    }
    
    //1 recommendation - Spend 1 less minute in the shower and save {integer1} {integer2} 
    private void recommendLessShowerTimeAmphiro(Account account) {
        if(DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth() ){
            AbstractMap.SimpleEntry<Boolean, Integer> recommendLessShowerTime 
                = iMessagesRepository.recommendShampooChangeAmphiro(account.getKey());

            if (recommendLessShowerTime.getKey()) {

                int accountDynamicRecommendationId = createAccountDynamicRecommendation
                    (account.getId(), EnumDynamicRecommendations.LESS_SHOWER_TIME.getValue(), DateTime.now());            

                //Float euros = (float) (EUROS_PER_LITRE*recommendLessShowerTime.getValue());
                //will use liters instead of currency here.
                Integer liters1 = recommendLessShowerTime.getValue();
                Integer liters2 = 2*recommendLessShowerTime.getValue();
                
                setAccountDynamicRecommendationProperty
                    (accountDynamicRecommendationId, config.getIntKey1(), liters1.toString()); 
                setAccountDynamicRecommendationProperty
                    (accountDynamicRecommendationId, config.getIntKey2(), liters2.toString());            
            }
        }
    }    
    
    //2 recommendation - You could save {currency1} if you used a bit less hot water in the shower. {currency2} 
    private void recommendLowerTemperatureAmphiro(Account account) {
        if(DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth() ){
            AbstractMap.SimpleEntry<Boolean, Integer> recommendLowerTemperature 
                = iMessagesRepository.recommendLowerTemperatureAmphiro(account.getKey());

            if (recommendLowerTemperature.getKey()) {
                int accountDynamicRecommendationId = createAccountDynamicRecommendation
                    (account.getId(), EnumDynamicRecommendations.LOWER_TEMPERATURE.getValue(), DateTime.now());            

                Integer annualShowerConsumption = recommendLowerTemperature.getValue();

                //formula: degrees  * litres * kcal * kwh * kwh price

                //formula description:
                //http://antoine.frostburg.edu/chem/senese/101/thermo/faq/energy-required-for-temperature-rise.shtml
                //https://answers.yahoo.com/question/index?qid=20071209205616AADfWQ3           

                //1 calorie will raise the temperature of 1 gram of water 1 degree Celsius. 
                //1000 calories will raise the temperature of 1 litre of water 1 degree Celsius
                //1 cal is 1.163E-6 kWh (1.163*10^-6) 
                //https://www.unitjuggler.com/convert-energy-from-cal-to-kWh.html
                //kwh greek price is 0.224 euros clean 
                //http://www.adslgr.com/forum/threads/860523-%CE%A4%CE%B9%CE%BC%CE%AE-%CE
                //%BA%CE%B9%CE%BB%CE%BF%CE%B2%CE%B1%CF%84%CF%8E%CF%81%CE%B1%CF%82-%CE%94%CE%95%CE%97-2015
                //example:     
                //2 degrees, total 30 showers per month for 2 people, 40 liters per shower.
                //2*12*30*40*1000*1.163*10^-6*0.224 euros
                //=7.50 euros

                Double pricePerKWH;
                switch (account.getCountry()){
                    case "United Kingdom":
                        pricePerKWH = config.getAverageGbpPerKwh();
                        break;
                    case "Spain":
                        pricePerKWH = config.getEurosPerKwh();
                        break;
                    default:
                        pricePerKWH = config.getEurosPerKwh();
                        break;
                }               
                Double eurosSavedPerYear = ((2 *annualShowerConsumption* 1000 * 1.163 * pricePerKWH) / 1000000);                 
                //degrees will be a fixed number set to 2.
                setAccountDynamicRecommendationProperty
                        (accountDynamicRecommendationId, config.getCurrencyKey1(), eurosSavedPerYear.toString()); 
                setAccountDynamicRecommendationProperty
                        (accountDynamicRecommendationId, config.getCurrencyKey2(), eurosSavedPerYear.toString());
            }
        }
    }    
    
    //3 recommendation - Reduce the water flow in the shower and gain {integer1} {integer2}
    private void recommendLowerFlowAmphiro(Account account) {
        if(DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth() ){
            AbstractMap.SimpleEntry<Boolean, Integer> recommendLowerFlow 
                = iMessagesRepository.recommendLowerFlowAmphiro(account.getKey());

            if (recommendLowerFlow.getKey()) {
                int accountDynamicRecommendationId = createAccountDynamicRecommendation
                    (account.getId(), EnumDynamicRecommendations.LOWER_FLOW.getValue(), DateTime.now());            

                setAccountDynamicRecommendationProperty
                        (accountDynamicRecommendationId, config.getIntKey1(), recommendLowerFlow.getValue().toString()); 
                setAccountDynamicRecommendationProperty
                        (accountDynamicRecommendationId, config.getIntKey2(), recommendLowerFlow.getValue().toString());
            }
        }
    }     
    
    //4 recommendation - Change your showerhead and save {integer1} {integer2}
    private void recommendShowerHeadChangeAmphiro(Account account) {
        if(DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth() ){
            AbstractMap.SimpleEntry<Boolean, Integer> recommendShowerHeadChange 
                = iMessagesRepository.recommendShowerHeadChangeAmphiro(account.getKey());

            if (recommendShowerHeadChange.getKey()) {
                int accountDynamicRecommendationId = createAccountDynamicRecommendation
                    (account.getId(), EnumDynamicRecommendations.CHANGE_SHOWERHEAD.getValue(), DateTime.now());            

                Integer annualLitresSaved = recommendShowerHeadChange.getValue();

                setAccountDynamicRecommendationProperty
                    (accountDynamicRecommendationId, config.getIntKey1(), annualLitresSaved.toString());
                setAccountDynamicRecommendationProperty
                    (accountDynamicRecommendationId, config.getIntKey2(), annualLitresSaved.toString());
            }
        }
    }    
          
    //5 recommendation - Have you considered changing your shampoo? {integer1} percent
    private void recommendShampooAmphiro(Account account) {
        if(DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth() ){
            AbstractMap.SimpleEntry<Boolean, Integer> recommendShampooChange 
                = iMessagesRepository.recommendShampooChangeAmphiro(account.getKey());

            if (recommendShampooChange.getKey()) {

                int accountDynamicRecommendationId = createAccountDynamicRecommendation
                    (account.getId(), EnumDynamicRecommendations.SHAMPOO_CHANGE.getValue(), DateTime.now());

                setAccountDynamicRecommendationProperty(accountDynamicRecommendationId, 
                        config.getIntKey1(), recommendShampooChange.getValue().toString());            
            }
        }
    }

    //6 recommendation - When showering, reduce the water flow when you do not need it {integer1} {integer2}
    private void recommendReduceFlowWhenNotNeededAmphiro(Account account) {
        if(DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth() ){
            AbstractMap.SimpleEntry<Boolean, Integer> recommendReduceFlow 
                = iMessagesRepository.recommendReduceFlowWhenNotNeededAmphiro(account.getKey());

            if (recommendReduceFlow.getKey()) {

                int accountDynamicRecommendationId = createAccountDynamicRecommendation(account.getId(),
                        EnumDynamicRecommendations.REDUCE_FLOW_WHEN_NOT_NEEDED.getValue(), DateTime.now());

                Integer moreShowerWaterThanOthers = recommendReduceFlow.getValue();
                
                setAccountDynamicRecommendationProperty
                    (accountDynamicRecommendationId, config.getIntKey1(), moreShowerWaterThanOthers.toString());                         
                setAccountDynamicRecommendationProperty
                    (accountDynamicRecommendationId, config.getIntKey2(), moreShowerWaterThanOthers.toString());             
            }
        }
    }    
    
    private List<Integer> getAllUtilities() {
        List<Integer> groups = new ArrayList<>();
        try {
            TypedQuery<eu.daiad.web.domain.application.Utility> query = entityManager
                    .createQuery("select a from utility a",
                            eu.daiad.web.domain.application.Utility.class);
            List<eu.daiad.web.domain.application.Utility> result = query.getResultList();
            for (eu.daiad.web.domain.application.Utility group : result) {
                groups.add(group.getId());
            }
            return groups;
        } catch (Exception ex) {
            throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
        }
    }

    private List<Account> getUsersOfUtility(int utilityId) {
        List<Account> userAccounts = new ArrayList<>();
        try {
            TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager
                    .createQuery("select a from account a where a.utility.id = :id",
                            eu.daiad.web.domain.application.Account.class);
            query.setParameter("id", utilityId);

            List<eu.daiad.web.domain.application.Account> result = query.getResultList();
            for (eu.daiad.web.domain.application.Account account : result) {
                for (AccountRole accountRole : account.getRoles()) {
                    if (accountRole.getRole().getName().equals(EnumRole.ROLE_USER.toString())) { 
                        userAccounts.add(getAccountByUsername(account.getUsername()));
                    }
                }
            }
            return userAccounts;
        } catch (Exception ex) {
            throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
        }
    }

    private Account getAccountByUsername(String username) {

        TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager
                .createQuery("select a from account a where a.username = :username",
                        eu.daiad.web.domain.application.Account.class).setFirstResult(0)
                .setMaxResults(1);
        query.setParameter("username", username);
        Account account = query.getSingleResult();
        
        return account;
    }

    private List<AccountAlert> getAccountAlertsByUser(Account account){
        int accountId = account.getId();
        
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> query = entityManager
                .createQuery("select a from account_alert a where a.accountId = :accountId",
                        eu.daiad.web.domain.application.AccountAlert.class);
        query.setParameter("accountId", accountId);
       
        return query.getResultList();
    }
    
    private boolean isAlertAlreadyProducedForUser(int alertId, Account account){
        
        int accountId = account.getId();
        
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> query = entityManager.createQuery
            ("select a from account_alert a where a.accountId = :accountId and a.alertId = :alertId",
                eu.daiad.web.domain.application.AccountAlert.class).setFirstResult(0)
                    .setMaxResults(1);
        query.setParameter("accountId", accountId);
        query.setParameter("alertId", alertId);        
        List<AccountAlert> resultsList = query.getResultList();

        return !resultsList.isEmpty(); 

    } 
    
    private void setAccountAlertProperty(int accountAlertId, String key, String value){
        AccountAlertProperty accountAlertProperty = new AccountAlertProperty();
        accountAlertProperty.setAccountAlertId(accountAlertId);
        accountAlertProperty.setKey(key);
        accountAlertProperty.setValue(value);
        entityManager.persist(accountAlertProperty);
        entityManager.flush();  
             
    }
    
    private void setAccountDynamicRecommendationProperty(int accountDynamicRecommendationId, String key, String value){
        AccountDynamicRecommendationProperty accountDynamicRecommendationProperty
                = new AccountDynamicRecommendationProperty();
        accountDynamicRecommendationProperty.setAccountDymanicRecommendationId(accountDynamicRecommendationId);
        accountDynamicRecommendationProperty.setKey(key);
        accountDynamicRecommendationProperty.setValue(value);
        entityManager.persist(accountDynamicRecommendationProperty);
        entityManager.flush();       
    }    
    
    private int createAccountAlert(int accountId, int alertId, DateTime timestamp){
        AccountAlert accountAlert = new AccountAlert();
        accountAlert.setAccountId(accountId);
        accountAlert.setAlertId(alertId);
        accountAlert.setCreatedOn(timestamp);
        entityManager.persist(accountAlert);
        entityManager.flush();
        
        return accountAlert.getId();
    }
    
    private int createAccountDynamicRecommendation(int accountId, int recommendationId, DateTime createdOn){
        AccountDynamicRecommendation accountDynamicRecommendation = new AccountDynamicRecommendation();
        accountDynamicRecommendation.setAccountId(accountId);
        accountDynamicRecommendation.setDynamicRecommendationId(recommendationId);
        accountDynamicRecommendation.setCreatedOn(createdOn);
        entityManager.persist(accountDynamicRecommendation);
        entityManager.flush(); 
        return accountDynamicRecommendation.getId();
    }

    private Recommendation createRecommendation(DynamicRecommendationTranslation recommendationTranslated,
            String titleFormatted, String descriptionFormatted){
        
        Recommendation recommendation = new Recommendation();
        recommendation.setId(recommendationTranslated.getDynamicRecommendationId());
        recommendation.setType(EnumDynamicRecommendations.getType());
        recommendation.setTitle(titleFormatted);
        recommendation.setDescription(descriptionFormatted);
        recommendation.setImageLink(recommendationTranslated.getImageLink());

        return recommendation;
    }       

    private Recommendation createAlert(AlertTranslation alertTranslated, String titleFormatted,
                                                                        String descriptionFormatted){

        Recommendation alert = new Recommendation();
        alert.setId(alertTranslated.getAlertId());
        alert.setType(EnumAlerts.getType());
        alert.setTitle(titleFormatted);
        alert.setDescription(descriptionFormatted);
        alert.setImageLink(alertTranslated.getImageLink());

        return alert;
    } 
    
    private Recommendation createAlert(AlertTranslation alertTranslated, String titleFormatted){

        Recommendation alert = new Recommendation();
        alert.setId(alertTranslated.getAlertId());
        alert.setType(EnumAlerts.getType());
        alert.setTitle(titleFormatted);
        alert.setImageLink(alertTranslated.getImageLink());
        
        return alert;
    }
    
    private float convertCurrencyIfNeed(float euros, Locale currencySymbol){
        //this is dummy method for future use. Currently returns only euros. 
        //The currency is converted in the message computation for now and only for KWH prices         
        if(currencySymbol.equals(Locale.GERMANY)){
            return euros;
        }
        else if(currencySymbol.equals(Locale.UK)){
            return euros;
            //return (float) (euros*0.8); //get currency rate from db
        }
        else{
            return euros;
        }
    }   
    
    @Override
    public void testMethodCreateMessagesForDummyUser(MessageCalculationConfiguration config) {
        int accountId = 6666;
        
        //Alerts        
        //1
        createAccountAlert(accountId, EnumAlerts.WATER_LEAK.getValue(), DateTime.now());
        
        //5
        createAccountAlert(accountId, EnumAlerts.WATER_QUALITY.getValue(), DateTime.now());
        
        //7
        int accountAlertId7 = createAccountAlert
            (accountId, EnumAlerts.NEAR_DAILY_WATER_BUDGET.getValue(), DateTime.now());

        setAccountAlertProperty(accountAlertId7, config.getIntKey1(), "66");
        setAccountAlertProperty(accountAlertId7, config.getIntKey2(), "96");   
        
        //8
        int accountAlertId8 = createAccountAlert
            (accountId, EnumAlerts.NEAR_WEEKLY_WATER_BUDGET.getValue(), DateTime.now());

        setAccountAlertProperty(accountAlertId8, config.getIntKey1(), "366");
        setAccountAlertProperty(accountAlertId8, config.getIntKey2(), "466");
        
        //11
        int accountAlertId11 = createAccountAlert
            (accountId, EnumAlerts.REACHED_DAILY_WATER_BUDGET.getValue(), DateTime.now());
        
        setAccountAlertProperty(accountAlertId11, config.getIntKey1(), "106"); 
        
        //12
        int accountAlertId12 = createAccountAlert
            (accountId, EnumAlerts.REACHED_DAILY_SHOWER_BUDGET.getValue(), DateTime.now());

        setAccountAlertProperty(accountAlertId12, config.getIntKey1(), "96");  
        
        //13
        createAccountAlert(accountId, EnumAlerts.WATER_CHAMPION.getValue(), DateTime.now());
        
        //14
        createAccountAlert(accountId, EnumAlerts.SHOWER_CHAMPION.getValue(), DateTime.now());

        //15
        int accountAlertId15 = createAccountAlert
            (accountId, EnumAlerts.TOO_MUCH_WATER_SWM.getValue(), DateTime.now());

        setAccountAlertProperty(accountAlertId15, config.getIntKey1(), "10166");
        
        //16
        int accountAlertId16 = createAccountAlert
            (accountId, EnumAlerts.TOO_MUCH_WATER_AMPHIRO.getValue(), DateTime.now());

        setAccountAlertProperty(accountAlertId16, config.getIntKey1(), "7166");
        
        //17
        int accountAlertId17 = createAccountAlert
            (accountId, EnumAlerts.TOO_MUCH_ENERGY.getValue(), DateTime.now());
        setAccountAlertProperty(accountAlertId17, config.getCurrencyKey1(), "36");        
        
        //18
        int accountAlertId18 = createAccountAlert
            (accountId, EnumAlerts.REDUCED_WATER_USE.getValue(), DateTime.now());
       
        setAccountAlertProperty(accountAlertId18, config.getIntKey1(), "26");
       
        //19
        int accountAlertId19 = createAccountAlert
            (accountId, EnumAlerts.IMPROVED_SHOWER_EFFICIENCY.getValue(), DateTime.now());
        
        setAccountAlertProperty(accountAlertId19, config.getIntKey1(), "36");
 
        //20
        int accountAlertId20 = createAccountAlert
            (accountId, EnumAlerts.WATER_EFFICIENCY_LEADER.getValue(), DateTime.now());
                
        setAccountAlertProperty(accountAlertId20, config.getIntKey1(), "9166"); 
        
        //21
        createAccountAlert(accountId, EnumAlerts.KEEP_UP_SAVING_WATER.getValue(), DateTime.now());
        
        //22
        createAccountAlert(accountId, EnumAlerts.GOOD_JOB_MONTHLY.getValue(), DateTime.now());
        
        //23
        int accountAlertId23 = createAccountAlert
            (accountId, EnumAlerts.LITERS_ALREADY_SAVED.getValue(), DateTime.now());
                
        setAccountAlertProperty(accountAlertId23, config.getIntKey1(), "266"); 
        
        //24
        createAccountAlert(accountId, EnumAlerts.TOP_25_PERCENT_OF_SAVERS.getValue(), DateTime.now());
        
        //25
        createAccountAlert(accountId, EnumAlerts.TOP_10_PERCENT_OF_SAVERS.getValue(), DateTime.now());        
        
               
        //Recommendations
        
        //1
        int accountDynamicRecommendationId1 = createAccountDynamicRecommendation
            (accountId, EnumDynamicRecommendations.LESS_SHOWER_TIME.getValue(), DateTime.now());            

        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId1, config.getIntKey1(), "166"); 
        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId1, config.getIntKey2(), "166");            
        
        
        //2
        int accountDynamicRecommendationId2 = createAccountDynamicRecommendation
            (accountId, EnumDynamicRecommendations.LOWER_TEMPERATURE.getValue(), DateTime.now());            

        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId2, config.getCurrencyKey1(), "36"); 
        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId2, config.getCurrencyKey2(), "36");

        //3
        int accountDynamicRecommendationId3 = createAccountDynamicRecommendation
            (accountId, EnumDynamicRecommendations.LOWER_FLOW.getValue(), DateTime.now());            

        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId3, config.getIntKey1(), "19166"); 
        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId3, config.getIntKey2(), "19166");
        
        //4
        int accountDynamicRecommendationId4 = createAccountDynamicRecommendation
            (accountId, EnumDynamicRecommendations.CHANGE_SHOWERHEAD.getValue(), DateTime.now());            

        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId4, config.getIntKey1(), "9966");
        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId4, config.getIntKey2(), "9966");    
        
        //5
        int accountDynamicRecommendationId5 = createAccountDynamicRecommendation
                    (accountId, EnumDynamicRecommendations.SHAMPOO_CHANGE.getValue(), DateTime.now());

        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId5, config.getIntKey1(), "21");        
 
        //6
        int accountDynamicRecommendationId6 = createAccountDynamicRecommendation(accountId,
                        EnumDynamicRecommendations.REDUCE_FLOW_WHEN_NOT_NEEDED.getValue(), DateTime.now());

        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId6, config.getIntKey1(), "13166");                         
        setAccountDynamicRecommendationProperty(accountDynamicRecommendationId6, config.getIntKey2(), "13166");         
    }
    
}
