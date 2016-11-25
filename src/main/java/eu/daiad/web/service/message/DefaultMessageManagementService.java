package eu.daiad.web.service.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Locale;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import org.springframework.beans.factory.annotation.Autowired;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.domain.application.AccountDynamicRecommendationEntity;
import eu.daiad.web.domain.application.StaticRecommendationEntity;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.DynamicRecommendation;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.repository.application.IAccountAlertRepository;
import eu.daiad.web.repository.application.IAccountDynamicRecommendationRepository;
import eu.daiad.web.repository.application.IAccountStaticRecommendationRepository;
import eu.daiad.web.repository.application.IStaticRecommendationRepository;
import eu.daiad.web.repository.application.IUserRepository;

public class DefaultMessageManagementService implements IMessageManagementService 
{
    //@PersistenceContext(unitName = "default")
    //EntityManager entityManager;

    @Autowired
    IUserRepository userRepository;
    
    @Autowired
    IStaticRecommendationRepository tipRepository;
    
    @Autowired
    IAccountAlertRepository accountAlertRepository;
    
    @Autowired
    IAccountDynamicRecommendationRepository accountDynamicRecommendationRepository;
    
    @Autowired
    IAccountStaticRecommendationRepository accountStaticRecommendationRepository;
    
    @Override
    public void executeAccount(
        MessageCalculationConfiguration config,
        ConsumptionStats aggregates, MessageResolutionPerAccountStatus messageStatus, UUID accountKey) 
    {
        AccountEntity account = userRepository.getAccountByKey(accountKey);
        executeAccount(config, aggregates, messageStatus, account);
    }

    private void executeAccount(
        MessageCalculationConfiguration config,
        ConsumptionStats stats, MessageResolutionPerAccountStatus messageStatus, AccountEntity account) 
    {
        generateMessages(config, stats, messageStatus, account);    
        generateStaticTips(config, messageStatus, account);
    }

    private void generateMessages(
        MessageCalculationConfiguration config,
        ConsumptionStats stats, MessageResolutionPerAccountStatus messageStatus, AccountEntity account)
    {
        // Add messages common to all devices
        
        EnumDeviceType[] deviceTypes = new EnumDeviceType[] {
                EnumDeviceType.METER, EnumDeviceType.AMPHIRO
        };
        
        generateMessagesFor(config, deviceTypes, stats, messageStatus, account); 
        
        // Add AMPHIRO-only messages
        
        generateMessagesForAmphiro(config, stats, messageStatus, account);
        
        // Add METER-only messages
        
        generateMessagesForMeter(config, stats, messageStatus, account);
        
        return;
    }

    private void generateMessagesFor(
            MessageCalculationConfiguration config, EnumDeviceType[] deviceTypes, 
            ConsumptionStats stats, MessageResolutionPerAccountStatus messageStatus, AccountEntity account)
    {          
        generateInsights(config, messageStatus, account);
    }

    private void generateMessagesForAmphiro(
            MessageCalculationConfiguration config,
            ConsumptionStats stats, MessageResolutionPerAccountStatus messageStatus, AccountEntity account)
    {
        if (!messageStatus.isAmphiroInstalled()) {
            return;
        }

        //alertHotTemperatureAmphiro(config, messageStatus, account); //inactive
        //alertShowerStillOnAmphiro(config, messageStatus, account); //inactive
        alertTooMuchWaterConsumptionAmphiro(config, messageStatus, account);
        alertTooMuchEnergyAmphiro(config, messageStatus, account);
        //alertNearDailyBudgetAmphiro(config, messageStatus, account); //inactive
        //alertNearWeeklyBudgetAmphiro(config, messageStatus, account); //inactive
        //alertReachedDailyBudgetAmphiro(config, messageStatus, account); // inactive
        //alertShowerChampionAmphiro(config, messageStatus, account); //inactive
        alertImprovedShowerEfficiencyAmphiro(config, messageStatus, account);

        recommendLessShowerTimeAmphiro(config, stats, messageStatus, account);
        recommendLowerTemperatureAmphiro(config, messageStatus, account);
        recommendLowerFlowAmphiro(config, messageStatus, account);
        recommendShowerHeadChangeAmphiro(config, messageStatus, account);
        recommendShampooAmphiro(config, messageStatus, account);
        //recommendReduceFlowWhenNotNeededAmphiro(config, messageStatus, account); //inactive, mobile only.
    }

