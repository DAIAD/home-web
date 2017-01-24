package eu.daiad.web.service.message;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.StaticRecommendationEntity;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.EnumAlertTemplate;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.model.message.EnumRecommendationTemplate;
import eu.daiad.web.model.message.EnumRecommendationType;
import eu.daiad.web.model.message.IMessageResolutionStatus;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.model.message.Recommendation;
import eu.daiad.web.repository.application.IAccountAlertRepository;
import eu.daiad.web.repository.application.IAccountRecommendationRepository;
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
    IAccountRecommendationRepository accountRecommendationRepository;

    @Autowired
    IAccountStaticRecommendationRepository accountStaticRecommendationRepository;

    @Autowired
    IPriceDataService priceData;

    @Override
    public void executeAccount(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, MessageResolutionPerAccountStatus messageStatus, UUID accountKey)
    {
        AccountEntity account = userRepository.getAccountByKey(accountKey);
        executeAccount(config, stats, messageStatus, account);
    }

    private void executeAccount(
        IMessageGeneratorService.Configuration config,
        ConsumptionStats stats, MessageResolutionPerAccountStatus messageStatus, AccountEntity account)
    {
        generateMessages(config, stats, messageStatus, account);
        generateTips(config, messageStatus, account);
    }

    private void generateMessages(
        IMessageGeneratorService.Configuration config,
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
        IMessageGeneratorService.Configuration config, EnumDeviceType[] deviceTypes,
        ConsumptionStats stats, MessageResolutionPerAccountStatus messageStatus, AccountEntity account)
    {
        generateInsights(config, messageStatus, account);
    }

    private void generateMessagesForAmphiro(
        IMessageGeneratorService.Configuration config,
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
        IMessageGeneratorService.Configuration config, ConsumptionStats stats,
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
    private void generateTips(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
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
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertWaterLeakSWM();
        if (r == null || !r.isSignificant())
            return;

        if (countAlertsByType(account, EnumAlertType.WATER_LEAK) > 3)
            return;

        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.WATER_LEAK);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #2 - Shower still on!
    private void alertShowerStillOnAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertShowerStillOnAmphiro();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.SHOWER_ON);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #3 - water fixtures ignored

    // Alert #4 - unusual activity, no consumption patterns available yet: ignored

    // Alert #5 - water quality not assured!
    // Todo : This alert is appears repeatedly if user has not used any water
    private void alertWaterQualitySWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertWaterQualitySWM();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.WATER_QUALITY);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #6 - Water too hot!
    private void alertHotTemperatureAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertHotTemperatureAmphiro();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.HIGH_TEMPERATURE);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #7 - reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearDailyBudgetSWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertNearDailyBudgetSWM();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.NEAR_DAILY_WATER_BUDGET);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #8 - reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearWeeklyBudgetSWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertNearWeeklyBudgetSWM();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.NEAR_WEEKLY_WATER_BUDGET);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #9 - reached 80% of your daily shower budget {integer1} {integer2}
    private void alertNearDailyBudgetAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertNearDailyBudgetAmphiro();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.NEAR_DAILY_SHOWER_BUDGET);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #10 - reached 80% of your weekly shower budget {integer1} {integer2}
    private void alertNearWeeklyBudgetAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (DateTime.now().getDayOfWeek() != config.getComputeThisDayOfWeek())
            return;

        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertNearWeeklyBudgetAmphiro();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.NEAR_WEEKLY_SHOWER_BUDGET);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #11 - reached daily Water Budget {integer1}
    private void alertReachedDailyBudgetSWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertReachedDailyBudgetSWM();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.REACHED_DAILY_WATER_BUDGET);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #12 - Reached daily Shower Budget {integer1}
    private void alertReachedDailyBudgetAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertReachedDailyBudgetAmphiro();
        if (r != null && r.isSignificant()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.REACHED_DAILY_SHOWER_BUDGET);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #13 - You are a real water champion!
    private void alertWaterChampionSWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertWaterChampionSWM();
        if (r != null && r.isSignificant()) {
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.WATER_CHAMPION);
                accountAlertRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Alert #14 - You are a real shower champion!
    private void alertShowerChampionAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertShowerChampionAmphiro();
        if (r != null && r.isSignificant()) {
            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.SHOWER_CHAMPION);
                accountAlertRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Alert #15 - You are using too much water {integer1}
    private void alertTooMuchWaterConsumptionSWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countAlertsByTypeThisWeek(account, EnumAlertType.TOO_MUCH_WATER) < 1) {
            IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertTooMuchWaterConsumptionSWM();
            if (r == null || !r.isSignificant())
                return;
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.TOO_MUCH_WATER_METER);
                accountAlertRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Alert #16 - You are using too much water in the shower {integer1}
    private void alertTooMuchWaterConsumptionAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countAlertsByTypeThisWeek(account, EnumAlertType.TOO_MUCH_WATER) < 1) {
            IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertTooMuchWaterConsumptionAmphiro();
            if (r == null || !r.isSignificant())
                return;
            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.TOO_MUCH_WATER_SHOWER);
                accountAlertRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Alert #17 - You are spending too much energy for showering {integer1} {currency}
    private void alertTooMuchEnergyAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countAlertsByTypeThisWeek(account, EnumAlertType.TOO_MUCH_ENERGY) < 1) {
            IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertTooMuchEnergyAmphiro();
            if (r == null || !r.isSignificant())
                return;

            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.TOO_MUCH_ENERGY);
                accountAlertRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Alert #18 - well done! You have greatly reduced your water use {integer1} percent
    private void alertReducedWaterUseSWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countAlertsByType(account, EnumAlertType.REDUCED_WATER_USE) < 1) {
            IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertReducedWaterUseSWM();
            if (r == null || !r.isSignificant())
                return;

            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.REDUCED_WATER_USE_METER);
                accountAlertRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Alert #19 - well done! You have greatly improved your shower efficiency {integer1} percent
    private void alertImprovedShowerEfficiencyAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countAlertsByType(account, EnumAlertType.REDUCED_WATER_USE) < 1) {
            IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertReducedWaterUseAmphiro();
            if (r == null || !r.isSignificant())
                return;

            int day = DateTime.now().getDayOfWeek();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
                Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.REDUCED_WATER_USE_SHOWER);
                accountAlertRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Alert #20 - you are a water efficiency leader {integer1} litres
    private void alertWaterEfficiencyLeaderSWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countAlertsByType(account, EnumAlertType.WATER_EFFICIENCY_LEADER) < 1) {
            IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertWaterEfficiencyLeaderSWM();
            if (r == null || !r.isSignificant())
                return;

            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.WATER_EFFICIENCY_LEADER);
                accountAlertRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Alert #21 - Keep up saving water!
    private void alertKeepUpSavingWaterSWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countAlertsByType(account, EnumAlertType.KEEP_UP_SAVING_WATER) < 1) {
            Alert.ParameterizedTemplate parameters = new Alert.SimpleParameterizedTemplate(
                DateTime.now(), EnumDeviceType.METER, EnumAlertTemplate.KEEP_UP_SAVING_WATER);
            accountAlertRepository.createWith(account, parameters);
        }
    }

    // Alert #22 - You are doing a great job!
    private void alertPromptGoodJobMonthlySWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertPromptGoodJobMonthlySWM();
        if (r == null || !r.isSignificant())
            return;

        int day = DateTime.now().getDayOfMonth();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.GOOD_JOB_MONTHLY);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #23 - You have already saved {integer1} litres of water!
    private void alertLitresSavedSWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertLitresSavedSWM();
        if (r == null || !r.isSignificant())
            return;

        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.LITERS_ALREADY_SAVED);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #24 - You are one of the top 25% savers in your region.
    private void alertTop25SaverSWM(
        IMessageGeneratorService.Configuration config,
        MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertTop25SaverWeeklySWM();
        if (r == null || !r.isSignificant())
            return;

        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.TOP_25_PERCENT_OF_SAVERS);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Alert #25 - You are among the top 10% of savers in your region.
    private void alertTop10SaverSWM(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        IMessageResolutionStatus<Alert.ParameterizedTemplate> r = status.getAlertTop10SaverWeeklySWM();
        if (r == null || !r.isSignificant())
            return;

        int day = DateTime.now().getDayOfWeek();
        if (config.isOnDemandExecution() || day == config.getComputeThisDayOfWeek()) {
            Assert.state(r.getMessage().getTemplate() == EnumAlertTemplate.TOP_10_PERCENT_OF_SAVERS);
            accountAlertRepository.createWith(account, r.getMessage());
        }
    }

    // Recommendation #1 - Spend 1 less minute in the shower and save {integer1} {integer2}
    private void recommendLessShowerTimeAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countRecommendationsByTypeThisMonth(account, EnumRecommendationType.LESS_SHOWER_TIME) < 1) {
            IMessageResolutionStatus<Recommendation.ParameterizedTemplate> r = status.getRecommendLessShowerTimeAmphiro();
            if (r == null || !r.isSignificant())
                return;

            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getMessage().getTemplate() == EnumRecommendationTemplate.LESS_SHOWER_TIME);
                accountRecommendationRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Recommendation #2 - You could save {currency1} if you used a bit less hot water in the shower. {currency2}
    private void recommendLowerTemperatureAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countRecommendationsByTypeThisMonth(account, EnumRecommendationType.LOWER_TEMPERATURE) < 1) {
            IMessageResolutionStatus<Recommendation.ParameterizedTemplate> r = status.getRecommendLowerTemperatureAmphiro();
            if (r == null || !r.isSignificant())
                return;

            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getMessage().getTemplate() == EnumRecommendationTemplate.LOWER_TEMPERATURE);
                accountRecommendationRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Recommendation #3 - Reduce the water flow in the shower and gain {integer1} {integer2}
    private void recommendLowerFlowAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countRecommendationsByTypeThisMonth(account, EnumRecommendationType.LOWER_FLOW) < 1) {
            IMessageResolutionStatus<Recommendation.ParameterizedTemplate> r = status.getRecommendLowerFlowAmphiro();
            if (r == null || !r.isSignificant())
                return;

            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getMessage().getTemplate() == EnumRecommendationTemplate.LOWER_FLOW);
                accountRecommendationRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Recommendation #4 - Change your shower head and save {integer1} {integer2}
    private void recommendShowerHeadChangeAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countRecommendationsByTypeThisMonth(account, EnumRecommendationType.CHANGE_SHOWERHEAD) < 1) {
            IMessageResolutionStatus<Recommendation.ParameterizedTemplate> r = status.getRecommendShowerHeadChangeAmphiro();
            if (r == null || !r.isSignificant())
                return;

            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getMessage().getTemplate() == EnumRecommendationTemplate.CHANGE_SHOWERHEAD);
                accountRecommendationRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Recommendation #5 - Have you considered changing your shampoo? {integer1} percent
    private void recommendShampooAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countRecommendationsByTypeThisMonth(account, EnumRecommendationType.CHANGE_SHAMPOO) < 1) {
            IMessageResolutionStatus<Recommendation.ParameterizedTemplate> r = status.getRecommendShampooChangeAmphiro();
            if (r == null || !r.isSignificant())
                return;

            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getMessage().getTemplate() == EnumRecommendationTemplate.CHANGE_SHAMPOO);
                accountRecommendationRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Recommendation #6 - When showering, reduce the water flow when you dont need it {integer1} {integer2}
    private void recommendReduceFlowWhenNotNeededAmphiro(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if (countRecommendationsByTypeThisMonth(account, EnumRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED) < 1) {
            IMessageResolutionStatus<Recommendation.ParameterizedTemplate> r = status.getRecommendReduceFlowWhenNotNeededAmphiro();
            if (r == null || !r.isSignificant())
                return;

            int day = DateTime.now().getDayOfMonth();
            if (config.isOnDemandExecution() || day == config.getComputeThisDayOfMonth()) {
                Assert.state(r.getMessage().getTemplate() == EnumRecommendationTemplate.REDUCE_FLOW_WHEN_NOT_NEEDED);
                accountRecommendationRepository.createWith(account, r.getMessage());
            }
        }
    }

    // Insights
    private void generateInsights(
        IMessageGeneratorService.Configuration config, MessageResolutionPerAccountStatus messageStatus, AccountEntity account)
    {
        for (IMessageResolutionStatus<Recommendation.ParameterizedTemplate> r: messageStatus.getInsights()) {
            if (r != null && r.isSignificant())
                accountRecommendationRepository.createWith(account, r.getMessage());
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
        AccountEntity account, EnumRecommendationType recommendationType)
    {
        DateTimeZone tz = DateTimeZone.forID(account.getTimezone());
        DateTime now = DateTime.now(tz);
        DateTime t0 = now.withDayOfMonth(1).withTime(0, 0, 0, 0);
        Interval interval = new Interval(t0, now);
        return accountRecommendationRepository.countByAccountAndType(
            account.getKey(), recommendationType, interval);
    }
}