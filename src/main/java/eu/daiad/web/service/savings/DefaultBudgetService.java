package eu.daiad.web.service.savings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.domain.application.BudgetEntity;
import eu.daiad.web.domain.application.BudgetSnapshotEntity;
import eu.daiad.web.domain.application.ClusterEntity;
import eu.daiad.web.domain.application.mappings.BudgetSegmentEntity;
import eu.daiad.web.job.builder.BudgetProcessingJobBuilder;
import eu.daiad.web.job.task.BudgetProcessingTask;
import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.error.SavingsPotentialErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryBuilder;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MeterDataPoint;
import eu.daiad.web.model.query.savings.Budget;
import eu.daiad.web.model.query.savings.BudgetExploreClusterResult;
import eu.daiad.web.model.query.savings.BudgetExploreConsumerResult;
import eu.daiad.web.model.query.savings.BudgetParameters;
import eu.daiad.web.model.query.savings.BudgetQuery;
import eu.daiad.web.model.query.savings.BudgetQueryResult;
import eu.daiad.web.model.query.savings.BudgetSegment;
import eu.daiad.web.model.query.savings.BudgetSnapshot;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IBudgetRepository;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.ISavingsPotentialRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.BaseService;
import eu.daiad.web.service.IDataService;
import eu.daiad.web.service.scheduling.Constants;
import eu.daiad.web.service.scheduling.ISchedulerService;

/**
 * Provides methods for computing and querying budgets.
 */
