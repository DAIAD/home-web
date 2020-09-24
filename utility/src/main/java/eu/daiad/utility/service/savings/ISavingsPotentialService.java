package eu.daiad.utility.service.savings;

import java.util.List;
import java.util.UUID;

import eu.daiad.common.domain.application.SavingsPotentialResultEntity;
import eu.daiad.common.model.query.savings.SavingScenario;
import eu.daiad.common.model.query.savings.SavingScenarioExploreResult;
import eu.daiad.common.model.query.savings.SavingScenarioQuery;
import eu.daiad.common.model.query.savings.SavingScenarioQueryResult;
import eu.daiad.common.model.query.savings.TemporalSavingsConsumerSelectionFilter;
import eu.daiad.common.model.security.AuthenticatedUser;

/**
 * Provides methods for computing and querying savings potential scenarios.
 */
public interface ISavingsPotentialService {

    /**
     * Creates a new savings potential scenario
     *
     * @param user the owner.
     * @param name a user-friendly name for the scenario.
     * @param parameters parameters for selecting user data.
     * @return the new scenario key.
     */
    UUID create(AuthenticatedUser user, String name, TemporalSavingsConsumerSelectionFilter parameters);

    /**
     * Schedules a new job for computing the savings potential scenario results.
     *
     * @param key the scenario key.
     */
    void refresh(UUID key);

    /**
     * Finds the scenario given its key.
     *
     * @param key the scenario key.
     * @return the scenario.
     */
    SavingScenario find(UUID key);

    /**
     * Gets savings potential based on a scenario and cluster.
     *
     * @param scenarioKey the scenario key.
     * @param clusterKey the cluster key.
     * @return the cluster segments with the scenario use potential savings.
     */
    SavingScenarioExploreResult explore(UUID scenarioKey, UUID clusterKey);

    /**
     * Filters scenarios using a query.
     *
     * @param query the query.
     * @return a collection of {@link SavingScenario}.
     */
    SavingScenarioQueryResult find(SavingScenarioQuery query);

    /**
     * Deletes an existing savings potential scenario.
     *
     * @param key the unique scenario key.
     */
    void delete(UUID key);

    /**
     * Returns all the results for a specific scenario.
     *
     * @param key the scenario key.
     * @return a list of {@link SavingsPotentialResultEntity}.
     */
    List<SavingsPotentialResultEntity> getScenarioResults(UUID key);
}