    private void generateMessagesForMeter(
            MessageCalculationConfiguration config, ConsumptionStats stats, 
            MessageResolutionPerAccountStatus messageStatus, AccountEntity account) 
    {
        if (!messageStatus.isMeterInstalled()) {
            return;
        }

        alertWaterLeakSWM(config, messageStatus, account);
        //alertWaterQualitySWM(config, messageStatus, account); // inactive
        //alertPromptGoodJobMonthlySWM(config, messageStatus, account); // inactive prompt
        alertTooMuchWaterConsumptionSWM(config, messageStatus, account);
        alertReducedWaterUseSWM(config, messageStatus, account);

        //alertNearDailyBudgetSWM(config, messageStatus, account); // inactive
        //alertNearWeeklyBudgetSWM(config, messageStatus, account); // inactive
        //alertReachedDailyBudgetSWM(config, messageStatus, account); // inactive
        //alertWaterChampionSWM(config, messageStatus, account); // inactive

        alertWaterEfficiencyLeaderSWM(config, messageStatus, account);
        //alertKeepUpSavingWaterSWM(config, messageStatus, account); //inactive prompt

        //alertLitresSavedSWM(config, messageStatus, account); //inactive prompt
        //alertTop25SaverSWM(config, messageStatus, account); //inactive
        //alertTop10SaverSWM(config, messageStatus, account); //inactive
    }

