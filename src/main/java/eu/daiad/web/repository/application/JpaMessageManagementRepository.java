package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.domain.application.AccountAlertPropertyEntity;
import eu.daiad.web.domain.application.AccountDynamicRecommendationEntity;
import eu.daiad.web.domain.application.AccountDynamicRecommendationPropertyEntity;
import eu.daiad.web.domain.application.AccountRoleEntity;
import eu.daiad.web.domain.application.AccountStaticRecommendationEntity;
import eu.daiad.web.domain.application.AlertEntity;
import eu.daiad.web.domain.application.DynamicRecommendationEntity;
import eu.daiad.web.domain.application.StaticRecommendationEntity;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.message.DynamicRecommendation;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.BaseRepository;

@Repository()
@Transactional("applicationTransactionManager")
public class JpaMessageManagementRepository extends BaseRepository implements IMessageManagementRepository 
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public void executeAccount(
            MessageCalculationConfiguration config, ConsumptionStats aggregates,
            MessageResolutionPerAccountStatus messageStatus, UUID accountkey) 
    {
        AccountEntity account = getAccountByKey(accountkey);
        this.executeAccount(config, aggregates, messageStatus, account);
    }

    private void executeAccount(
            MessageCalculationConfiguration config, ConsumptionStats stats,
            MessageResolutionPerAccountStatus messageStatus, AccountEntity account) 
    {
        generateMessages(config, stats, messageStatus, account);    
        generateStaticTips(config, messageStatus, account);
    }

    @Override
    public DateTime getLastDateOfAccountStaticRecommendation(AuthenticatedUser user) 
    {
        DateTime lastCreatedOn = null;
        TypedQuery<AccountStaticRecommendationEntity> q = entityManager.createQuery(
                "select a from account_static_recommendation a " + 
                    "where a.account.id = :accountId order by a.createdOn desc",
                eu.daiad.web.domain.application.AccountStaticRecommendationEntity.class);
        q.setParameter("accountId", user.getId());

        List<AccountStaticRecommendationEntity> accountStaticRecommendations = q.getResultList();
        if (!accountStaticRecommendations.isEmpty()) {
            lastCreatedOn = accountStaticRecommendations.get(0).getCreatedOn();
        }

        return lastCreatedOn;
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

        //alertHotTemperatureAmphiro(account); //inactive
        //alertShowerStillOnAmphiro(account); //inactive
        alertTooMuchWaterConsumptionAmphiro(config, stats, messageStatus, account);
        alertTooMuchEnergyAmphiro(config, stats, messageStatus, account);
        //alertNearDailyBudgetAmphiro(account); //inactive
        //alertNearWeeklyBudgetAmphiro(account); //inactive
        //alertReachedDailyBudgetAmphiro(account);
        //alertShowerChampionAmphiro(account); //inactive
        alertImprovedShowerEfficiencyAmphiro(config, messageStatus, account);

        recommendLessShowerTimeAmphiro(config, stats, messageStatus, account);
        recommendLowerTemperatureAmphiro(config, stats, messageStatus, account);
        recommendLowerFlowAmphiro(config, stats, messageStatus, account);
        recommendShowerHeadChangeAmphiro(config, stats, messageStatus, account);
        recommendShampooAmphiro(config, stats, messageStatus, account);
        //recommendReduceFlowWhenNotNeededAmphiro(account); //inactive, mobile only.
    }

    private void generateMessagesForMeter(
            MessageCalculationConfiguration config, ConsumptionStats stats, 
            MessageResolutionPerAccountStatus messageStatus, AccountEntity account) 
    {
        if (!messageStatus.isMeterInstalled()) {
            return;
        }

        alertWaterLeakSWM(config, messageStatus, account);
        //alertWaterQualitySWM(config, status, account);
        // alertPromptGoodJobMonthlySWM(config, aggregates, status, account);
        // //inactive prompt
        // promptGoodJobWeeklySWM(account); using monthly for now.
        alertTooMuchWaterConsumptionSWM(config, stats, messageStatus, account);
        alertReducedWaterUseSWM(config, messageStatus, account);

        //alertNearDailyBudgetSWM(config, status, account);
        //alertNearWeeklyBudgetSWM(config, status, account);
        //alertReachedDailyBudgetSWM(config, status, account);
        //alertWaterChampionSWM(config, status, account);

        alertWaterEfficiencyLeaderSWM(config, stats, messageStatus, account);
        // alertKeepUpSavingWaterSWM(config, status, account); //inactive prompt

        // alertLitresSavedSWM(config, status, account); //inactive prompt
        // alertTop25SaverSWM(config, aggregates, status, account); //inactive
        // alertTop10SaverSWM(config, aggregates, status, account); //inactive
    }

    private AlertEntity getAlertByType(EnumAlertType type) 
    {
        TypedQuery<eu.daiad.web.domain.application.AlertEntity> query = entityManager.createQuery(
                    "select a from alert a where a.id = :id", 
                    eu.daiad.web.domain.application.AlertEntity.class)
                .setFirstResult(0)
                .setMaxResults(1);

        query.setParameter("id", type.getValue());
        return query.getSingleResult();
    }

    private DynamicRecommendationEntity getDynamicRecommendationByType(EnumDynamicRecommendationType type) {
        TypedQuery<eu.daiad.web.domain.application.DynamicRecommendationEntity> query = entityManager.createQuery(
                    "select d from dynamic_recommendation d where d.id = :id",
                    eu.daiad.web.domain.application.DynamicRecommendationEntity.class)
                .setFirstResult(0)
                .setMaxResults(1);

        query.setParameter("id", type.getValue());
        return query.getSingleResult();
    }

    private StaticRecommendationEntity getRandomStaticRecommendationForLocale(String accountLocale) {
        String locale;
        switch (accountLocale) {
            case "en":
                locale = accountLocale;
                break;
            case "es":
                locale = accountLocale;
                break;
            default:
                locale = "en";
        }

        TypedQuery<eu.daiad.web.domain.application.StaticRecommendationEntity> accountAlertsQuery = entityManager
                        .createQuery("select a from static_recommendation a where a.locale = :locale and a.active = :active",
                                        eu.daiad.web.domain.application.StaticRecommendationEntity.class);
        accountAlertsQuery.setParameter("locale", locale);
        accountAlertsQuery.setParameter("active", true);
        List<StaticRecommendationEntity> staticRecommendations = accountAlertsQuery.getResultList();

        if (staticRecommendations.isEmpty()) {
            return null;
        }

        Collections.shuffle(staticRecommendations);

        StaticRecommendationEntity singleRandomStaticRecommendation = staticRecommendations.get(0);

        return singleRandomStaticRecommendation;
    }

    private List<StaticRecommendationEntity> getInitialRandomStaticRecommendationForLocale(String accountLocale) 
    {
        List<StaticRecommendationEntity> initialTips = new ArrayList<>();
        String locale;
        if (StringUtils.isBlank(accountLocale)) {
            accountLocale = "";
        }

        switch (accountLocale) {
            case "en":
                locale = accountLocale;
                break;
            case "es":
                locale = accountLocale;
                break;
            default:
                locale = "en";
        }

        TypedQuery<eu.daiad.web.domain.application.StaticRecommendationEntity> accountAlertsQuery = entityManager
                        .createQuery("select a from static_recommendation a where a.locale = :locale and a.active = :active",
                                        eu.daiad.web.domain.application.StaticRecommendationEntity.class);
        accountAlertsQuery.setParameter("locale", locale);
        accountAlertsQuery.setParameter("active", true);
        List<StaticRecommendationEntity> staticRecommendations = accountAlertsQuery.getResultList();

        if (staticRecommendations.size() <= 3) {
            initialTips = staticRecommendations;
        } else {
            Collections.shuffle(staticRecommendations);
            for (int i = 0; i < 3; i++) {
                initialTips.add(staticRecommendations.get(i));
            }
        }
        return initialTips;
    }

    // random static tip
    private void generateStaticTips(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (status.isInitialStaticTips()) {
            List<StaticRecommendationEntity> randomTips = getInitialRandomStaticRecommendationForLocale(account.getLocale());
            for (StaticRecommendationEntity randomTip : randomTips) {
                createAccountStaticRecommendation(account, randomTip, DateTime.now());
            }
        } else if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.isStaticTipToBeProduced()) {
                StaticRecommendationEntity randomTip = getRandomStaticRecommendationForLocale(account.getLocale());
                if (randomTip != null) {
                    createAccountStaticRecommendation(account, randomTip, DateTime.now());
                }
            }
        }
    }

    // 1 alert - Check for water leaks!
    private void alertWaterLeakSWM(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if(!isAlertProducedOverThreeTimesForUser(EnumAlertType.WATER_LEAK.getValue(), account)){      
            if(config.isOnDemandExecution()){
                if (status.isAlertWaterLeakSWM()) {
                    createAccountAlert(account, getAlertByType(EnumAlertType.WATER_LEAK), DateTime.now());
                }
                return;
            }
            if (DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek() 
                    || DateTime.now().getDayOfWeek() == DateTimeConstants.WEDNESDAY) {
                if (status.isAlertWaterLeakSWM()) {
                    createAccountAlert(account, getAlertByType(EnumAlertType.WATER_LEAK), DateTime.now());
                }
            }            
        }
        
    }

    // 2 alert - Shower still on!
    private void alertShowerStillOnAmphiro(
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.isAlertShowerStillOnAmphiro()) {
            createAccountAlert(account, getAlertByType(EnumAlertType.SHOWER_ON), DateTime.now());
        }
    }

    // 3 alert - water fixtures ignored
    // 4 alert - unusual activity, no consumption patterns available yet: ignored

    // TODO : This alert is repeatedly appearing every time the job is executed
    // and the user has not used any water e.g. if the job is executed once per
    // hour, a new alert is generated every hour ...

    // 5 alert - Water quality not assured!
    private void alertWaterQualitySWM(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.isAlertWaterQualitySWM()) {
            createAccountAlert(account, getAlertByType(EnumAlertType.WATER_QUALITY), DateTime.now());
        }
    }

    // 6 alert - Water too hot!
    private void alertHotTemperatureAmphiro(
            MessageCalculationConfiguration config, 
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.isAlertHotTemperatureAmphiro()) {
            createAccountAlert(account, getAlertByType(EnumAlertType.HOT_TEMPERATURE), DateTime.now());
        }
    }

    // 7 alert - Reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearDailyBudgetSWM(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.getAlertNearDailyBudgetSWM() != null) {
            AccountAlertEntity alert = createAccountAlert(account, getAlertByType(EnumAlertType.NEAR_DAILY_WATER_BUDGET),
                            DateTime.now());
            setAccountAlertProperty(
                    alert, config.getIntKey1(), status.getAlertNearDailyBudgetSWM().getKey().toString());
            setAccountAlertProperty(
                    alert, config.getIntKey2(), status.getAlertNearDailyBudgetSWM().getValue().toString());
        }
    }

    // 8 alert - Reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearWeeklyBudgetSWM(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.getAlertNearWeeklyBudgetSWM() != null) {
            AccountAlertEntity alert = createAccountAlert(account, getAlertByType(EnumAlertType.NEAR_WEEKLY_WATER_BUDGET),
                            DateTime.now());
            setAccountAlertProperty(
                    alert, config.getIntKey1(), status.getAlertNearWeeklyBudgetSWM().getKey().toString());
            setAccountAlertProperty(
                    alert, config.getIntKey2(), status.getAlertNearWeeklyBudgetSWM().getValue().toString());
        }
    }

    // 9 alert - Reached 80% of your daily shower budget {integer1} {integer2}
    private void alertNearDailyBudgetAmphiro(MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status,
                    AccountEntity account) {
        if (status.getAlertNearDailyBudgetAmphiro() != null) {
            AccountAlertEntity alert = createAccountAlert(account, getAlertByType(EnumAlertType.NEAR_DAILY_SHOWER_BUDGET),
                            DateTime.now());

            setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertNearDailyBudgetAmphiro().getKey()
                            .toString());
            setAccountAlertProperty(alert, config.getIntKey2(), status.getAlertNearDailyBudgetAmphiro().getValue()
                            .toString());
        }
    }

    // 10 alert - Reached 80% of your weekly shower budget {integer1} {integer2}
    private void alertNearWeeklyBudgetAmphiro(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.getAlertNearWeeklyBudgetAmphiro() != null) {
                AccountAlertEntity alert = createAccountAlert(
                        account, getAlertByType(EnumAlertType.NEAR_WEEKLY_SHOWER_BUDGET), DateTime.now());
                setAccountAlertProperty(
                        alert, config.getIntKey1(), status.getAlertNearWeeklyBudgetAmphiro().getKey().toString());
                setAccountAlertProperty(
                        alert, config.getIntKey2(), status.getAlertNearWeeklyBudgetAmphiro().getValue().toString());
            }
        }
    }

    // 11 alert - Reached daily Water Budget {integer1}
    private void alertReachedDailyBudgetSWM(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.getAlertReachedDailyBudgetSWM() != null) {
            AccountAlertEntity alert = createAccountAlert(
                    account, getAlertByType(EnumAlertType.REACHED_DAILY_WATER_BUDGET), DateTime.now());
            setAccountAlertProperty(
                    alert, config.getIntKey1(), status.getAlertReachedDailyBudgetSWM().getValue().toString());
        }
    }

    // 12 alert - Reached daily Shower Budget {integer1}
    private void alertReachedDailyBudgetAmphiro(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (status.getAlertReachedDailyBudgetSWM() != null) {
            if (status.getAlertReachedDailyBudgetSWM().getKey()) {
                AccountAlertEntity alert = createAccountAlert(account,
                        getAlertByType(EnumAlertType.REACHED_DAILY_SHOWER_BUDGET), DateTime.now());
                setAccountAlertProperty(
                        alert, config.getIntKey1(), status.getAlertReachedDailyBudgetSWM().getValue().toString());
            }
        }

    }

    // 13 alert - You are a real water champion!
    private void alertWaterChampionSWM(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    { 
        if(config.isOnDemandExecution()){
            if (status.isAlertWaterChampionSWM()) {
                createAccountAlert(account, getAlertByType(EnumAlertType.WATER_CHAMPION), DateTime.now());
            }
            return;
        }          
        if (DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
            if (status.isAlertWaterChampionSWM()) {
                createAccountAlert(account, getAlertByType(EnumAlertType.WATER_CHAMPION), DateTime.now());
            }
        }
    }

    // 14 alert - You are a real shower champion!
    private void alertShowerChampionAmphiro(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if(config.isOnDemandExecution()){
            if (status.isAlertShowerChampionAmphiro()) {
                createAccountAlert(account, getAlertByType(EnumAlertType.SHOWER_CHAMPION), DateTime.now());
            }
            return;
        }            
        if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
            if (status.isAlertShowerChampionAmphiro()) {
                createAccountAlert(account, getAlertByType(EnumAlertType.SHOWER_CHAMPION), DateTime.now());
            }
        }
    }

    // 15 alert - You are using too much water {integer1}
    private void alertTooMuchWaterConsumptionSWM(
            MessageCalculationConfiguration config,
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if(isAlertAlreadyProducedThisWeekForUser(EnumAlertType.TOO_MUCH_WATER_SWM.getValue(), account)){
            if(config.isOnDemandExecution()){
                if (status.getAlertTooMuchWaterConsumptionSWM().getKey()) {
                    AccountAlertEntity alert = createAccountAlert(account, getAlertByType(EnumAlertType.TOO_MUCH_WATER_SWM),
                                    DateTime.now());
                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertTooMuchWaterConsumptionSWM()
                                    .getValue().toString());
                }
                return;
            }        
            if (DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
                if (status.getAlertTooMuchWaterConsumptionSWM().getKey()) {
                    AccountAlertEntity alert = createAccountAlert(account, getAlertByType(EnumAlertType.TOO_MUCH_WATER_SWM),
                                    DateTime.now());
                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertTooMuchWaterConsumptionSWM()
                                    .getValue().toString());
                }
            }            
        }
    }

    // 16 alert - You are using too much water in the shower {integer1}
    private void alertTooMuchWaterConsumptionAmphiro(
            MessageCalculationConfiguration config, 
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if(isAlertAlreadyProducedThisWeekForUser(EnumAlertType.TOO_MUCH_WATER_AMPHIRO.getValue(), account)){
            if(config.isOnDemandExecution()){
                if (status.getAlertTooMuchWaterConsumptionAmphiro().getKey()) {
                    AccountAlertEntity alert = createAccountAlert(account, getAlertByType(EnumAlertType.TOO_MUCH_WATER_AMPHIRO),
                                    DateTime.now());
                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertTooMuchWaterConsumptionAmphiro()
                                    .getValue().toString());
                }
                return;
            }
            if (DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
                if (status.getAlertTooMuchWaterConsumptionAmphiro().getKey()) {
                    AccountAlertEntity alert = createAccountAlert(account, getAlertByType(EnumAlertType.TOO_MUCH_WATER_AMPHIRO),
                                    DateTime.now());
                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertTooMuchWaterConsumptionAmphiro()
                                    .getValue().toString());
                }
            }            
        }        
    }

    // 17 alert - You are spending too much energy for showering {integer1} {currency}
    private void alertTooMuchEnergyAmphiro(
            MessageCalculationConfiguration config, 
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if(isAlertAlreadyProducedThisWeekForUser(EnumAlertType.TOO_MUCH_ENERGY.getValue(), account)){          
            if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
                if (status.getAlertTooMuchEnergyAmphiro().getKey()) {
                    AccountAlertEntity alert = createAccountAlert(
                            account, getAlertByType(EnumAlertType.TOO_MUCH_ENERGY), DateTime.now());
                    Double pricePerKWH;
                    switch (account.getCountry()) {
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
                    Double annualShowerConsumption = status.getAlertTooMuchEnergyAmphiro().getValue();
                    Double eurosSavedPerYear = ((2 * annualShowerConsumption * 1000 * 1.163 * pricePerKWH) / 1000000);
                    setAccountAlertProperty(alert, config.getCurrencyKey1(), eurosSavedPerYear.toString());
                }
            }
        }
    }

    // 18 alert - Well done! You have greatly reduced your water use {integer1} percent
    private void alertReducedWaterUseSWM(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (!isAlertAlreadyProducedForUser(EnumAlertType.REDUCED_WATER_USE.getValue(), account)) {
            if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
                if (status.getAlertReducedWaterUseSWM().getKey()) {
                    AccountAlertEntity alert = createAccountAlert(account, getAlertByType(EnumAlertType.REDUCED_WATER_USE),
                                    DateTime.now());

                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertReducedWaterUseSWM().getValue()
                                    .toString());
                }
            }
        }
    }

    // 19 alert - Well done! You have greatly improved your shower efficiency {integer1} percent
    private void alertImprovedShowerEfficiencyAmphiro(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {    
        if (!isAlertAlreadyProducedForUser(EnumAlertType.IMPROVED_SHOWER_EFFICIENCY.getValue(), account)) {
            if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
                if (status.getAlertImprovedShowerEfficiencyAmphiro().getKey()) {
                    AccountAlertEntity alert = createAccountAlert(account,
                            getAlertByType(EnumAlertType.IMPROVED_SHOWER_EFFICIENCY), DateTime.now());
                    setAccountAlertProperty(
                            alert, config.getIntKey1(),
                            status.getAlertImprovedShowerEfficiencyAmphiro().getValue().toString());
                }
            }
        }
    }

    // 20 alert - Congratulations! You are a water efficiency leader {integer1} litres
    private void alertWaterEfficiencyLeaderSWM(
            MessageCalculationConfiguration config, 
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if(isAlertAlreadyProducedThisMonthForUser(EnumAlertType.WATER_EFFICIENCY_LEADER.getValue(), account)){          
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getAlertWaterEfficiencyLeaderSWM().getKey()) {
                    AccountAlertEntity alert = createAccountAlert(account, getAlertByType(EnumAlertType.WATER_EFFICIENCY_LEADER),
                                    DateTime.now());

                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertWaterEfficiencyLeaderSWM()
                                    .getValue().toString());
                }
            }
        }    
    }

    // 21 alert - Keep up saving water!
    private void alertKeepUpSavingWaterSWM(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        // compute only if the message list is empty
        if (!isAlertAlreadyProducedForUser(EnumAlertType.KEEP_UP_SAVING_WATER.getValue(), account)) {
            createAccountAlert(account, getAlertByType(EnumAlertType.KEEP_UP_SAVING_WATER), DateTime.now());
        }
    }

    // 22 alert - You are doing a great job!
    private void alertPromptGoodJobMonthlySWM(
            MessageCalculationConfiguration config,
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
            if (status.isAlertPromptGoodJobMonthlySWM()) {
                createAccountAlert(account, getAlertByType(EnumAlertType.GOOD_JOB_MONTHLY), DateTime.now());
            }
        }
    }

    // 23 alert - You have already saved {integer1} litres of water!
    private void alertLitresSavedSWM(
            MessageCalculationConfiguration config, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.getAlertLitresSavedSWM().getKey()) {
                AccountAlertEntity alert = createAccountAlert(account, getAlertByType(EnumAlertType.LITERS_ALREADY_SAVED),
                                DateTime.now());
                setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertLitresSavedSWM().getValue().toString());
            }
        }
    }

    // 24 alert - Congratulations! You are one of the top 25% savers in your region.
    private void alertTop25SaverSWM(
            MessageCalculationConfiguration config, 
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {         
        if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.isAlertTop25SaverWeeklySWM()) {
                createAccountAlert(account, getAlertByType(EnumAlertType.TOP_25_PERCENT_OF_SAVERS), DateTime.now());
            }
        }
    }

    // 25 alert - Congratulations! You are among the top group of savers in your
    // city.
    private void alertTop10SaverSWM(MessageCalculationConfiguration config, ConsumptionStats aggregates,
                    MessageResolutionPerAccountStatus status, AccountEntity account) {

        if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.isAlertTop10SaverSWM()) {
                createAccountAlert(account, getAlertByType(EnumAlertType.TOP_10_PERCENT_OF_SAVERS), DateTime.now());
            }
        }
    }

    // 1 recommendation - Spend 1 less minute in the shower and save {integer1} {integer2}
    private void recommendLessShowerTimeAmphiro(
            MessageCalculationConfiguration config, ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {     
        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.LESS_SHOWER_TIME.getValue(), account)){         
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendShampooChangeAmphiro().getKey()) {
                    AccountDynamicRecommendationEntity recommendation = createAccountDynamicRecommendation(
                            account,
                            getDynamicRecommendationByType(EnumDynamicRecommendationType.LESS_SHOWER_TIME),
                            DateTime.now());
                    // Float euros = (float)
                    // (EUROS_PER_LITRE*recommendLessShowerTime.getValue());
                    // will use liters instead of currency here.
                    Integer liters1 = status.getRecommendShampooChangeAmphiro().getValue();
                    Integer liters2 = 2 * status.getRecommendShampooChangeAmphiro().getValue();
                    setAccountDynamicRecommendationProperty(recommendation, config.getIntKey1(), liters1.toString());
                    setAccountDynamicRecommendationProperty(recommendation, config.getIntKey2(), liters2.toString());
                }
            }
        }
    }

    // 2 recommendation - You could save {currency1} if you used a bit less hot water in the shower. {currency2}
    private void recommendLowerTemperatureAmphiro(
            MessageCalculationConfiguration config,
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.LOWER_TEMPERATURE.getValue(), account)){         
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendLowerTemperatureAmphiro().getKey()) {
                    AccountDynamicRecommendationEntity recommendation = createAccountDynamicRecommendation(account,
                                    getDynamicRecommendationByType(EnumDynamicRecommendationType.LOWER_TEMPERATURE),
                                    DateTime.now());
                    Integer annualShowerConsumption = status.getRecommendLowerTemperatureAmphiro().getValue();

                    // formula: degrees * litres * kcal * kwh * kwh price

                    // formula description:
                    // http://antoine.frostburg.edu/chem/senese/101/thermo/faq/energy-required-for-temperature-rise.shtml
                    // https://answers.yahoo.com/question/index?qid=20071209205616AADfWQ3

                    // 1 calorie will raise the temperature of 1 gram of water 1
                    // degree Celsius.
                    // 1000 calories will raise the temperature of 1 litre of water
                    // 1 degree Celsius
                    // 1 cal is 1.163E-6 kWh (1.163*10^-6)
                    // https://www.unitjuggler.com/convert-energy-from-cal-to-kWh.html
                    // kwh greek price is 0.224 euros clean
                    // http://www.adslgr.com/forum/threads/860523-%CE%A4%CE%B9%CE%BC%CE%AE-%CE
                    // %BA%CE%B9%CE%BB%CE%BF%CE%B2%CE%B1%CF%84%CF%8E%CF%81%CE%B1%CF%82-%CE%94%CE%95%CE%97-2015
                    // example:
                    // 2 degrees, total 30 showers per month for 2 people, 40 liters
                    // per shower.
                    // 2*12*30*40*1000*1.163*10^-6*0.224 euros
                    // =7.50 euros

                    Double pricePerKWH;
                    switch (account.getCountry()) {
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
                    Double eurosSavedPerYear = ((2 * annualShowerConsumption * 1000 * 1.163 * pricePerKWH) / 1000000);
                    // degrees will be a fixed number set to 2.
                    setAccountDynamicRecommendationProperty(
                            recommendation, config.getCurrencyKey1(), eurosSavedPerYear.toString());
                    setAccountDynamicRecommendationProperty(
                            recommendation, config.getCurrencyKey2(), eurosSavedPerYear.toString());
                }
            }
        }
    }

    // 3 recommendation - Reduce the water flow in the shower and gain {integer1} {integer2}
    private void recommendLowerFlowAmphiro(
            MessageCalculationConfiguration config,
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.LOWER_FLOW.getValue(), account)){          
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendLowerFlowAmphiro().getKey()) {
                    AccountDynamicRecommendationEntity recommendation = createAccountDynamicRecommendation(
                            account, getDynamicRecommendationByType(EnumDynamicRecommendationType.LOWER_FLOW), DateTime.now());
                    setAccountDynamicRecommendationProperty(
                            recommendation, config.getIntKey1(),
                            status.getRecommendLowerFlowAmphiro().getValue().toString());
                    setAccountDynamicRecommendationProperty(
                            recommendation, config.getIntKey2(),
                            status.getRecommendLowerFlowAmphiro().getValue().toString());
                }
            }
        }
    }

    // 4 recommendation - Change your shower head and save {integer1} {integer2}
    private void recommendShowerHeadChangeAmphiro(
            MessageCalculationConfiguration config, ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.CHANGE_SHOWERHEAD.getValue(), account)){           
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendShowerHeadChangeAmphiro().getKey()) {
                    AccountDynamicRecommendationEntity recommendation = createAccountDynamicRecommendation(account,
                                    getDynamicRecommendationByType(EnumDynamicRecommendationType.CHANGE_SHOWERHEAD),
                                    DateTime.now());
                    Integer annualLitresSaved = status.getRecommendShowerHeadChangeAmphiro().getValue();
                    setAccountDynamicRecommendationProperty(
                            recommendation, config.getIntKey1(), annualLitresSaved.toString());
                    setAccountDynamicRecommendationProperty(
                            recommendation, config.getIntKey2(), annualLitresSaved.toString());
                }
            }
        }
    }

    // 5 recommendation - Have you considered changing your shampoo? {integer1} percent
    private void recommendShampooAmphiro(
            MessageCalculationConfiguration config,
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account)
    {
        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.SHAMPOO_CHANGE.getValue(), account)){            
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendShampooChangeAmphiro().getKey()) {
                    AccountDynamicRecommendationEntity recommendation = createAccountDynamicRecommendation(account,
                            getDynamicRecommendationByType(EnumDynamicRecommendationType.SHAMPOO_CHANGE), DateTime.now());
                    setAccountDynamicRecommendationProperty(recommendation, config.getIntKey1(), status
                            .getRecommendShampooChangeAmphiro().getValue().toString());
                }
            }
        }
    }

    // 6 recommendation - When showering, reduce the water flow when you dont need it {integer1} {integer2}
    private void recommendReduceFlowWhenNotNeededAmphiro(
            MessageCalculationConfiguration config, 
            ConsumptionStats aggregates, MessageResolutionPerAccountStatus status, AccountEntity account) 
    {
        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED.getValue(), account)){          
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendReduceFlowWhenNotNeededAmphiro().getKey()) {
                    AccountDynamicRecommendationEntity recommendation = createAccountDynamicRecommendation(
                                    account,
                                    getDynamicRecommendationByType(EnumDynamicRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED),
                                    DateTime.now());
                    Integer moreShowerWaterThanOthers = status.getRecommendReduceFlowWhenNotNeededAmphiro().getValue();
                    setAccountDynamicRecommendationProperty(
                            recommendation, config.getIntKey1(), moreShowerWaterThanOthers.toString());
                    setAccountDynamicRecommendationProperty(
                            recommendation, config.getIntKey2(), moreShowerWaterThanOthers.toString());
                }
            }
        }
    }

    private List<Integer> getAllUtilities() {
        List<Integer> groups = new ArrayList<>();
        try {
            TypedQuery<eu.daiad.web.domain.application.UtilityEntity> query = entityManager.createQuery(
                            "select a from utility a", eu.daiad.web.domain.application.UtilityEntity.class);
            List<eu.daiad.web.domain.application.UtilityEntity> result = query.getResultList();
            for (eu.daiad.web.domain.application.UtilityEntity group : result) {
                groups.add(group.getId());
            }
            return groups;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    private List<AccountEntity> getUsersOfUtility(int utilityId) {
        List<AccountEntity> userAccounts = new ArrayList<>();
        try {
            TypedQuery<eu.daiad.web.domain.application.AccountEntity> query = entityManager.createQuery(
                            "select a from account a where a.utility.id = :id",
                            eu.daiad.web.domain.application.AccountEntity.class);
            query.setParameter("id", utilityId);

            List<eu.daiad.web.domain.application.AccountEntity> result = query.getResultList();
            for (eu.daiad.web.domain.application.AccountEntity account : result) {
                for (AccountRoleEntity accountRole : account.getRoles()) {
                    if (accountRole.getRole().getName().equals(EnumRole.ROLE_USER.toString())) {
                        userAccounts.add(getAccountByUsername(account.getUsername()));
                    }
                }
            }
            return userAccounts;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    private AccountEntity getAccountByKey(UUID key) {
        TypedQuery<eu.daiad.web.domain.application.AccountEntity> query = entityManager.createQuery(
                        "select a from account a where a.key = :key", 
                        eu.daiad.web.domain.application.AccountEntity.class)
                .setFirstResult(0).setMaxResults(1);

        query.setParameter("key", key);
        return query.getSingleResult();
    }

    private AccountEntity getAccountByUsername(String username) {
        TypedQuery<eu.daiad.web.domain.application.AccountEntity> query = entityManager.createQuery(
                        "select a from account a where a.username = :username",
                        eu.daiad.web.domain.application.AccountEntity.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("username", username);
        return query.getSingleResult();
    }

    private List<AccountAlertEntity> getAccountAlertsByUser(AccountEntity account) {
        TypedQuery<eu.daiad.web.domain.application.AccountAlertEntity> query = entityManager.createQuery(
                        "select a from account_alert a where a.account.id = :accountId",
                        eu.daiad.web.domain.application.AccountAlertEntity.class);

        query.setParameter("accountId", account.getId());
        return query.getResultList();
    }

    private boolean isAlertAlreadyProducedForUser(int alertId, AccountEntity account) {
        TypedQuery<eu.daiad.web.domain.application.AccountAlertEntity> query = entityManager.createQuery(
                        "select a from account_alert a where a.account.id = :accountId and a.alert.id = :alertId",
                        eu.daiad.web.domain.application.AccountAlertEntity.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("accountId", account.getId());
        query.setParameter("alertId", alertId);

        List<AccountAlertEntity> resultsList = query.getResultList();
        return !resultsList.isEmpty();

    }
    
    private boolean isAlertAlreadyProducedThisWeekForUser(int alertId, AccountEntity account) {
        TypedQuery<eu.daiad.web.domain.application.AccountAlertEntity> query = entityManager.createQuery(
                        "select a from account_alert a where a.account.id = :accountId and a.alert.id = :alertId and a.createdOn > :date",
                        eu.daiad.web.domain.application.AccountAlertEntity.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("accountId", account.getId());
        query.setParameter("alertId", alertId);
        query.setParameter("date", DateTime.now().minusDays(7));

        List<AccountAlertEntity> resultsList = query.getResultList();
        return !resultsList.isEmpty();

    }    
    
    private boolean isAlertAlreadyProducedThisMonthForUser(int alertId, AccountEntity account) {
        TypedQuery<eu.daiad.web.domain.application.AccountAlertEntity> query = entityManager.createQuery(
                        "select a from account_alert a where a.account.id = :accountId and a.alert.id = :alertId and a.createdOn > :date",
                        eu.daiad.web.domain.application.AccountAlertEntity.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("accountId", account.getId());
        query.setParameter("alertId", alertId);
        query.setParameter("date", DateTime.now().minusMonths(1));

        List<AccountAlertEntity> resultsList = query.getResultList();

        return !resultsList.isEmpty();

    } 
    
    private boolean isRecommendationAlreadyProducedThisMonthForUser(int recommendationId, AccountEntity account) {
        TypedQuery<eu.daiad.web.domain.application.AccountDynamicRecommendationEntity> query = entityManager.createQuery(
            "select a from account_dynamic_recommendation a where a.account.id = :accountId and a.recommendation.id = :recommendationId and a.createdOn > :date",
            eu.daiad.web.domain.application.AccountDynamicRecommendationEntity.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("accountId", account.getId());
        query.setParameter("recommendationId", recommendationId);
        query.setParameter("date", DateTime.now().minusMonths(1));

        List<AccountDynamicRecommendationEntity> resultsList = query.getResultList();
        return !resultsList.isEmpty();
    } 
    
    private boolean isAlertProducedOverThreeTimesForUser(int alertId, AccountEntity account) {
        TypedQuery<eu.daiad.web.domain.application.AccountAlertEntity> query = entityManager.createQuery(
                        "select a from account_alert a where a.account.id = :accountId and a.alert.id = :alertId",
                        eu.daiad.web.domain.application.AccountAlertEntity.class).setFirstResult(0).setMaxResults(5);
        query.setParameter("accountId", account.getId());
        query.setParameter("alertId", alertId);

        List<AccountAlertEntity> resultsList = query.getResultList();
        return resultsList.size() > 3;
    }      

    private void generateInsights(
            MessageCalculationConfiguration config,
            MessageResolutionPerAccountStatus messageStatus, AccountEntity account)
    {
        for (DynamicRecommendation.Parameters m: messageStatus.getInsights()) {
            DynamicRecommendationEntity recommendation = 
                    getDynamicRecommendationByType(m.getType());
            
            AccountDynamicRecommendationEntity accountRecommendation = 
                createAccountDynamicRecommendation(account, recommendation, DateTime.now());
            
            for (Map.Entry<String, Object> p: m.getPairs().entrySet()) {
                setAccountDynamicRecommendationProperty(
                        accountRecommendation, p.getKey(), String.valueOf(p.getValue()));
            }   
        }
    }
    
    //
    // ~ Persist methods
    //
    
    private void setAccountAlertProperty(AccountAlertEntity alert, String key, String value) {
        AccountAlertPropertyEntity accountAlertProperty = new AccountAlertPropertyEntity();

        accountAlertProperty.setAlert(alert);
        accountAlertProperty.setKey(key);
        accountAlertProperty.setValue(value);

        this.entityManager.persist(accountAlertProperty);
    }

    private void setAccountDynamicRecommendationProperty(
            AccountDynamicRecommendationEntity recommendation, String key, String value) 
    {
        AccountDynamicRecommendationPropertyEntity accountDynamicRecommendationProperty = new AccountDynamicRecommendationPropertyEntity();

        accountDynamicRecommendationProperty.setRecommendation(recommendation);
        accountDynamicRecommendationProperty.setKey(key);
        accountDynamicRecommendationProperty.setValue(value);

        this.entityManager.persist(accountDynamicRecommendationProperty);
    }

    private AccountAlertEntity createAccountAlert(AccountEntity account, AlertEntity alert, DateTime timestamp) 
    {
        AccountAlertEntity accountAlert = new AccountAlertEntity();

        accountAlert.setAccount(account);
        accountAlert.setAlert(alert);
        accountAlert.setCreatedOn(timestamp);

        this.entityManager.persist(accountAlert);
        return accountAlert;
    }

    private AccountDynamicRecommendationEntity createAccountDynamicRecommendation(
            AccountEntity account, DynamicRecommendationEntity recommendation, DateTime createdOn) 
    {
        AccountDynamicRecommendationEntity accountDynamicRecommendation = new AccountDynamicRecommendationEntity();

        accountDynamicRecommendation.setAccount(account);
        accountDynamicRecommendation.setRecommendation(recommendation);
        accountDynamicRecommendation.setCreatedOn(createdOn);

        this.entityManager.persist(accountDynamicRecommendation);
        return accountDynamicRecommendation;
    }

    private AccountStaticRecommendationEntity createAccountStaticRecommendation(
            AccountEntity account, StaticRecommendationEntity recommendation, DateTime createdOn) 
    {
        AccountStaticRecommendationEntity accountStaticRecommendation = new AccountStaticRecommendationEntity();

        accountStaticRecommendation.setAccount(account);
        accountStaticRecommendation.setRecommendation(recommendation);
        accountStaticRecommendation.setCreatedOn(createdOn);

        this.entityManager.persist(accountStaticRecommendation);
        return accountStaticRecommendation;
    }
}
