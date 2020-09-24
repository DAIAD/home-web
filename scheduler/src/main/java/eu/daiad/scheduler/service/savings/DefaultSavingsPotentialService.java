package eu.daiad.scheduler.service.savings;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.common.domain.application.ClusterEntity;
import eu.daiad.common.domain.application.SavingsPotentialResultEntity;
import eu.daiad.common.domain.application.SavingsPotentialScenarioEntity;
import eu.daiad.common.domain.application.mappings.SavingScenarioSegmentEntity;
import eu.daiad.common.model.error.SavingsPotentialErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.query.savings.SavingScenario;
import eu.daiad.common.model.query.savings.SavingScenarioExploreResult;
import eu.daiad.common.model.query.savings.SavingScenarioQuery;
import eu.daiad.common.model.query.savings.SavingScenarioQueryResult;
import eu.daiad.common.model.query.savings.SavingScenarioSegment;
import eu.daiad.common.model.query.savings.TemporalSavingsConsumerSelectionFilter;
import eu.daiad.common.model.scheduling.Constants;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.repository.application.IGroupRepository;
import eu.daiad.common.repository.application.ISavingsPotentialRepository;
import eu.daiad.common.service.BaseService;
import eu.daiad.scheduler.job.builder.SavingsPotentialJobBuilder;
import eu.daiad.scheduler.service.scheduling.ISchedulerService;

/**
 * Default implementation of {@link ISavingsPotentialService} that provides
 * methods for computing and querying savings potential scenarios.
 */
@Service
public class DefaultSavingsPotentialService extends BaseService implements ISavingsPotentialService {

    /**
     * Service for querying, scheduling and launching jobs.
     */
    @Autowired
    private ISchedulerService schedulerService;

    /**
     * Repository for accessing group members.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Repository for accessing savings potential scenario data.
     */
    @Autowired
    private ISavingsPotentialRepository savingsPotentialRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Creates a new savings potential scenario
     *
     * @param user the owner.
     * @param name a user-friendly name for the scenario.
     * @param parameters parameters for selecting user data.
     * @return the new scenario key.
     */
    @Override
    public UUID create(AuthenticatedUser user, String name, TemporalSavingsConsumerSelectionFilter parameters) {
        UUID key;
        try {
            key = savingsPotentialRepository.create(user.getId(), name, parameters);

            Map<String, String> jobParameters = new HashMap<>();

            // Set execution mode to SAVINGS
            String parameterKey = Constants.PARAMETER_NAME_DELIMITER +
                                  SavingsPotentialJobBuilder.EnumJobInParameter.MODE.getValue();
            String parameterValue = SavingsPotentialJobBuilder.MODE_SAVINGS;
            jobParameters.put(parameterKey, parameterValue);

            // Set scenario key
            parameterKey = Constants.PARAMETER_NAME_DELIMITER +
                           SavingsPotentialJobBuilder.EnumJobInParameter.SCENARIO_KEY.getValue();
            parameterValue = key.toString();
            jobParameters.put(parameterKey, parameterValue);

            schedulerService.launch("SAVINGS-POTENTIAL", jobParameters);
        } catch(Exception ex) {
            throw wrapApplicationException(ex, SavingsPotentialErrorCode.CREATION_FAILED);
        }

        return key;
    }

