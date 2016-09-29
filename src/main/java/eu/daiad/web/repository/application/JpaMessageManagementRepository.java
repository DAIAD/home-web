package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import eu.daiad.web.domain.application.Account;
import eu.daiad.web.domain.application.AccountAlert;
import eu.daiad.web.domain.application.AccountAlertProperty;
import eu.daiad.web.domain.application.AccountDynamicRecommendation;
import eu.daiad.web.domain.application.AccountDynamicRecommendationProperty;
import eu.daiad.web.domain.application.AccountRole;
import eu.daiad.web.domain.application.AccountStaticRecommendation;
import eu.daiad.web.domain.application.Alert;
import eu.daiad.web.domain.application.DynamicRecommendation;
import eu.daiad.web.domain.application.StaticRecommendation;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.message.ConsumptionAggregateContainer;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.PendingMessageStatus;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.BaseRepository;

@Repository()
@Transactional("applicationTransactionManager")
public class JpaMessageManagementRepository extends BaseRepository implements IMessageManagementRepository {

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public void executeAccount(MessageCalculationConfiguration config, ConsumptionAggregateContainer aggregates,
                    PendingMessageStatus status, UUID accountkey) {
        Account account = getAccountByKey(accountkey);

        this.executeAccount(config, aggregates, status, account);
    }

    private void executeAccount(MessageCalculationConfiguration config, ConsumptionAggregateContainer aggregates,
                    PendingMessageStatus status, Account account) {
        computeAmphiroMessagesForUser(config, aggregates, status, account);
        computeSmartWaterMeterMessagesForUser(config, aggregates, status, account);
        computeStaticTipsForUser(config, status, account);
    }

    @Override
    public DateTime getLastDateOfAccountStaticRecommendation(AuthenticatedUser user) {
        DateTime lastCreatedOn = null;
        TypedQuery<AccountStaticRecommendation> accountStaticRecommendationQuery = entityManager
                        .createQuery("select a from account_static_recommendation a where a.account.id = :accountId order by a.createdOn desc",
                                        eu.daiad.web.domain.application.AccountStaticRecommendation.class);
        accountStaticRecommendationQuery.setParameter("accountId", user.getId());

        List<AccountStaticRecommendation> accountStaticRecommendations = accountStaticRecommendationQuery
                        .getResultList();

        if (!accountStaticRecommendations.isEmpty()) {
            lastCreatedOn = accountStaticRecommendations.get(0).getCreatedOn();
        }

        return lastCreatedOn;
    }