@Service
public class DefaultBudgetService extends BaseService implements IBudgetService {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DefaultBudgetService.class);

    /**
     * Service for querying, scheduling and launching jobs.
     */
    @Autowired
    private ISchedulerService schedulerService;

    /**
     * Repository for accessing budget data.
     */
    @Autowired
    private IBudgetRepository budgetRepository;

    /**
     * Repository for accessing savings scenario data.
     */
    @Autowired
    private ISavingsPotentialRepository savingsPotentialRepository;

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Repository for accessing group members.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Service for querying consumption data.
     */
    @Autowired
    private IDataService dataService;

    /**
     * A builder used to create {@link ObjectMapper} instances for serializing scenario parameters.
     */
    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    /**
     * Object mapper for serializing scenario parameters.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Creates a new budget.
     *
     * @param user the owner.
     * @param name a user-friendly name for the budget.
     * @param parameters parameters for selecting user data.
     * @return the new budget key.
     */
    @Override
    public UUID createBudget(AuthenticatedUser user, String name, BudgetParameters parameters) {
        try {
            return budgetRepository.createBudget(user.getId(), name, parameters);
        } catch(Exception ex) {
            throw wrapApplicationException(ex, SavingsPotentialErrorCode.CREATION_FAILED);
        }
    }

    /**
     * Schedules the execution of a job for computing a snapshot for a budget for a selected year and month.
     *
     * @param key the budget key.
     * @param year the reference year.
     * @param month the reference month.
     */
    @Override
    public void scheduleSnapshotCreation(UUID key, int year, int month) {
        Map<String, String> jobParameters = new HashMap<String, String>();

        // Set budget key
        String parameterKey = BudgetProcessingJobBuilder.STEP_BUDGET_PROCESSING +
                              Constants.PARAMETER_NAME_DELIMITER +
                              BudgetProcessingTask.EnumInParameter.BUDGET_KEY.getValue();
        String parameterValue = key.toString();
        jobParameters.put(parameterKey, parameterValue);

        // Set reference date
        parameterKey = BudgetProcessingJobBuilder.STEP_BUDGET_PROCESSING +
                       Constants.PARAMETER_NAME_DELIMITER +
                       BudgetProcessingTask.EnumInParameter.REFERENCE_DATE.getValue();
        parameterValue = String.format("%d%02d01", year, month);
        jobParameters.put(parameterKey, parameterValue);

        schedulerService.launch(BudgetProcessingJobBuilder.JOB_NAME, jobParameters);
    }

    /**
     * Creates a snapshot for an active budget for a selected year and month.
     *
     * @param jobId the id of the job that created the snapshot.
     * @param key the budget key.
     * @param year the reference year.
     * @param month the reference month.
     */
    @Override
    public void createSnapshot(long jobId, UUID key, int year, int month) {
        Long snapshotId = null;

        try {
            // Skip processing for dates after the last month
            DateTime now = DateTime.now();
            if ((now.getYear() * 100 + now.getMonthOfYear()) <= (year * 100 + month)) {
                return;
            }

            // Load budget and utility data
            Budget budget = find(key);
            UtilityInfo utility = utilityRepository.getUtilityByKey(budget.getUtilityKey());
            DateTimeZone timezone = DateTimeZone.forID(utility.getTimezone());

            // Initialize budget if not already
            if (!budget.isInitialized()) {
                budgetRepository.initializeBudgetConsumers(key);
            }
            // Reset snapshot
            budgetRepository.deleteSnapshot(key, year, month);
            snapshotId = budgetRepository.createSnapshot(key, jobId, year, month);

            // Compute interval dates
            DateTime currentPeriodStart = new DateTime(year, month, 1, 0, 0, timezone)
                                                .secondOfMinute().setCopy(0)
                                                .millisOfSecond().setCopy(0);
            DateTime currentPeriodEnd = currentPeriodStart.dayOfMonth().withMaximumValue()
                                                          .hourOfDay().setCopy(23)
                                                          .minuteOfHour().setCopy(59)
                                                          .secondOfMinute().setCopy(59)
                                                          .millisOfSecond().setCopy(999);

            DateTime lastPeriodStart = currentPeriodStart.minusYears(1);
            DateTime lastPeriodEnd = lastPeriodStart.dayOfMonth().withMaximumValue()
                                                    .hourOfDay().setCopy(23)
                                                    .minuteOfHour().setCopy(59)
                                                    .secondOfMinute().setCopy(59)
                                                    .millisOfSecond().setCopy(999);

            double consumptionTotalBefore = 0D;
            double consumptionTotalAfter = 0D;

            for (UUID userKey : budgetRepository.getBudgetMembers(key)) {
                double consumptionBefore = getConusmption(userKey, timezone, lastPeriodStart, lastPeriodEnd);
                double consumptionAfter = getConusmption(userKey, timezone, currentPeriodStart, currentPeriodEnd);

                consumptionTotalBefore += consumptionBefore;
                consumptionTotalAfter += consumptionAfter;

                budgetRepository.updateConsumer(key, snapshotId, userKey, consumptionBefore, consumptionAfter, DateTime.now());

            }
            budgetRepository.updateSnapshot(key, snapshotId, consumptionTotalBefore, consumptionTotalAfter, DateTime.now());
        } catch (Exception ex) {
            logger.error("Snapshot creation has failed.", ex);
            if (snapshotId != null) {
                budgetRepository.updateSnapshot(key, snapshotId, DateTime.now());
            }
        }
    }

    private double getConusmption(UUID userKey, DateTimeZone timezone, DateTime from, DateTime to) {
        DataQuery monthlyConsumptionQuery = DataQueryBuilder.create()
                        .timezone(timezone)
                        .absolute(from, to, EnumTimeAggregation.MONTH)
                        .user("user", userKey)
                        .sum()
                        .meter()
                        .userAggregates()
                        .build();

        DataQueryResponse result = dataService.execute(monthlyConsumptionQuery);

        if (result.getSuccess()) {
            if (result.getMeters().isEmpty()) {
                return 0D;
            }

            GroupDataSeries series = result.getMeters().get(0);
            if ((!series.getPoints().isEmpty()) && (series.getPoints().size() != 1)) {
                throw new RuntimeException("Expected a single value for the series.");
            }
            if(series.getPoints().isEmpty()) {
                return 0D;
            }
            return ((MeterDataPoint) series.getPoints().get(0)).getVolume().get(EnumMetric.SUM);
        } else {
            throw createApplicationException(SharedErrorCode.UNKNOWN);
        }
    }

    /**
     * Finds the budget given its key.
     *
     * @param key the budget key.
     * @return the budget.
     */
    @Override
    public Budget find(UUID key) {
        BudgetEntity entity = budgetRepository.getBudgetByKey(key);

        try {
            return new Budget(entity, objectMapper);
        } catch (IOException e) {
            throw createApplicationException(SavingsPotentialErrorCode.PARSE_ERROR);
        }
    }

    /**
     * Gets all active budgets.
     *
     * @return a list of {@link Budget}.
     */
    @Override
    public List<Budget> findActive() {
        List<Budget> result = new ArrayList<Budget>();

        try {
            for (BudgetEntity entity : budgetRepository.getActiveBudgets()) {
                result.add(new Budget(entity, objectMapper));
            }
        } catch (IOException e) {
            throw createApplicationException(SavingsPotentialErrorCode.PARSE_ERROR);
        }

        return result;
    }

    /**
     * Filters budgets using a query.
     *
     * @param query the query.
     * @return a collection of {@link Budget}.
     */
    @Override
    public BudgetQueryResult find(BudgetQuery query) {
        if (query != null) {
            query.setOwnerId(getAuthenticatedUser().getId());

            return budgetRepository.query(query);
        }
        return new BudgetQueryResult(0, 10);
    }

    /**
     * Gets all pending budget snapshots.
     *
     * @return a list of {@link BudgetSnapshot}.
     */
    @Override
    public List<BudgetSnapshot> findPendingSnapshots() {
        List<BudgetSnapshot> snapshots = new ArrayList<BudgetSnapshot>();

        for (BudgetSnapshotEntity entity : budgetRepository.findPendingSnapshots()) {
            snapshots.add(new BudgetSnapshot(entity));
        }

        return snapshots;
    }


    /**
     * Deletes an existing budget.
     *
     * @param key the unique budget key.
     */
    @Override
    public void delete(UUID key) {
        budgetRepository.deleteBudgetByKey(key);
    }

    /**
     * Sets active state for a budget.
     *
     * @param key the budget key.
     * @param active the new active state value.
     */
    @Override
    public void setActive(UUID key, boolean active) {
        budgetRepository.setActive(key, active);
    }

    /**
     * Gets consumption data based on an active budget and cluster.
     *
     * @param budgetKey the budget key.
     * @param clusterKey the cluster key.
     * @return the cluster segments with the budget population consumption.
     */
    @Override
    public BudgetExploreClusterResult exploreCluster(UUID budgetKey, UUID clusterKey) {
        AuthenticatedUser user = getAuthenticatedUser();

        BudgetEntity budget = budgetRepository.getBudgetByKey(budgetKey);

        if ((user != null) && (user.getId() != budget.getOwner().getId())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        ClusterEntity cluster = groupRepository.getClusterByKey(clusterKey);

        List<BudgetSegmentEntity> segments = budgetRepository.exploreCluster(budgetKey, clusterKey);

        BudgetExploreClusterResult result = new BudgetExploreClusterResult();
        result.setClusterKey(cluster.getKey());
        result.setClusterName(cluster.getName());
        result.setBudgetKey(budget.getKey());
        result.setBudgetName(budget.getName());

        for (BudgetSegmentEntity s : segments) {
            result.getSegments().add(new BudgetSegment(s));
        }

        return result;
    }

    /**
     * Gets consumption data based on an active budget and user.
     *
     * @param budgetKey the budget key.
     * @param userKey the user key.
     * @return the user consumption data.
     */
    @Override
    public BudgetExploreConsumerResult exploreConsumer(UUID budgetKey, UUID userKey) {
        return budgetRepository.exploreConsumer(budgetKey, userKey);
    }

    private AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if ((authentication != null) && (authentication.getPrincipal() instanceof AuthenticatedUser)) {
            return (AuthenticatedUser) authentication.getPrincipal();
        }
        return null;
    }

}