    /**
     * Schedules a new job for computing the savings potential scenario results.
     *
     * @param key the scenario key.
     */
    @Override
    public void refresh(UUID key) {
        AuthenticatedUser user = getAuthenticatedUser();
        SavingsPotentialScenarioEntity entity = savingsPotentialRepository.getScenarioByKey(key);

        if ((user != null) && (user.getId() != entity.getOwner().getId())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        try {
            savingsPotentialRepository.reset(key);

            Map<String, String> jobParameters = new HashMap<>();

            // Set execution mode to SAVINGS
            String parameterKey = Constants.PARAMETER_NAME_DELIMITER +
                                  SavingsPotentialJobBuilder.EnumJobInParameter.MODE.getValue();
            String parameterValue = SavingsPotentialJobBuilder.MODE_SAVINGS;
            jobParameters.put(parameterKey, parameterValue);

            // Set scenario key
            parameterKey = Constants.PARAMETER_NAME_DELIMITER +
                           SavingsPotentialJobBuilder.EnumJobInParameter.SCENARIO_KEY.getValue();
            parameterValue = key.toString();
            jobParameters.put(parameterKey, parameterValue);

            schedulerService.launch("SAVINGS-POTENTIAL", jobParameters);
        } catch(Exception ex) {
            throw wrapApplicationException(ex, SavingsPotentialErrorCode.CREATION_FAILED);
        }
    }

    /**
     * Finds the scenario given its key.
     *
     * @param key the scenario key.
     * @return the scenario.
     */
    @Override
    public SavingScenario find(UUID key) {
        AuthenticatedUser user = getAuthenticatedUser();
        SavingsPotentialScenarioEntity entity = savingsPotentialRepository.getScenarioByKey(key);

        if ((user != null) && (user.getId() != entity.getOwner().getId())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        try {
            return new SavingScenario(entity, objectMapper);
        } catch (IOException e) {
            throw createApplicationException(SavingsPotentialErrorCode.PARSE_ERROR);
        }
    }

    /**
     * Gets savings potential based on a scenario and cluster.
     *
     * @param scenarioKey the scenario key.
     * @param clusterKey the cluster key.
     * @return the cluster segments with the scenario use potential savings.
     */
    @Override
    public SavingScenarioExploreResult explore(UUID scenarioKey, UUID clusterKey) {
        AuthenticatedUser user = getAuthenticatedUser();

        SavingsPotentialScenarioEntity scenario = savingsPotentialRepository.getScenarioByKey(scenarioKey);

        if ((user != null) && (user.getId() != scenario.getOwner().getId())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        ClusterEntity cluster = groupRepository.getClusterByKey(clusterKey);

        List<SavingScenarioSegmentEntity> segments = savingsPotentialRepository.explore(scenarioKey, clusterKey);

        SavingScenarioExploreResult result = new SavingScenarioExploreResult();
        result.setClusterKey(cluster.getKey());
        result.setClusterName(cluster.getName());
        result.setScenarioKey(scenario.getKey());
        result.setScenarioName(scenario.getName());

        for (SavingScenarioSegmentEntity s : segments) {
            result.getSegments().add(new SavingScenarioSegment(s));
        }

        return result;
    }

    /**
     * Deletes an existing savings potential scenario.
     *
     * @param key the unique scenario key.
     */
    @Override
    public void delete(UUID key) {
        AuthenticatedUser user = getAuthenticatedUser();
        SavingsPotentialScenarioEntity entity = savingsPotentialRepository.getScenarioByKey(key);

        if ((user != null) && (user.getId() != entity.getOwner().getId())) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        savingsPotentialRepository.deleteScenarioByKey(key);
    }

    @Override
    public SavingScenarioQueryResult find(SavingScenarioQuery query) {
        if (query != null) {
            query.setOwnerId(getAuthenticatedUser().getId());

            return savingsPotentialRepository.query(query);
        }
        return new SavingScenarioQueryResult(0, 10);
    }

    private AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if ((authentication != null) && (authentication.getPrincipal() instanceof AuthenticatedUser)) {
            return (AuthenticatedUser) authentication.getPrincipal();
        }
        return null;
    }


    /**
     * Returns all the results for a specific scenario.
     *
     * @param key the scenario key.
     * @return a list of {@link SavingsPotentialResultEntity}.
     */
    @Override
    public List<SavingsPotentialResultEntity> getScenarioResults(UUID key) {
        return savingsPotentialRepository.getScenarioResults(key);
    }


}
