package eu.daiad.utility.service.savings;

import java.util.List;
import java.util.UUID;

import eu.daiad.common.model.query.savings.Budget;
import eu.daiad.common.model.query.savings.BudgetExploreClusterResult;
import eu.daiad.common.model.query.savings.BudgetExploreConsumerResult;
import eu.daiad.common.model.query.savings.BudgetParameters;
import eu.daiad.common.model.query.savings.BudgetQuery;
import eu.daiad.common.model.query.savings.BudgetQueryResult;
import eu.daiad.common.model.query.savings.BudgetSnapshot;
import eu.daiad.common.model.security.AuthenticatedUser;

/**
 * Provides methods for computing and querying budgets.
 */
public interface IBudgetService {

    /**
     * Creates a new budget.
     *
     * @param user the owner.
     * @param name a user-friendly name for the budget.
     * @param parameters parameters for selecting user data.
     * @return the new budget key.
     */
    UUID createBudget(AuthenticatedUser user, String name, BudgetParameters parameters);

    /**
     * Schedules the execution of a job for computing a snapshot for a budget for a selected year and month.
     *
     * @param key the budget key.
     * @param year the reference year.
     * @param month the reference month.
     */
    void scheduleSnapshotCreation(UUID key, int year, int month);

    /**
     * Creates a snapshot for an active budget for a selected year and month.
     *
     * @param jobId the id of the job that created the snapshot.
     * @param key the budget key.
     * @param year the reference year.
     * @param month the reference month.
     */
    void createSnapshot(long jobId, UUID key, int year, int month);


    /**
     * Finds the budget given its key.
     *
     * @param key the budget key.
     * @return the budget.
     */
    Budget find(UUID key);

    /**
     * Gets all active budgets.
     *
     * @return a list of {@link Budget}.
     */
    List<Budget> findActive();

    /**
     * Gets all pending budget snapshots.
     *
     * @return a list of {@link BudgetSnapshot}.
     */
    List<BudgetSnapshot> findPendingSnapshots();

    /**
     * Filters budgets using a query.
     *
     * @param query the query.
     * @return a collection of {@link Budget}.
     */
    BudgetQueryResult find(BudgetQuery query);

    /**
     * Deletes an existing budget.
     *
     * @param key the unique budget key.
     */
    void delete(UUID key);

    /**
     * Sets active state for a budget.
     *
     * @param key the budget key.
     * @param active the new active state value.
     */
    void setActive(UUID key, boolean active);

    /**
     * Gets consumption data based on an active budget and cluster.
     *
     * @param budgetKey the budget key.
     * @param clusterKey the cluster key.
     * @return the cluster segments with the budget population consumption.
     */
    BudgetExploreClusterResult exploreCluster(UUID budgetKey, UUID clusterKey);

    /**
     * Gets consumption data based on an active budget and user.
     *
     * @param budgetKey the budget key.
     * @param userKey the user key.
     * @return the user consumption data.
     */
    BudgetExploreConsumerResult exploreConsumer(UUID budgetKey, UUID userKey);

}
