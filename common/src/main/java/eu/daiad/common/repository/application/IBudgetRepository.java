package eu.daiad.common.repository.application;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.daiad.common.domain.application.BudgetEntity;
import eu.daiad.common.domain.application.BudgetSnapshotEntity;
import eu.daiad.common.domain.application.mappings.BudgetSegmentEntity;
import eu.daiad.common.model.query.savings.Budget;
import eu.daiad.common.model.query.savings.BudgetExploreConsumerResult;
import eu.daiad.common.model.query.savings.BudgetParameters;
import eu.daiad.common.model.query.savings.BudgetQuery;
import eu.daiad.common.model.query.savings.BudgetQueryResult;

/**
 * Provides methods for accessing budget data.
 */
public interface IBudgetRepository {

    /**
     * Creates a new budget.
     *
     * @param ownerId the owner account id.
     * @param name a user-friendly name for the budget.
     * @param parameters parameters for selecting user data.
     * @return the new budget key.
     * @throws IOException if initialization fails.
     */
    UUID createBudget(int ownerId, String name, BudgetParameters parameters) throws IOException ;

    /**
     * Initializes the budget consumers.
     *
     * @param budgetKey the budget key.
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    void initializeBudgetConsumers(UUID budgetKey) throws JsonParseException, JsonMappingException, IOException;

    /**
     * Resets a snapshot for an active budget.
     *
     * @param key the unique budget key.
     * @param year reference date year.
     * @param month reference date month.
     */
    void deleteSnapshot(UUID key, int year, int month);

    /**
     * Loads a budget given its key.
     *
     * @param key the unique budget key.
     * @return the budget.
     */
    BudgetEntity getBudgetByKey(UUID key);

    /**
     * Gets all active budgets.
     *
     * @return a list of {@link BudgetEntity} objects.
     */
    List<BudgetEntity> getActiveBudgets();

    /**
     * Gets all pending budget snapshots.
     *
     * @return a list of {@link BudgetSnapshotEntity}.
     */
    List<BudgetSnapshotEntity> findPendingSnapshots();

    /**
     * Loads a budget given its id.
     *
     * @param id the budget id.
     * @return the budget.
     */
    BudgetEntity getBudgetById(long id);

    /**
     * Deletes an existing budget given its key.
     *
     * @param key the unique budget key.
     */
    void deleteBudgetByKey(UUID key);

    /**
     * Sets active state for a budget.
     *
     * @param key the budget key.
     * @param active the new active state value.
     */
    void setActive(UUID key, boolean active);

    /**
     * Filter budget using a query.
     *
     * @param query the query.
     * @return a collection of {@link Budget}.
     */
    BudgetQueryResult query(BudgetQuery query);

    /**
     * Updates budget snapshot consumption values.
     *
     * @param key the budget key.
     * @param jobId the id of the job that created the snapshot.
     * @param year reference date year.
     * @param month reference date month.
     * @return the new snapshot id.
     */
    long createSnapshot(UUID key, long jobId, int year, int month);

    /**
     * Updates budget snapshot consumption values and sets its state to COMPLETED.
     *
     * @param key the budget key.
     * @param snapshotId the snapshot id.
     * @param consumptionBefore previous consumption volume.
     * @param consumptionAfter current consumption volume.
     * @param updatedOn update timestamp.
     * @param nextUpdateOn next update timestamp.
     */
    void updateSnapshot(
		UUID key, long snapshotId, double consumptionBefore, double consumptionAfter, DateTime updatedOn, DateTime nextUpdateOn
	);

    /**
     * Updates budget snapshot and sets its state to FAILED.
     *
     * @param key the budget key.
     * @param snapshotId the snapshot id.
     * @param updatedOn update timestamp.
     * @param nextUpdateOn next update timestamp.
     */
    void updateSnapshot(UUID key, long snapshotId, DateTime updatedOn, DateTime nextUpdateOn);

    /**
     * Updates budget user consumption values.
     *
     * @param budgetKey the budget key.
     * @param snapshotId the snapshot id.
     * @param userKey the user key.
     * @param consumptionBefore previous consumption volume.
     * @param consumptionAfter current consumption volume.
     * @param updatedOn update timestamp.
     */
    void updateConsumer(UUID budgetKey, long snapshotId, UUID userKey, double consumptionBefore, double consumptionAfter, DateTime updatedOn);

    /**
     * Compute consumption per cluster segment for a given budget and cluster.
     *
     * @param budgetKey the budget key.
     * @param clusterKey the cluster key.
     * @return a list of {@link BudgetSegmentEntity}.
     */
    List<BudgetSegmentEntity> exploreCluster(UUID budgetKey, UUID clusterKey);


    /**
     * Query consumer consumption data for a given budget and user.
     *
     * @param budgetKey the budget key.
     * @param consumerKey the consumer key.
     * @return a list of {@link BudgetSegmentEntity}.
     */
    BudgetExploreConsumerResult exploreConsumer(UUID budgetKey, UUID consumerKey);

    /**
     * Gets all budget consumers user keys.
     *
     * @param budgetKey the budget key.
     * @return a list of {@link UUID}.
     */
    List<UUID> getBudgetMembers(UUID budgetKey);

    /**
     * Resets status for all savings scenarios whose processing has been
     * interrupted.
     */
    void cleanStatus();

}