    private void computeAmphiroMessagesForUser(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {

        if (!status.isAmphiroInstalled()) {
            return;
        }

        // alertHotTemperatureAmphiro(account); //inactive
        // alertShowerStillOnAmphiro(account); //inactive
        alertTooMuchWaterConsumptionAmphiro(config, aggregates, status, account);
        alertTooMuchEnergyAmphiro(config, aggregates, status, account);
        // alertNearDailyBudgetAmphiro(account); //inactive
        // alertNearWeeklyBudgetAmphiro(account); //inactive
        // alertReachedDailyBudgetAmphiro(account);
        // alertShowerChampionAmphiro(account); //inactive
        alertImprovedShowerEfficiencyAmphiro(config, status, account);

        recommendLessShowerTimeAmphiro(config, aggregates, status, account);
        recommendLowerTemperatureAmphiro(config, aggregates, status, account);
        recommendLowerFlowAmphiro(config, aggregates, status, account);
        recommendShowerHeadChangeAmphiro(config, aggregates, status, account);
        recommendShampooAmphiro(config, aggregates, status, account);
        // recommendReduceFlowWhenNotNeededAmphiro(account); //inactive, mobile
        // only.

    }

    private void computeSmartWaterMeterMessagesForUser(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {
        if (!status.isMeterInstalled()) {
            return;
        }

        alertWaterLeakSWM(config, status, account);
        //alertWaterQualitySWM(config, status, account);
        // alertPromptGoodJobMonthlySWM(config, aggregates, status, account);
        // //inactive prompt
        // promptGoodJobWeeklySWM(account); using monthly for now.
        alertTooMuchWaterConsumptionSWM(config, aggregates, status, account);
        alertReducedWaterUseSWM(config, status, account);

        //alertNearDailyBudgetSWM(config, status, account);
        //alertNearWeeklyBudgetSWM(config, status, account);
        //alertReachedDailyBudgetSWM(config, status, account);
        //alertWaterChampionSWM(config, status, account);

        alertWaterEfficiencyLeaderSWM(config, aggregates, status, account);
        // alertKeepUpSavingWaterSWM(config, status, account); //inactive prompt

        // alertLitresSavedSWM(config, status, account); //inactive prompt
        // alertTop25SaverSWM(config, aggregates, status, account); //inactive
        // prompt
        // alertTop10SaverSWM(config, aggregates, status, account); //inactive
        // prompt
    }

    private Alert getAlertByType(EnumAlertType type) {
        TypedQuery<eu.daiad.web.domain.application.Alert> query = entityManager.createQuery(
                        "select a from alert a where a.id = :id", eu.daiad.web.domain.application.Alert.class)
                        .setFirstResult(0).setMaxResults(1);

        query.setParameter("id", type.getValue());

        return query.getSingleResult();
    }

    private DynamicRecommendation getDynamicRecommendationByType(EnumDynamicRecommendationType type) {
        TypedQuery<eu.daiad.web.domain.application.DynamicRecommendation> query = entityManager.createQuery(
                        "select d from dynamic_recommendation d where d.id = :id",
                        eu.daiad.web.domain.application.DynamicRecommendation.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("id", type.getValue());

        return query.getSingleResult();
    }

    private StaticRecommendation getRandomStaticRecommendationForLocale(String accountLocale) {
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

        TypedQuery<eu.daiad.web.domain.application.StaticRecommendation> accountAlertsQuery = entityManager
                        .createQuery("select a from static_recommendation a where a.locale = :locale and a.active = :active",
                                        eu.daiad.web.domain.application.StaticRecommendation.class);
        accountAlertsQuery.setParameter("locale", locale);
        accountAlertsQuery.setParameter("active", true);
        List<StaticRecommendation> staticRecommendations = accountAlertsQuery.getResultList();

        if (staticRecommendations.isEmpty()) {
            return null;
        }

        Collections.shuffle(staticRecommendations);

        StaticRecommendation singleRandomStaticRecommendation = staticRecommendations.get(0);

        return singleRandomStaticRecommendation;
    }

    private List<StaticRecommendation> getInitialRandomStaticRecommendationForLocale(String accountLocale) {
        List<StaticRecommendation> initialTips = new ArrayList<>();
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

        TypedQuery<eu.daiad.web.domain.application.StaticRecommendation> accountAlertsQuery = entityManager
                        .createQuery("select a from static_recommendation a where a.locale = :locale and a.active = :active",
                                        eu.daiad.web.domain.application.StaticRecommendation.class);
        accountAlertsQuery.setParameter("locale", locale);
        accountAlertsQuery.setParameter("active", true);
        List<StaticRecommendation> staticRecommendations = accountAlertsQuery.getResultList();

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
    private void computeStaticTipsForUser(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        
        if (status.isInitialStaticTips()) {
            List<StaticRecommendation> randomTips = getInitialRandomStaticRecommendationForLocale(account.getLocale());
            for (StaticRecommendation randomTip : randomTips) {
                createAccountStaticRecommendation(account, randomTip, DateTime.now());
            }
        } else if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.isStaticTipToBeProduced()) {
                StaticRecommendation randomTip = getRandomStaticRecommendationForLocale(account.getLocale());
                if (randomTip != null) {
                    createAccountStaticRecommendation(account, randomTip, DateTime.now());
                }
            }
        }
    }

    // 1 alert - Check for water leaks!
    private void alertWaterLeakSWM(MessageCalculationConfiguration config, PendingMessageStatus status, Account account) {

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
    private void alertShowerStillOnAmphiro(ConsumptionAggregateContainer aggregates, PendingMessageStatus status,
                    Account account) {
        if (status.isAlertShowerStillOnAmphiro()) {
            createAccountAlert(account, getAlertByType(EnumAlertType.SHOWER_ON), DateTime.now());
        }
    }

    // 3 alert - water fixtures ignored
    // 4 alert - unusual activity, no consumption patterns available yet,
    // ignored

    // TODO : This alert is repeatedly appearing every time the job is executed
    // and the user has not used any water e.g. if the job is executed once per
    // hour, a new alert is generated every hour ...

    // 5 alert - Water quality not assured!
    private void alertWaterQualitySWM(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        if (status.isAlertWaterQualitySWM()) {
            createAccountAlert(account, getAlertByType(EnumAlertType.WATER_QUALITY), DateTime.now());
        }
    }

    // 6 alert - Water too hot!
    private void alertHotTemperatureAmphiro(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {
        if (status.isAlertHotTemperatureAmphiro()) {
            createAccountAlert(account, getAlertByType(EnumAlertType.HOT_TEMPERATURE), DateTime.now());
        }
    }

    // 7 alert - Reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearDailyBudgetSWM(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        if (status.getAlertNearDailyBudgetSWM() != null) {
            AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.NEAR_DAILY_WATER_BUDGET),
                            DateTime.now());

            setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertNearDailyBudgetSWM().getKey().toString());
            setAccountAlertProperty(alert, config.getIntKey2(), status.getAlertNearDailyBudgetSWM().getValue()
                            .toString());
        }
    }

    // 8 alert - Reached 80% of your daily water budget {integer1} {integer2}
    private void alertNearWeeklyBudgetSWM(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        if (status.getAlertNearWeeklyBudgetSWM() != null) {
            AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.NEAR_WEEKLY_WATER_BUDGET),
                            DateTime.now());

            setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertNearWeeklyBudgetSWM().getKey()
                            .toString());
            setAccountAlertProperty(alert, config.getIntKey2(), status.getAlertNearWeeklyBudgetSWM().getValue()
                            .toString());
        }
    }

    // 9 alert - Reached 80% of your daily shower budget {integer1} {integer2}
    private void alertNearDailyBudgetAmphiro(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        if (status.getAlertNearDailyBudgetAmphiro() != null) {
            AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.NEAR_DAILY_SHOWER_BUDGET),
                            DateTime.now());

            setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertNearDailyBudgetAmphiro().getKey()
                            .toString());
            setAccountAlertProperty(alert, config.getIntKey2(), status.getAlertNearDailyBudgetAmphiro().getValue()
                            .toString());
        }
    }

    // 10 alert - Reached 80% of your weekly shower budget {integer1} {integer2}
    private void alertNearWeeklyBudgetAmphiro(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        if (DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.getAlertNearWeeklyBudgetAmphiro() != null) {
                AccountAlert alert = createAccountAlert(account,
                                getAlertByType(EnumAlertType.NEAR_WEEKLY_SHOWER_BUDGET), DateTime.now());

                setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertNearWeeklyBudgetAmphiro().getKey()
                                .toString());
                setAccountAlertProperty(alert, config.getIntKey2(), status.getAlertNearWeeklyBudgetAmphiro().getValue()
                                .toString());
            }
        }
    }

    // 11 alert - Reached daily Water Budget {integer1}
    private void alertReachedDailyBudgetSWM(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        if (status.getAlertReachedDailyBudgetSWM() != null) {
            AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.REACHED_DAILY_WATER_BUDGET),
                            DateTime.now());

            setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertReachedDailyBudgetSWM().getValue()
                            .toString());
        }
    }

    // 12 alert - Reached daily Shower Budget {integer1}
    private void alertReachedDailyBudgetAmphiro(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        if (status.getAlertReachedDailyBudgetSWM() != null) {
            if (status.getAlertReachedDailyBudgetSWM().getKey()) {
                AccountAlert alert = createAccountAlert(account,
                                getAlertByType(EnumAlertType.REACHED_DAILY_SHOWER_BUDGET), DateTime.now());

                setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertReachedDailyBudgetSWM().getValue()
                                .toString());
            }
        }

    }

    // 13 alert - You are a real water champion!
    private void alertWaterChampionSWM(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        
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
    private void alertShowerChampionAmphiro(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        
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
    private void alertTooMuchWaterConsumptionSWM(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {
        
        if(isAlertAlreadyProducedThisWeekForUser(EnumAlertType.TOO_MUCH_WATER_SWM.getValue(), account)){
            if(config.isOnDemandExecution()){
                if (status.getAlertTooMuchWaterConsumptionSWM().getKey()) {
                    AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.TOO_MUCH_WATER_SWM),
                                    DateTime.now());

                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertTooMuchWaterConsumptionSWM()
                                    .getValue().toString());
                }
                return;
            }        

            if (DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
                if (status.getAlertTooMuchWaterConsumptionSWM().getKey()) {
                    AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.TOO_MUCH_WATER_SWM),
                                    DateTime.now());

                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertTooMuchWaterConsumptionSWM()
                                    .getValue().toString());
                }
            }            
        }
    }

    // 16 alert - You are using too much water in the shower {integer1}
    private void alertTooMuchWaterConsumptionAmphiro(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {

        if(isAlertAlreadyProducedThisWeekForUser(EnumAlertType.TOO_MUCH_WATER_AMPHIRO.getValue(), account)){
            if(config.isOnDemandExecution()){
                if (status.getAlertTooMuchWaterConsumptionAmphiro().getKey()) {
                    AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.TOO_MUCH_WATER_AMPHIRO),
                                    DateTime.now());

                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertTooMuchWaterConsumptionAmphiro()
                                    .getValue().toString());
                }
                return;
            }

            if (DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
                if (status.getAlertTooMuchWaterConsumptionAmphiro().getKey()) {
                    AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.TOO_MUCH_WATER_AMPHIRO),
                                    DateTime.now());

                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertTooMuchWaterConsumptionAmphiro()
                                    .getValue().toString());
                }
            }            
        }        
    }

    // 17 alert - You are spending too much energy for showering {integer1}
    // {currency}
    private void alertTooMuchEnergyAmphiro(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {
        
        if(isAlertAlreadyProducedThisWeekForUser(EnumAlertType.TOO_MUCH_ENERGY.getValue(), account)){          
            if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
                if (status.getAlertTooMuchEnergyAmphiro().getKey()) {
                    AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.TOO_MUCH_ENERGY),
                                    DateTime.now());

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

    // 18 alert - Well done! You have greatly reduced your water use {integer1}
    // percent
    private void alertReducedWaterUseSWM(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        
        if (!isAlertAlreadyProducedForUser(EnumAlertType.REDUCED_WATER_USE.getValue(), account)) {
            if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
                if (status.getAlertReducedWaterUseSWM().getKey()) {
                    AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.REDUCED_WATER_USE),
                                    DateTime.now());

                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertReducedWaterUseSWM().getValue()
                                    .toString());
                }
            }
        }
    }

    // 19 alert - Well done! You have greatly improved your shower efficiency
    // {integer1} percent
    private void alertImprovedShowerEfficiencyAmphiro(MessageCalculationConfiguration config,
                    PendingMessageStatus status, Account account) {
        
        if (!isAlertAlreadyProducedForUser(EnumAlertType.IMPROVED_SHOWER_EFFICIENCY.getValue(), account)) {
            if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
                if (status.getAlertImprovedShowerEfficiencyAmphiro().getKey()) {
                    AccountAlert alert = createAccountAlert(account,
                                    getAlertByType(EnumAlertType.IMPROVED_SHOWER_EFFICIENCY), DateTime.now());

                    setAccountAlertProperty(alert, config.getIntKey1(), status
                                    .getAlertImprovedShowerEfficiencyAmphiro().getValue().toString());
                }
            }
        }
    }

    // 20 alert - Congratulations! You are a water efficiency leader {integer1}
    // litres
    private void alertWaterEfficiencyLeaderSWM(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {

        if(isAlertAlreadyProducedThisMonthForUser(EnumAlertType.WATER_EFFICIENCY_LEADER.getValue(), account)){          
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getAlertWaterEfficiencyLeaderSWM().getKey()) {
                    AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.WATER_EFFICIENCY_LEADER),
                                    DateTime.now());

                    setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertWaterEfficiencyLeaderSWM()
                                    .getValue().toString());
                }
            }
        }    
    }

    // 21 alert - Keep up saving water!
    private void alertKeepUpSavingWaterSWM(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        // compute only if the message list is empty
        if (!isAlertAlreadyProducedForUser(EnumAlertType.KEEP_UP_SAVING_WATER.getValue(), account)) {
            createAccountAlert(account, getAlertByType(EnumAlertType.KEEP_UP_SAVING_WATER), DateTime.now());
        }
    }

    // 22 alert - You are doing a great job!
    private void alertPromptGoodJobMonthlySWM(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {
        if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
            if (status.isAlertPromptGoodJobMonthlySWM()) {
                createAccountAlert(account, getAlertByType(EnumAlertType.GOOD_JOB_MONTHLY), DateTime.now());
            }
        }
    }

    // 23 alert - You have already saved {integer1} litres of water!
    private void alertLitresSavedSWM(MessageCalculationConfiguration config, PendingMessageStatus status,
                    Account account) {
        
        if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.getAlertLitresSavedSWM().getKey()) {
                AccountAlert alert = createAccountAlert(account, getAlertByType(EnumAlertType.LITERS_ALREADY_SAVED),
                                DateTime.now());

                setAccountAlertProperty(alert, config.getIntKey1(), status.getAlertLitresSavedSWM().getValue()
                                .toString());
            }
        }
    }

    // 24 alert - Congratulations! You are one of the top 25% savers in your
    // region.
    private void alertTop25SaverSWM(MessageCalculationConfiguration config, ConsumptionAggregateContainer aggregates,
                    PendingMessageStatus status, Account account) {     
        
        if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.isAlertTop25SaverWeeklySWM()) {
                createAccountAlert(account, getAlertByType(EnumAlertType.TOP_25_PERCENT_OF_SAVERS), DateTime.now());
            }
        }
    }

    // 25 alert - Congratulations! You are among the top group of savers in your
    // city.
    private void alertTop10SaverSWM(MessageCalculationConfiguration config, ConsumptionAggregateContainer aggregates,
                    PendingMessageStatus status, Account account) {

        if (config.isOnDemandExecution() || DateTime.now().getDayOfWeek() == config.getComputeThisDayOfWeek()) {
            if (status.isAlertTop10SaverSWM()) {
                createAccountAlert(account, getAlertByType(EnumAlertType.TOP_10_PERCENT_OF_SAVERS), DateTime.now());
            }
        }
    }

    // 1 recommendation - Spend 1 less minute in the shower and save {integer1}
    // {integer2}
    private void recommendLessShowerTimeAmphiro(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {     
        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.LESS_SHOWER_TIME.getValue(), account)){         
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendShampooChangeAmphiro().getKey()) {
                    AccountDynamicRecommendation recommendation = createAccountDynamicRecommendation(account,
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

    // 2 recommendation - You could save {currency1} if you used a bit less hot
    // water in the shower. {currency2}
    private void recommendLowerTemperatureAmphiro(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {

        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.LOWER_TEMPERATURE.getValue(), account)){         
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendLowerTemperatureAmphiro().getKey()) {
                    AccountDynamicRecommendation recommendation = createAccountDynamicRecommendation(account,
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
                    setAccountDynamicRecommendationProperty(recommendation, config.getCurrencyKey1(), eurosSavedPerYear
                                    .toString());
                    setAccountDynamicRecommendationProperty(recommendation, config.getCurrencyKey2(), eurosSavedPerYear
                                    .toString());
                }
            }
        }
    }

    // 3 recommendation - Reduce the water flow in the shower and gain
    // {integer1} {integer2}
    private void recommendLowerFlowAmphiro(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {

        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.LOWER_FLOW.getValue(), account)){          
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendLowerFlowAmphiro().getKey()) {
                    AccountDynamicRecommendation recommendation = createAccountDynamicRecommendation(account,
                                    getDynamicRecommendationByType(EnumDynamicRecommendationType.LOWER_FLOW), DateTime
                                                    .now());

                    setAccountDynamicRecommendationProperty(recommendation, config.getIntKey1(), status
                                    .getRecommendLowerFlowAmphiro().getValue().toString());
                    setAccountDynamicRecommendationProperty(recommendation, config.getIntKey2(), status
                                    .getRecommendLowerFlowAmphiro().getValue().toString());
                }
            }
        }
    }

    // 4 recommendation - Change your shower head and save {integer1} {integer2}
    private void recommendShowerHeadChangeAmphiro(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {

        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.CHANGE_SHOWERHEAD.getValue(), account)){           
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendShowerHeadChangeAmphiro().getKey()) {
                    AccountDynamicRecommendation recommendation = createAccountDynamicRecommendation(account,
                                    getDynamicRecommendationByType(EnumDynamicRecommendationType.CHANGE_SHOWERHEAD),
                                    DateTime.now());

                    Integer annualLitresSaved = status.getRecommendShowerHeadChangeAmphiro().getValue();

                    setAccountDynamicRecommendationProperty(recommendation, config.getIntKey1(), annualLitresSaved
                                    .toString());
                    setAccountDynamicRecommendationProperty(recommendation, config.getIntKey2(), annualLitresSaved
                                    .toString());
                }
            }
        }
    }

    // 5 recommendation - Have you considered changing your shampoo? {integer1}
    // percent
    private void recommendShampooAmphiro(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {

        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.SHAMPOO_CHANGE.getValue(), account)){            
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendShampooChangeAmphiro().getKey()) {
                    AccountDynamicRecommendation recommendation = createAccountDynamicRecommendation(account,
                                    getDynamicRecommendationByType(EnumDynamicRecommendationType.SHAMPOO_CHANGE), DateTime
                                                    .now());

                    setAccountDynamicRecommendationProperty(recommendation, config.getIntKey1(), status
                                    .getRecommendShampooChangeAmphiro().getValue().toString());
                }
            }
        }
    }

    // 6 recommendation - When showering, reduce the water flow when you do not
    // need it {integer1} {integer2}
    private void recommendReduceFlowWhenNotNeededAmphiro(MessageCalculationConfiguration config,
                    ConsumptionAggregateContainer aggregates, PendingMessageStatus status, Account account) {

        if(isRecommendationAlreadyProducedThisMonthForUser(EnumDynamicRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED.getValue(), account)){          
            if (config.isOnDemandExecution() || DateTime.now().getDayOfMonth() == config.getComputeThisDayOfMonth()) {
                if (status.getRecommendReduceFlowWhenNotNeededAmphiro().getKey()) {
                    AccountDynamicRecommendation recommendation = createAccountDynamicRecommendation(
                                    account,
                                    getDynamicRecommendationByType(EnumDynamicRecommendationType.REDUCE_FLOW_WHEN_NOT_NEEDED),
                                    DateTime.now());

                    Integer moreShowerWaterThanOthers = status.getRecommendReduceFlowWhenNotNeededAmphiro().getValue();

                    setAccountDynamicRecommendationProperty(recommendation, config.getIntKey1(), moreShowerWaterThanOthers
                                    .toString());
                    setAccountDynamicRecommendationProperty(recommendation, config.getIntKey2(), moreShowerWaterThanOthers
                                    .toString());
                }
            }
        }
    }

    private List<Integer> getAllUtilities() {
        List<Integer> groups = new ArrayList<>();
        try {
            TypedQuery<eu.daiad.web.domain.application.Utility> query = entityManager.createQuery(
                            "select a from utility a", eu.daiad.web.domain.application.Utility.class);
            List<eu.daiad.web.domain.application.Utility> result = query.getResultList();
            for (eu.daiad.web.domain.application.Utility group : result) {
                groups.add(group.getId());
            }
            return groups;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    private List<Account> getUsersOfUtility(int utilityId) {
        List<Account> userAccounts = new ArrayList<>();
        try {
            TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager.createQuery(
                            "select a from account a where a.utility.id = :id",
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
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    private Account getAccountByKey(UUID key) {
        TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager.createQuery(
                        "select a from account a where a.key = :key", eu.daiad.web.domain.application.Account.class)
                        .setFirstResult(0).setMaxResults(1);

        query.setParameter("key", key);

        return query.getSingleResult();
    }

    private Account getAccountByUsername(String username) {
        TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager.createQuery(
                        "select a from account a where a.username = :username",
                        eu.daiad.web.domain.application.Account.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("username", username);

        return query.getSingleResult();
    }

    private List<AccountAlert> getAccountAlertsByUser(Account account) {
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> query = entityManager.createQuery(
                        "select a from account_alert a where a.account.id = :accountId",
                        eu.daiad.web.domain.application.AccountAlert.class);

        query.setParameter("accountId", account.getId());

        return query.getResultList();
    }

    private boolean isAlertAlreadyProducedForUser(int alertId, Account account) {
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> query = entityManager.createQuery(
                        "select a from account_alert a where a.account.id = :accountId and a.alert.id = :alertId",
                        eu.daiad.web.domain.application.AccountAlert.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("accountId", account.getId());
        query.setParameter("alertId", alertId);

        List<AccountAlert> resultsList = query.getResultList();

        return !resultsList.isEmpty();

    }
    
    private boolean isAlertAlreadyProducedThisWeekForUser(int alertId, Account account) {
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> query = entityManager.createQuery(
                        "select a from account_alert a where a.account.id = :accountId and a.alert.id = :alertId and a.createdOn > :date",
                        eu.daiad.web.domain.application.AccountAlert.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("accountId", account.getId());
        query.setParameter("alertId", alertId);
        query.setParameter("date", DateTime.now().minusDays(7));

        List<AccountAlert> resultsList = query.getResultList();

        return !resultsList.isEmpty();

    }    
    
    private boolean isAlertAlreadyProducedThisMonthForUser(int alertId, Account account) {
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> query = entityManager.createQuery(
                        "select a from account_alert a where a.account.id = :accountId and a.alert.id = :alertId and a.createdOn > :date",
                        eu.daiad.web.domain.application.AccountAlert.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("accountId", account.getId());
        query.setParameter("alertId", alertId);
        query.setParameter("date", DateTime.now().minusMonths(1));

        List<AccountAlert> resultsList = query.getResultList();

        return !resultsList.isEmpty();

    } 
    
    private boolean isRecommendationAlreadyProducedThisMonthForUser(int recommendationId, Account account) {
        TypedQuery<eu.daiad.web.domain.application.AccountDynamicRecommendation> query = entityManager.createQuery(
            "select a from account_dynamic_recommendation a where a.account.id = :accountId and a.recommendation.id = :recommendationId and a.createdOn > :date",
            eu.daiad.web.domain.application.AccountDynamicRecommendation.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("accountId", account.getId());
        query.setParameter("recommendationId", recommendationId);
        query.setParameter("date", DateTime.now().minusMonths(1));

        List<AccountDynamicRecommendation> resultsList = query.getResultList();

        return !resultsList.isEmpty();

    } 
    
    private boolean isAlertProducedOverThreeTimesForUser(int alertId, Account account) {
        TypedQuery<eu.daiad.web.domain.application.AccountAlert> query = entityManager.createQuery(
                        "select a from account_alert a where a.account.id = :accountId and a.alert.id = :alertId",
                        eu.daiad.web.domain.application.AccountAlert.class).setFirstResult(0).setMaxResults(5);

        query.setParameter("accountId", account.getId());
        query.setParameter("alertId", alertId);

        List<AccountAlert> resultsList = query.getResultList();

        return resultsList.size() > 3;

    }      

    private void setAccountAlertProperty(AccountAlert alert, String key, String value) {
        AccountAlertProperty accountAlertProperty = new AccountAlertProperty();

        accountAlertProperty.setAlert(alert);
        accountAlertProperty.setKey(key);
        accountAlertProperty.setValue(value);

        this.entityManager.persist(accountAlertProperty);
    }

    private void setAccountDynamicRecommendationProperty(AccountDynamicRecommendation recommendation, String key,
                    String value) {
        AccountDynamicRecommendationProperty accountDynamicRecommendationProperty = new AccountDynamicRecommendationProperty();

        accountDynamicRecommendationProperty.setRecommendation(recommendation);
        accountDynamicRecommendationProperty.setKey(key);
        accountDynamicRecommendationProperty.setValue(value);

        this.entityManager.persist(accountDynamicRecommendationProperty);
    }

    private AccountAlert createAccountAlert(Account account, Alert alert, DateTime timestamp) {
        AccountAlert accountAlert = new AccountAlert();

        accountAlert.setAccount(account);
        accountAlert.setAlert(alert);
        accountAlert.setCreatedOn(timestamp);

        this.entityManager.persist(accountAlert);

        return accountAlert;
    }

    private AccountDynamicRecommendation createAccountDynamicRecommendation(Account account,
                    DynamicRecommendation recommendation, DateTime createdOn) {
        AccountDynamicRecommendation accountDynamicRecommendation = new AccountDynamicRecommendation();

        accountDynamicRecommendation.setAccount(account);
        accountDynamicRecommendation.setRecommendation(recommendation);
        accountDynamicRecommendation.setCreatedOn(createdOn);

        this.entityManager.persist(accountDynamicRecommendation);

        return accountDynamicRecommendation;
    }

    private AccountStaticRecommendation createAccountStaticRecommendation(Account account,
                    StaticRecommendation recommendation, DateTime createdOn) {

        AccountStaticRecommendation accountStaticRecommendation = new AccountStaticRecommendation();

        accountStaticRecommendation.setAccount(account);
        accountStaticRecommendation.setRecommendation(recommendation);
        accountStaticRecommendation.setCreatedOn(createdOn);

        this.entityManager.persist(accountStaticRecommendation);

        return accountStaticRecommendation;
    }

    private float convertCurrencyIfNeed(float euros, Locale currencySymbol) {
        // this is dummy method for future use. Currently returns only euros.
        // The currency is converted in the message computation for now and only
        // for KWH prices
        if (currencySymbol.equals(Locale.GERMANY)) {
            return euros;
        } else if (currencySymbol.equals(Locale.UK)) {
            return euros;
            // return (float) (euros*0.8); //get currency rate from db
        } else {
            return euros;
        }
    }
}