    // Static tips
    private void generateStaticTips(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        Locale locale = Locale.forLanguageTag(account.getLocale());
        DateTime now = DateTime.now();
        if (status.isInitialStaticTips()) {
            List<StaticRecommendationEntity> randomTips = tipRepository.rand(locale, 3);
            for (StaticRecommendationEntity r: randomTips)
                accountStaticRecommendationRepository.createWith(account, r.getId());
        } else if (config.isOnDemandExecution() || now.getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.isStaticTipToBeProduced()) {
                StaticRecommendationEntity r = tipRepository.randOne(locale);
                if (r != null)
                    accountStaticRecommendationRepository.createWith(account, r.getId());
            }
        }
    }

    // Alert #1 - Check for water leaks!
    private void alertWaterLeakSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (!status.isAlertWaterLeakSWM())
            return;
        
        int day = DateTime.now().getDayOfWeek();
        int numProducedAlerts = countProducedAlerts(EnumAlertType.WATER_LEAK, account);
        if (numProducedAlerts > 3)
            return;
        
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek())
        {      
            accountAlertRepository.createWith(account, EnumAlertType.WATER_LEAK, null);
        }
    }

    // Alert #2 - Shower still on!
    private void alertShowerStillOnAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.isAlertShowerStillOnAmphiro()) {
            accountAlertRepository.createWith(account, EnumAlertType.SHOWER_ON, null);
        }
    }

    // Alert #3 - water fixtures ignored
    
    // Alert #4 - unusual activity, no consumption patterns available yet: ignored

    // Alert #5 - water quality not assured!
    // Todo : This alert is appears repeatedly if user has not used any water
    private void alertWaterQualitySWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.isAlertWaterQualitySWM()) {
            accountAlertRepository.createWith(account, EnumAlertType.WATER_QUALITY, null);
        }
    }

    // Alert #6 - Water too hot!
    private void alertHotTemperatureAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.isAlertHotTemperatureAmphiro()) {
            accountAlertRepository.createWith(account, EnumAlertType.HOT_TEMPERATURE, null);
        }
    }

    // Alert #7 - reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearDailyBudgetSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.getAlertNearDailyBudgetSWM() != null) {
            Entry<Integer,Integer> e = status.getAlertNearDailyBudgetSWM();
            Integer i1 = e.getKey(), i2 = e.getValue();
            
            // Todo use Alert.CommonParameters
            Map<String, Object> p = new HashMap<>();
            p.put("integer1", i1);
            p.put("integer2", i2);
            
            accountAlertRepository.createWith(account, EnumAlertType.NEAR_DAILY_WATER_BUDGET, p);
        }
    }

    // Alert #8 - reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearWeeklyBudgetSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.getAlertNearWeeklyBudgetSWM() != null) {
            Entry<Integer,Integer> e = status.getAlertNearWeeklyBudgetSWM();
            Integer i1 = e.getKey(), i2 = e.getValue();
            
            // Todo use Alert.CommonParameters
            Map<String, Object> p = new HashMap<>();
            p.put("integer1", i1);
            p.put("integer2", i2);
            
            accountAlertRepository.createWith(account, EnumAlertType.NEAR_WEEKLY_WATER_BUDGET, p);
        }
    }

    // Alert #9 - reached 80% of your daily shower budget {integer1} {integer2}
    private void alertNearDailyBudgetAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.getAlertNearDailyBudgetAmphiro() != null) {
            Entry<Integer,Integer> e = status.getAlertNearDailyBudgetAmphiro();
            Integer i1 = e.getKey(), i2 = e.getValue();
            
            // Todo use Alert.CommonParameters
            Map<String, Object> p = new HashMap<>();
            p.put("integer1", i1);
            p.put("integer2", i2);
            
            accountAlertRepository.createWith(account, EnumAlertType.NEAR_DAILY_SHOWER_BUDGET, p);
        }
    }

    // Alert #10 - reached 80% of your weekly shower budget {integer1} {integer2}
    private void alertNearWeeklyBudgetAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (DateTime.now().getDayOfWeek() != config.getComputeThisDayOfWeek())
            return;
            
        if (status.getAlertNearWeeklyBudgetAmphiro() != null) {
            Entry<Integer,Integer> e = status.getAlertNearWeeklyBudgetAmphiro();
            Integer i1 = e.getKey(), i2 = e.getValue();

            // Todo use Alert.CommonParameters
            Map<String, Object> p = new HashMap<>();
            p.put("integer1", i1);
            p.put("integer2", i2);

            accountAlertRepository.createWith(account, EnumAlertType.NEAR_WEEKLY_SHOWER_BUDGET, p);
        }
    }

    // Alert #11 - reached daily Water Budget {integer1}
    private void alertReachedDailyBudgetSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        Entry<Boolean, Integer> e = status.getAlertReachedDailyBudgetSWM();
        if (e != null && e.getKey()) {    
            Integer i1 = e.getValue();
            
            // Todo use Alert.CommonParameters
            Map<String, Object> p = new HashMap<>();
            p.put("integer1", i1);
            
            accountAlertRepository.createWith(account, EnumAlertType.REACHED_DAILY_WATER_BUDGET, p);
        }
    }

    // Alert #12 - Reached daily Shower Budget {integer1}
    private void alertReachedDailyBudgetAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        Entry<Boolean, Integer> e = status.getAlertReachedDailyBudgetSWM();
        if (e != null && e.getKey()) {
            Integer i1 = e.getValue();
            
            // Todo use Alert.CommonParameters
            Map<String, Object> p = new HashMap<>();
            p.put("integer1", i1);
            
            accountAlertRepository.createWith(account, EnumAlertType.REACHED_DAILY_SHOWER_BUDGET, p);
        }

    }

    // Alert #13 - You are a real water champion!
    private void alertWaterChampionSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    { 
        boolean b = status.isAlertWaterChampionSWM();
        int day = DateTime.now().getDayOfMonth();
        if (b && (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth())) {
            accountAlertRepository.createWith(account, EnumAlertType.WATER_CHAMPION, null);
        }
    }

    // Alert #14 - You are a real shower champion!
    private void alertShowerChampionAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        boolean b = status.isAlertShowerChampionAmphiro();
        int day = DateTime.now().getDayOfMonth();
        if (b && (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth())) {
            accountAlertRepository.createWith(account, EnumAlertType.SHOWER_CHAMPION, null);
        }
    }

    // Alert #15 - You are using too much water {integer1}
    private void alertTooMuchWaterConsumptionSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (isAlertAlreadyProducedThisWeek(EnumAlertType.TOO_MUCH_WATER_SWM, account)) {
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Entry<Boolean, Double> e = status.getAlertTooMuchWaterConsumptionSWM();
                if (e !=  null && e.getKey()) {
                    Double x1 = e.getValue();
                    
                    // Todo use Alert.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("integer1", x1);
                    
                    accountAlertRepository.createWith(account, EnumAlertType.TOO_MUCH_WATER_SWM, p);
                }
            }
        }
    }

    // Alert #16 - You are using too much water in the shower {integer1}
    private void alertTooMuchWaterConsumptionAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (isAlertAlreadyProducedThisWeek(EnumAlertType.TOO_MUCH_WATER_AMPHIRO, account)) {
            int day = DateTime.now().getDayOfWeek();
            if(config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Entry<Boolean, Double> e = status.getAlertTooMuchWaterConsumptionAmphiro();
                if (e != null && e.getKey()) {
                    Double x1 = e.getValue();
                    
                    // Todo use Alert.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("integer1", x1);
                    
                    accountAlertRepository.createWith(account, EnumAlertType.TOO_MUCH_WATER_AMPHIRO, p);
                }
            }           
        }        
    }

    // Alert #17 - You are spending too much energy for showering {integer1} {currency}
    private void alertTooMuchEnergyAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (isAlertAlreadyProducedThisWeek(EnumAlertType.TOO_MUCH_ENERGY, account)) {          
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Entry<Boolean, Double> e = status.getAlertTooMuchEnergyAmphiro();
                if (e != null && e.getKey()) {
                    Double annualConsumption = e.getValue();
                    Double annualSavings = 
                        ((2 * annualConsumption * 1000 * 1.163 * getPricePerKwh(config, account)) / 1000000);
                    
                    // Todo use Alert.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("currency1", annualSavings);
                    
                    accountAlertRepository.createWith(account, EnumAlertType.TOO_MUCH_ENERGY, p);
                }
            }
        }
    }

    // Alert #18 - well done! You have greatly reduced your water use {integer1} percent
    private void alertReducedWaterUseSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (!isAlertAlreadyProduced(EnumAlertType.REDUCED_WATER_USE, account)) {
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Entry<Boolean, Integer> e = status.getAlertReducedWaterUseSWM();
                if (e != null && e.getKey()) {
                    Integer i1 = e.getValue();
                    
                    // Todo use Alert.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("integer1", i1);
                    
                    accountAlertRepository.createWith(account, EnumAlertType.REDUCED_WATER_USE, p);
                }
            }
        }
    }

    // Alert #19 - well done! You have greatly improved your shower efficiency {integer1} percent
    private void alertImprovedShowerEfficiencyAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (!isAlertAlreadyProduced(EnumAlertType.IMPROVED_SHOWER_EFFICIENCY, account)) {
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Entry<Boolean, Integer> e = status.getAlertImprovedShowerEfficiencyAmphiro();
                if (e != null && e.getKey()) {
                    Integer i1 = e.getValue();
                    
                    // Todo use Alert.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("integer1", i1);
                    
                    accountAlertRepository.createWith(account, EnumAlertType.IMPROVED_SHOWER_EFFICIENCY, p);
                }
            }
        }
    }

    // Alert #20 - you are a water efficiency leader {integer1} litres
    private void alertWaterEfficiencyLeaderSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (isAlertAlreadyProducedThisMonth(EnumAlertType.WATER_EFFICIENCY_LEADER, account)) {
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Entry<Boolean, Integer> e = status.getAlertWaterEfficiencyLeaderSWM();
                if (e != null && e.getKey()) {
                    Integer i1 = e.getValue();
                    
                    // Todo use Alert.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("integer1", i1);
                    
                    accountAlertRepository.createWith(account, EnumAlertType.WATER_EFFICIENCY_LEADER, p);
                }
            }
        }    
    }

    // Alert #21 - Keep up saving water!
    private void alertKeepUpSavingWaterSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (!isAlertAlreadyProduced(EnumAlertType.KEEP_UP_SAVING_WATER, account)) {
            accountAlertRepository.createWith(account, EnumAlertType.KEEP_UP_SAVING_WATER, null);
        }
    }

    // Alert #22 - You are doing a great job!
    private void alertPromptGoodJobMonthlySWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        int day = DateTime.now().getDayOfMonth();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
            if (status.isAlertPromptGoodJobMonthlySWM()) {
                accountAlertRepository.createWith(account, EnumAlertType.GOOD_JOB_MONTHLY, null);
            }
        }
    }

    // Alert #23 - You have already saved {integer1} litres of water!
    private void alertLitresSavedSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            Entry<Boolean, Integer> e = status.getAlertLitresSavedSWM();
            if (e != null && e.getKey()) {
                Integer i1 = e.getValue();
                
                // Todo use Alert.CommonParameters
                Map<String, Object> p = new HashMap<>();
                p.put("integer1", i1);
                
                accountAlertRepository.createWith(account, EnumAlertType.LITERS_ALREADY_SAVED, p);
            }
        }
    }

    // Alert #24 - You are one of the top 25% savers in your region.
    private void alertTop25SaverSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {         
        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            if (status.isAlertTop25SaverWeeklySWM()) {
                accountAlertRepository.createWith(account, EnumAlertType.TOP_25_PERCENT_OF_SAVERS, null);
            }
        }
    }

    // Alert #25 - You are among the top group of savers in your region.
    private void alertTop10SaverSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            if (status.isAlertTop10SaverSWM()) {
                accountAlertRepository.createWith(account, EnumAlertType.TOP_10_PERCENT_OF_SAVERS, null);
            }
        }
    }

    // Recommendation #1 - Spend 1 less minute in the shower and save {integer1} {integer2}
    private void recommendLessShowerTimeAmphiro(
        MessageCalculationConfiguration config, ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {     
        if (isRecommendationAlreadyProducedThisMonth(EnumDynamicRecommendationType.LESS_SHOWER_TIME, account)) {         
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Entry<Boolean, Integer> e = status.getRecommendShampooChangeAmphiro();
                if (e != null && e.getKey()) {
                    Integer i1 = e.getValue();
                    
                    // Todo use DynamicRecommendation.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("integer1", i1);
                    p.put("integer2", 2 * i1);
                    
                    accountDynamicRecommendationRepository.createWith(
                        account, EnumDynamicRecommendationType.LESS_SHOWER_TIME, p);
                }
            }
        }
    }

    // Recommendation #2 - You could save {currency1} if you used a bit less hot water in the shower. {currency2}
    private void recommendLowerTemperatureAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (isRecommendationAlreadyProducedThisMonth(EnumDynamicRecommendationType.LOWER_TEMPERATURE, account)) {         
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Entry<Boolean, Integer> e = status.getRecommendLowerTemperatureAmphiro();
                if (e != null && e.getKey()) {
                    Integer annualConsumption = e.getValue();
                    
                    // Formula: <degrees> * <litres> * <kcal> * <kwh> * <kwh price>
                    // http://antoine.frostburg.edu/chem/senese/101/thermo/faq/energy-required-for-temperature-rise.shtml
                    // https://answers.yahoo.com/question/index?qid=20071209205616AADfWQ3

                    // 1 calorie will raise the temperature of 1 gram of water 1 degree Celsius.
                    // 1000 calories will raise the temperature of 1 litre of water 1 degree Celsius
                    // 1 cal is 1.163E-6 kWh (1.163*10^-6)
                    // https://www.unitjuggler.com/convert-energy-from-cal-to-kWh.html
                    // kwh greek price is 0.224 euros clean
                    // http://www.adslgr.com/forum/threads/860523-%CE%A4%CE%B9%CE%BC%CE%AE-%CE
                    // %BA%CE%B9%CE%BB%CE%BF%CE%B2%CE%B1%CF%84%CF%8E%CF%81%CE%B1%CF%82-%CE%94%CE%95%CE%97-2015
                    
                    // Example:
                    // 2 degrees, total 30 showers per month for 2 people, 40 liters per shower.
                    // 2*12*30*40*1000*1.163*10^-6*0.224 euros = 7.50 euros
                    
                    Double  annualSavings = 
                        ((2 * annualConsumption * 1000 * 1.163 * getPricePerKwh(config, account)) / 1000000);
                    
                    // Todo use DynamicRecommendation.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("currency1", annualSavings);
                    p.put("currency2", annualSavings);
                    
                    accountDynamicRecommendationRepository.createWith(
                        account, EnumDynamicRecommendationType.LOWER_TEMPERATURE, p);
                }
            }
        }
    }

    // Recommendation #3 - Reduce the water flow in the shower and gain {integer1} {integer2}
    private void recommendLowerFlowAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (isRecommendationAlreadyProducedThisMonth(EnumDynamicRecommendationType.LOWER_FLOW, account)) {
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Entry<Boolean, Integer> e = status.getRecommendLowerFlowAmphiro();
                if (e != null && e.getKey()) {
                    Integer i1 = e.getValue();
                    
                    // Todo use DynamicRecommendation.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("integer1", i1);
                    p.put("integer2", i1);
                    
                    accountDynamicRecommendationRepository.createWith(
                        account, EnumDynamicRecommendationType.LOWER_FLOW, p);
                }
            }
        }
    }

    // Recommendation #4 - Change your shower head and save {integer1} {integer2}
    private void recommendShowerHeadChangeAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (!isRecommendationAlreadyProducedThisMonth(EnumDynamicRecommendationType.CHANGE_SHOWERHEAD, account)) {           
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Entry<Boolean, Integer> e = status.getRecommendShowerHeadChangeAmphiro();
                if (e != null && e.getKey()) {
                    Integer i1 = e.getValue();
                    
                    // Todo use DynamicRecommendation.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("integer1", i1);
                    p.put("integer2", i1);
                    
                    accountDynamicRecommendationRepository.createWith(
                        account, EnumDynamicRecommendationType.CHANGE_SHOWERHEAD, p);
                }
            }
        }
    }
    
    // Recommendation #5 - Have you considered changing your shampoo? {integer1} percent
    private void recommendShampooAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (!isRecommendationAlreadyProducedThisMonth(EnumDynamicRecommendationType.SHAMPOO_CHANGE, account)){            
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Entry<Boolean, Integer> e = status.getRecommendShampooChangeAmphiro();
                if (e != null && e.getKey()) {
                    Integer i1 = e.getValue();
                    
                    // Todo use DynamicRecommendation.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("integer1", i1);
                    
                    accountDynamicRecommendationRepository.createWith(
                        account, EnumDynamicRecommendationType.SHAMPOO_CHANGE, p);
                }
            }
        }
    }

    // Recommendation #6 - When showering, reduce the water flow when you dont need it {integer1} {integer2}
    private void recommendReduceFlowWhenNotNeededAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (!isRecommendationAlreadyProducedThisMonth(EnumDynamicRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED, account)){          
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Entry<Boolean, Integer> e = status.getRecommendReduceFlowWhenNotNeededAmphiro();
                if (e != null && e.getKey()) {
                    Integer i1 = e.getValue();
                    
                    // Todo use DynamicRecommendation.CommonParameters
                    Map<String, Object> p = new HashMap<>();
                    p.put("integer1", i1);
                    p.put("integer2", i1);
                    
                    accountDynamicRecommendationRepository.createWith(
                        account, EnumDynamicRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED, p);
                }
            }
        }
    }
    
    // Insights
    private void generateInsights(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus messageStatus, AccountEntity account)
    {
        for (MessageResolutionStatus<DynamicRecommendation.Parameters> m: messageStatus.getInsights()) {
            if (!m.isSignificant())
                continue;
            DynamicRecommendation.Parameters p = m.getParameters();
            accountDynamicRecommendationRepository.createWith(account, p.getType(), p.getPairs());   
        }
    }
    
    //
    // ~ Helpers
    //
    
    private boolean isAlertAlreadyProduced(EnumAlertType alertType, AccountEntity account) 
    {
        List<AccountAlertEntity> results = 
            accountAlertRepository.findByAccountAndType(account.getKey(), alertType);
        return !results.isEmpty();
    }
    
    private boolean isAlertAlreadyProducedThisWeek(EnumAlertType alertType, AccountEntity account) 
    {
        DateTime now = DateTime.now();
        Interval interval = new Interval(now.minusWeeks(1), now);
        List<AccountAlertEntity> results =
            accountAlertRepository.findByAccountAndType(account.getKey(), alertType, interval);
        return !results.isEmpty();
    }    
    
    private boolean isAlertAlreadyProducedThisMonth(EnumAlertType alertType, AccountEntity account) 
    {
        DateTime now = DateTime.now();
        Interval interval = new Interval(now.minusMonths(1), now);
        List<AccountAlertEntity> results =
            accountAlertRepository.findByAccountAndType(account.getKey(), alertType, interval);
        return !results.isEmpty();
    } 
        
    private int countProducedAlerts(EnumAlertType alertType, AccountEntity account) 
    {
        List<AccountAlertEntity> results = 
            accountAlertRepository.findByAccountAndType(account.getKey(), alertType);
        return results.size();
    }          
    
    private boolean isRecommendationAlreadyProducedThisMonth(
        EnumDynamicRecommendationType recommendationType, AccountEntity account)
    {
        DateTime now = DateTime.now();
        Interval interval = new Interval(now.minusMonths(1), now);
        List<AccountDynamicRecommendationEntity> results =
            accountDynamicRecommendationRepository.findByAccountAndType(
                account.getKey(), recommendationType, interval);
        return !results.isEmpty();
    } 
    
    private double getPricePerKwh(MessageCalculationConfiguration config, AccountEntity account)
    {
        double pricePerKwh;
        switch (account.getCountry()) {
        case "United Kingdom":
            pricePerKwh = config.getAverageGbpPerKwh();
            break;
        case "Spain":
        case "Greece":
        default:
            pricePerKwh = config.getEurosPerKwh();
            break;
        }
        return pricePerKwh;
    }
}