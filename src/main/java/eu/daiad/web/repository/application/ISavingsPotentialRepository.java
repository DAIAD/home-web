package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.daiad.web.domain.application.SavingsPotentialResultEntity;
import eu.daiad.web.domain.application.SavingsPotentialScenarioEntity;
import eu.daiad.web.domain.application.mappings.SavingScenarioSegmentEntity;
import eu.daiad.web.model.query.savings.EnumSavingScenarioStatus;
import eu.daiad.web.model.query.savings.SavingScenario;
import eu.daiad.web.model.query.savings.SavingScenarioQuery;
import eu.daiad.web.model.query.savings.SavingScenarioQueryResult;
import eu.daiad.web.model.query.savings.SavingsClusterCollection;
import eu.daiad.web.model.query.savings.TemporalSavingsConsumerSelectionFilter;

/**
 * Provides methods for accessing savings potential and water IQ data.
 */
public interface ISavingsPotentialRepository {

    /**
     * Creates a new savings potential scenario
     *
     * @param ownerId the owner account id.
     * @param name a user-friendly name for the scenario.
     * @param parameters parameters for selecting user data.
     * @return the new scenario key.
     * @throws JsonProcessingException if parameters serialization fails
     */
    UUID create(int ownerId, String name, TemporalSavingsConsumerSelectionFilter parameters) throws JsonProcessingException ;

    /**
     * Resets the results of a savings potential scenario.
     *
     * @param key the unique scenario key.
     */
    void reset(UUID key);

    /**
     * Loads a savings potential scenario given its key.
     *
     * @param key the unique scenario key.
     * @return the scenario.
     */
    SavingsPotentialScenarioEntity getScenarioByKey(UUID key);

    /**
     * Loads a savings potential scenario given its id.
     *
     * @param id the scenario id.
     * @return the scenario.
     */
    SavingsPotentialScenarioEntity getScenarioById(long id);

    /**
     * Deletes an existing savings potential scenario given its key.
     *
     * @param key the unique scenario key.
     */
    void deleteScenarioByKey(UUID key);

    /**
     * Updates scenario job execution.
     *
     * @param key the scenario key.
     * @param status new status value.
     * @param updatedOn update timestamp.
     */
    void updateJobExecution(UUID key, EnumSavingScenarioStatus status, DateTime updatedOn);

    /**
     * Filter savings potential scenario using a query.
     *
     * @param query the query.
     * @return a collection of {@link SavingScenario}.
     */
    SavingScenarioQueryResult query(SavingScenarioQuery query);

    /**
     * Updates scenario savings values.
     *
     * @param key the scenario key.
     * @param consumption consumption volume.
     * @param saved savings volume.
     * @param updatedOn update timestamp.
     */
    void updateSavings(UUID key, double consumption, double saved, DateTime updatedOn);

    /**
     * Updates scenario user savings values.
     *
     * @param scenarioKey the scenario key.
     * @param userKey the user key.
     * @param consumption consumption volume.
     * @param saved savings volume.
     * @param updatedOn update timestamp.
     */
    void updateSavings(UUID scenarioKey, UUID userKey, double consumption, double saved, DateTime updatedOn);

    /**
     * Stores savings potential data to data store.
     *
     * @param scenarioId scenario id.
     * @param jobId job id
     * @param clusters the clusters.
     */
    void storeSavings(long scenarioId, long jobId, SavingsClusterCollection clusters);

    /**
     * Stores Water IQ data to data store.
     *
     * @param utilityId utility id
     * @param jobId job id
     * @param clusters the clusters.
     */
    void storeWaterIq(int utilityId, long jobId, SavingsClusterCollection clusters);

    /**
     * Returns all the results for a specific scenario.
     *
     * @param key the scenario key.
     * @return a list of {@link SavingsPotentialResultEntity}.
     */
    List<SavingsPotentialResultEntity> getScenarioResults(UUID key);

    /**
     * Compute savings potential per cluster segment for a given scenario and cluster.
     *
     * @param scenarioKey the scenario key.
     * @param clusterKey the cluster key.
     * @return a list of {@link SavingScenarioSegmentEntity}.
     */
    List<SavingScenarioSegmentEntity> explore(UUID scenarioKey, UUID clusterKey);

    /**
     * Resets status for all savings scenarios whose processing has been
     * interrupted.
     */
    void cleanStatus();

}
