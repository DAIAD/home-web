package eu.daiad.web.service.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Locale;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.domain.application.AccountDynamicRecommendationEntity;
import eu.daiad.web.domain.application.StaticRecommendationEntity;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.DynamicRecommendation;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;
import eu.daiad.web.model.message.IMessageResolutionStatus;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.repository.application.IAccountAlertRepository;
import eu.daiad.web.repository.application.IAccountDynamicRecommendationRepository;
import eu.daiad.web.repository.application.IAccountStaticRecommendationRepository;
import eu.daiad.web.repository.application.IStaticRecommendationRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IPriceDataService;

@Service
public class DefaultMessageManagementService implements IMessageManagementService 
{
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
    
    @Autowired
    IPriceDataService priceData;
    
    @Override
    public void executeAccount(
        MessageCalculationConfiguration config,
        ConsumptionStats stats, MessageResolutionPerAccountStatus messageStatus, UUID accountKey) 
    {
        AccountEntity account = userRepository.getAccountByKey(accountKey);
        executeAccount(config, stats, messageStatus, account);
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

        alertHotTemperatureAmphiro(config, messageStatus, account); //inactive
        //alertShowerStillOnAmphiro(config, messageStatus, account); //inactive
        alertTooMuchWaterConsumptionAmphiro(config, messageStatus, account);
        alertTooMuchEnergyAmphiro(config, messageStatus, account);
        //alertNearDailyBudgetAmphiro(config, messageStatus, account); //inactive
        //alertNearWeeklyBudgetAmphiro(config, messageStatus, account); //inactive
        //alertReachedDailyBudgetAmphiro(config, messageStatus, account); // inactive
        //alertShowerChampionAmphiro(config, messageStatus, account); //inactive
        alertImprovedShowerEfficiencyAmphiro(config, messageStatus, account);

        recommendLessShowerTimeAmphiro(config, messageStatus, account);
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
            List<StaticRecommendationEntity> randomTips = tipRepository.random(locale, 3);
            for (StaticRecommendationEntity r: randomTips)
                accountStaticRecommendationRepository.createWith(account, r.getId());
        } else if (config.isOnDemandExecution() || now.getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.isStaticTipToBeProduced()) {
                StaticRecommendationEntity r = tipRepository.randomOne(locale);
                if (r != null)
                    accountStaticRecommendationRepository.createWith(account, r.getId());
            }
        }
    }

    // Alert #1 - Check for water leaks!
    private void alertWaterLeakSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertWaterLeakSWM();
        if (r == null || !r.isSignificant())
            return;
        
        if (countAlertsByType(account, EnumAlertType.WATER_LEAK) > 3)
            return;
        
        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.WATER_LEAK);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #2 - Shower still on!
    private void alertShowerStillOnAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertShowerStillOnAmphiro();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.SHOWER_ON);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #3 - water fixtures ignored
    
    // Alert #4 - unusual activity, no consumption patterns available yet: ignored

    // Alert #5 - water quality not assured!
    // Todo : This alert is appears repeatedly if user has not used any water
    private void alertWaterQualitySWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertWaterQualitySWM();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.WATER_QUALITY);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #6 - Water too hot!
    private void alertHotTemperatureAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertHotTemperatureAmphiro();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.HOT_TEMPERATURE);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #7 - reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearDailyBudgetSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    { 
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertNearDailyBudgetSWM();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.NEAR_DAILY_WATER_BUDGET);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #8 - reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearWeeklyBudgetSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertNearWeeklyBudgetSWM();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.NEAR_WEEKLY_WATER_BUDGET);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #9 - reached 80% of your daily shower budget {integer1} {integer2}
    private void alertNearDailyBudgetAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertNearDailyBudgetAmphiro();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.NEAR_DAILY_SHOWER_BUDGET);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #10 - reached 80% of your weekly shower budget {integer1} {integer2}
    private void alertNearWeeklyBudgetAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (DateTime.now().getDayOfWeek() != config.getComputeThisDayOfWeek())
            return;
            
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertNearWeeklyBudgetAmphiro();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.NEAR_WEEKLY_SHOWER_BUDGET);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #11 - reached daily Water Budget {integer1}
    private void alertReachedDailyBudgetSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertReachedDailyBudgetSWM();
        if (r != null && r.isSignificant()) {    
            Assert.state(r.getParameters().getType() == EnumAlertType.REACHED_DAILY_WATER_BUDGET);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #12 - Reached daily Shower Budget {integer1}
    private void alertReachedDailyBudgetAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertReachedDailyBudgetAmphiro();
        if (r != null && r.isSignificant()) {    
            Assert.state(r.getParameters().getType() == EnumAlertType.REACHED_DAILY_SHOWER_BUDGET);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #13 - You are a real water champion!
    private void alertWaterChampionSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    { 
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertWaterChampionSWM();
        if (r != null && r.isSignificant()) {
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getParameters().getType() == EnumAlertType.WATER_CHAMPION);
                accountAlertRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Alert #14 - You are a real shower champion!
    private void alertShowerChampionAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertShowerChampionAmphiro();
        if (r != null && r.isSignificant()) {
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getParameters().getType() == EnumAlertType.SHOWER_CHAMPION);
                accountAlertRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Alert #15 - You are using too much water {integer1}
    private void alertTooMuchWaterConsumptionSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (countAlertsByTypeThisWeek(account, EnumAlertType.TOO_MUCH_WATER_METER) < 1) {
            IMessageResolutionStatus<Alert.Parameters> r = status.getAlertTooMuchWaterConsumptionSWM();
            if (r == null || !r.isSignificant())
                return;
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Assert.state(r.getParameters().getType() == EnumAlertType.TOO_MUCH_WATER_METER);
                accountAlertRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Alert #16 - You are using too much water in the shower {integer1}
    private void alertTooMuchWaterConsumptionAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (countAlertsByTypeThisWeek(account, EnumAlertType.TOO_MUCH_WATER_AMPHIRO) < 1) {
            IMessageResolutionStatus<Alert.Parameters> r = status.getAlertTooMuchWaterConsumptionAmphiro();
            if (r == null || !r.isSignificant())
                return;
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Assert.state(r.getParameters().getType() == EnumAlertType.TOO_MUCH_WATER_AMPHIRO);
                accountAlertRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Alert #17 - You are spending too much energy for showering {integer1} {currency}
    private void alertTooMuchEnergyAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (countAlertsByTypeThisWeek(account, EnumAlertType.TOO_MUCH_ENERGY) < 1) {          
            IMessageResolutionStatus<Alert.Parameters> r = status.getAlertTooMuchEnergyAmphiro();
            if (r == null || !r.isSignificant())
                return;
            
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Assert.state(r.getParameters().getType() == EnumAlertType.TOO_MUCH_ENERGY);
                accountAlertRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Alert #18 - well done! You have greatly reduced your water use {integer1} percent
    private void alertReducedWaterUseSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (countAlertsByType(account, EnumAlertType.REDUCED_WATER_USE) < 1) {
            IMessageResolutionStatus<Alert.Parameters> r = status.getAlertReducedWaterUseSWM();
            if (r == null || !r.isSignificant())
                return;
            
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Assert.state(r.getParameters().getType() == EnumAlertType.REDUCED_WATER_USE);
                accountAlertRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Alert #19 - well done! You have greatly improved your shower efficiency {integer1} percent
    private void alertImprovedShowerEfficiencyAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (countAlertsByType(account, EnumAlertType.REDUCED_WATER_USE_IN_SHOWER) < 1) {
            IMessageResolutionStatus<Alert.Parameters> r = status.getAlertReducedWaterUseAmphiro();
            if (r == null || !r.isSignificant())
                return;
            
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Assert.state(r.getParameters().getType() == EnumAlertType.REDUCED_WATER_USE_IN_SHOWER);
                accountAlertRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Alert #20 - you are a water efficiency leader {integer1} litres
    private void alertWaterEfficiencyLeaderSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (countAlertsByType(account, EnumAlertType.WATER_EFFICIENCY_LEADER) < 1) {
            IMessageResolutionStatus<Alert.Parameters> r = status.getAlertWaterEfficiencyLeaderSWM();
            if (r == null || !r.isSignificant())
                return;
            
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getParameters().getType() == EnumAlertType.WATER_EFFICIENCY_LEADER);
                accountAlertRepository.createWith(account, r.getParameters());
            }
        }    
    }

    // Alert #21 - Keep up saving water!
    private void alertKeepUpSavingWaterSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (countAlertsByType(account, EnumAlertType.KEEP_UP_SAVING_WATER) < 1) {
            Alert.Parameters parameters = new Alert.CommonParameters(
                DateTime.now(), EnumDeviceType.METER, EnumAlertType.KEEP_UP_SAVING_WATER);
            accountAlertRepository.createWith(account, parameters);
        }
    }

    // Alert #22 - You are doing a great job!
    private void alertPromptGoodJobMonthlySWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertPromptGoodJobMonthlySWM();
        if (r == null || !r.isSignificant())
            return;
        
        int day = DateTime.now().getDayOfMonth();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.GOOD_JOB_MONTHLY);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #23 - You have already saved {integer1} litres of water!
    private void alertLitresSavedSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertLitresSavedSWM();
        if (r == null || !r.isSignificant())
            return;
        
        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.LITERS_ALREADY_SAVED);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #24 - You are one of the top 25% savers in your region.
    private void alertTop25SaverSWM(
        MessageCalculationConfiguration config,
        MessageResolutionPerAccountStatus status, AccountEntity account) 
    {         
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertTop25SaverWeeklySWM();
        if (r == null || !r.isSignificant())
            return;
        
        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.TOP_25_PERCENT_OF_SAVERS);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Alert #25 - You are among the top 10% of savers in your region.
    private void alertTop10SaverSWM(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        IMessageResolutionStatus<Alert.Parameters> r = status.getAlertTop10SaverWeeklySWM();
        if (r == null || !r.isSignificant())
            return;
        
        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            Assert.state(r.getParameters().getType() == EnumAlertType.TOP_10_PERCENT_OF_SAVERS);
            accountAlertRepository.createWith(account, r.getParameters());
        }
    }

    // Recommendation #1 - Spend 1 less minute in the shower and save {integer1} {integer2}
    private void recommendLessShowerTimeAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {     
        if (countRecommendationsByTypeThisMonth(account, EnumDynamicRecommendationType.LESS_SHOWER_TIME) < 1) {         
            IMessageResolutionStatus<DynamicRecommendation.Parameters> r = status.getRecommendLessShowerTimeAmphiro();
            if (r == null || !r.isSignificant())
                return;
            
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getParameters().getType() == EnumDynamicRecommendationType.LESS_SHOWER_TIME);
                accountDynamicRecommendationRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Recommendation #2 - You could save {currency1} if you used a bit less hot water in the shower. {currency2}
    private void recommendLowerTemperatureAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (countRecommendationsByTypeThisMonth(account, EnumDynamicRecommendationType.LOWER_TEMPERATURE) < 1) {         
            IMessageResolutionStatus<DynamicRecommendation.Parameters> r = status.getRecommendLowerTemperatureAmphiro();
            if (r == null || !r.isSignificant())
                return;
            
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getParameters().getType() == EnumDynamicRecommendationType.LOWER_TEMPERATURE);
                accountDynamicRecommendationRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Recommendation #3 - Reduce the water flow in the shower and gain {integer1} {integer2}
    private void recommendLowerFlowAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (countRecommendationsByTypeThisMonth(account, EnumDynamicRecommendationType.LOWER_FLOW) < 1) {
            IMessageResolutionStatus<DynamicRecommendation.Parameters> r = status.getRecommendLowerFlowAmphiro();
            if (r == null || !r.isSignificant())
                return;
            
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getParameters().getType() == EnumDynamicRecommendationType.LOWER_FLOW);
                accountDynamicRecommendationRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Recommendation #4 - Change your shower head and save {integer1} {integer2}
    private void recommendShowerHeadChangeAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (countRecommendationsByTypeThisMonth(account, EnumDynamicRecommendationType.CHANGE_SHOWERHEAD) < 1) {
            IMessageResolutionStatus<DynamicRecommendation.Parameters> r = status.getRecommendLowerFlowAmphiro();
            if (r == null || !r.isSignificant())
                return;
            
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getParameters().getType() == EnumDynamicRecommendationType.CHANGE_SHOWERHEAD);
                accountDynamicRecommendationRepository.createWith(account, r.getParameters());
            }
        }
    }
    
    // Recommendation #5 - Have you considered changing your shampoo? {integer1} percent
    private void recommendShampooAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countRecommendationsByTypeThisMonth(account, EnumDynamicRecommendationType.SHAMPOO_CHANGE) < 1) {            
            IMessageResolutionStatus<DynamicRecommendation.Parameters> r = status.getRecommendShampooChangeAmphiro();
            if (r == null || !r.isSignificant())
                return;
            
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getParameters().getType() == EnumDynamicRecommendationType.SHAMPOO_CHANGE);
                accountDynamicRecommendationRepository.createWith(account, r.getParameters());
            }
        }
    }

    // Recommendation #6 - When showering, reduce the water flow when you dont need it {integer1} {integer2}
    private void recommendReduceFlowWhenNotNeededAmphiro(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (countRecommendationsByTypeThisMonth(account, EnumDynamicRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED) < 1) {          
            IMessageResolutionStatus<DynamicRecommendation.Parameters> r = status.getRecommendReduceFlowWhenNotNeededAmphiro();
            if (r == null || !r.isSignificant())
                return;
            
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getParameters().getType() == EnumDynamicRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED);
                accountDynamicRecommendationRepository.createWith(account, r.getParameters());
            }
        }
    }
    
    // Insights
    private void generateInsights(
        MessageCalculationConfiguration config, MessageResolutionPerAccountStatus messageStatus, AccountEntity account)
    {
        for (IMessageResolutionStatus<DynamicRecommendation.Parameters> r: messageStatus.getInsights()) {
            if (r != null && r.isSignificant())
                accountDynamicRecommendationRepository.createWith(account, r.getParameters());   
        }
    }
    
    //
    // ~ Helpers
    //
    
    private long countAlertsByType(AccountEntity account, EnumAlertType alertType)
    {
        UUID accountKey = account.getKey();
        return accountAlertRepository.countByAccountAndType(accountKey, alertType);
    }
    
    private long countAlertsByTypeThisWeek(AccountEntity account, EnumAlertType alertType) 
    {
        DateTimeZone tz = DateTimeZone.forID(account.getTimezone());
        DateTime now = DateTime.now(tz);
        DateTime t0 = now.withDayOfWeek(DateTimeConstants.MONDAY).withTime(0, 0, 0, 0);
        Interval interval = new Interval(t0, now);
        return accountAlertRepository.countByAccountAndType(account.getKey(), alertType, interval);
    }    
    
    private long countAlertsByTypeThisMonth(AccountEntity account, EnumAlertType alertType) 
    {
        DateTimeZone tz = DateTimeZone.forID(account.getTimezone());
        DateTime now = DateTime.now(tz);
        DateTime t0 = now.withDayOfMonth(0).withTime(0, 0, 0, 0);
        Interval interval = new Interval(t0, now);
        return accountAlertRepository.countByAccountAndType(account.getKey(), alertType, interval);
    } 
           
    private long countRecommendationsByTypeThisMonth(
        AccountEntity account, EnumDynamicRecommendationType recommendationType)
    {
        DateTimeZone tz = DateTimeZone.forID(account.getTimezone());
        DateTime now = DateTime.now(tz);
        DateTime t0 = now.withDayOfMonth(1).withTime(0, 0, 0, 0);
        Interval interval = new Interval(t0, now);
        return accountDynamicRecommendationRepository.countByAccountAndType(
            account.getKey(), recommendationType, interval);
    } 
}