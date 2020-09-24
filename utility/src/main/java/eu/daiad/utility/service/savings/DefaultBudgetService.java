package eu.daiad.utility.service.savings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.common.domain.application.BudgetEntity;
import eu.daiad.common.domain.application.BudgetSnapshotEntity;
import eu.daiad.common.domain.application.ClusterEntity;
import eu.daiad.common.domain.application.mappings.BudgetSegmentEntity;
import eu.daiad.common.model.EnumTimeAggregation;
import eu.daiad.common.model.error.SavingsPotentialErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.query.DataQuery;
import eu.daiad.common.model.query.DataQueryBuilder;
import eu.daiad.common.model.query.DataQueryResponse;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.GroupDataSeries;
import eu.daiad.common.model.query.MeterDataPoint;
import eu.daiad.common.model.query.savings.Budget;
import eu.daiad.common.model.query.savings.BudgetExploreClusterResult;
import eu.daiad.common.model.query.savings.BudgetExploreConsumerResult;
import eu.daiad.common.model.query.savings.BudgetParameters;
import eu.daiad.common.model.query.savings.BudgetQuery;
import eu.daiad.common.model.query.savings.BudgetQueryResult;
import eu.daiad.common.model.query.savings.BudgetSegment;
import eu.daiad.common.model.query.savings.BudgetSnapshot;
import eu.daiad.common.model.scheduling.Constants;
import eu.daiad.common.model.scheduling.JobResponse;
import eu.daiad.common.model.scheduling.LaunchJobRequest;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.utility.UtilityInfo;
import eu.daiad.common.repository.application.IBudgetRepository;
import eu.daiad.common.repository.application.IGroupRepository;
import eu.daiad.common.repository.application.IUtilityRepository;
import eu.daiad.common.service.BaseService;
import eu.daiad.common.service.IDataService;
import eu.daiad.utility.feign.client.SchedulerFeignClient;

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
     * Scheduler client used for querying, scheduling and launching jobs.
     */
    @Autowired
    private ObjectProvider<SchedulerFeignClient> schedulerClient;

    /**
     * Repository for accessing budget data.
     */
    @Autowired
    private IBudgetRepository budgetRepository;

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
		LaunchJobRequest request = new LaunchJobRequest();
		
		// TODO: Use constants
		
        // Set budget key
		request.param(
			"compute-snapshot" + Constants.PARAMETER_NAME_DELIMITER + "budget.key",
			key.toString()
		);

        // Set reference date
		request.param(
			"compute-snapshot" + Constants.PARAMETER_NAME_DELIMITER + "reference.date",
			String.format("%d%02d01", year, month)
		);
        
        this.schedulerClient.getObject().launch(request, "BUDGET");
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
        
    	final ResponseEntity<JobResponse> e = this.schedulerClient.getObject().getJob("BUDGET");       	
        
        final JobResponse res = e.getBody();

		final DateTime nextUpdateOn = new DateTime(res.getJob().getNextExecution().longValue());
    	
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
            budgetRepository.updateSnapshot(
        		key, snapshotId, consumptionTotalBefore, consumptionTotalAfter, DateTime.now(), nextUpdateOn
    		);
        } catch (Exception ex) {
            logger.error("Snapshot creation has failed.", ex);
            if (snapshotId != null) {
                budgetRepository.updateSnapshot(key, snapshotId, DateTime.now(), nextUpdateOn);
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
        List<Budget> result = new ArrayList<>();

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
        List<BudgetSnapshot> snapshots = new ArrayList<>();

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
