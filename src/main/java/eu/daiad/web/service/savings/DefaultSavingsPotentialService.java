package eu.daiad.web.service.savings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.AreaGroupMemberEntity;
import eu.daiad.web.domain.application.ClusterEntity;
import eu.daiad.web.domain.application.SavingsPotentialResultEntity;
import eu.daiad.web.domain.application.SavingsPotentialScenarioEntity;
import eu.daiad.web.domain.application.mappings.SavingScenarioSegmentEntity;
import eu.daiad.web.job.builder.SavingsPotentialJobBuilder;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.SavingsPotentialErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.query.AreaSpatialFilter;
import eu.daiad.web.model.query.ConstraintSpatialFilter;
import eu.daiad.web.model.query.GroupSpatialFilter;
import eu.daiad.web.model.query.SpatialFilter;
import eu.daiad.web.model.query.savings.SavingScenario;
import eu.daiad.web.model.query.savings.SavingScenarioExploreResult;
import eu.daiad.web.model.query.savings.SavingScenarioParameters;
import eu.daiad.web.model.query.savings.SavingScenarioQuery;
import eu.daiad.web.model.query.savings.SavingScenarioQueryResult;
import eu.daiad.web.model.query.savings.SavingScenarioSegment;
import eu.daiad.web.model.query.savings.SavingsPopulationFilter;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.ISavingsPotentialRepository;
import eu.daiad.web.repository.application.ISpatialRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.BaseService;
import eu.daiad.web.service.scheduling.Constants;
import eu.daiad.web.service.scheduling.ISchedulerService;

/**
 * Default implementation of {@link ISavingsPotentialService} that provides
 * methods for computing and querying savings potential scenarios.
 */
@Service
public class DefaultSavingsPotentialService extends BaseService implements ISavingsPotentialService, InitializingBean {

    /**
     * Service for querying, scheduling and launching jobs.
     */
    @Autowired
    private ISchedulerService schedulerService;

    /**
     * Repository for accessing utility members.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Repository for accessing group members.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Repository for accessing smart water meter data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

    /**
     * Repository for accessing spatial data.
     */
    @Autowired
    private ISpatialRepository spatialRepository;

    /**
     * Repository for accessing savings potential scenario data.
     */
    @Autowired
    ISavingsPotentialRepository savingsPotentialRepository;

    /**
     * A builder used to create {@link ObjectMapper} instances for serializing scenario parameters.
     */
    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    /**
     * Object mapper for serializing scenario parameters.
     */
    private ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        objectMapper = jackson2ObjectMapperBuilder.build();
    }

    /**
     * Creates a new savings potential scenario
     *
     * @param user the owner.
     * @param name a user-friendly name for the scenario.
     * @param parameters parameters for selecting user data.
     * @return the new scenario key.
     */
    @Override
    public UUID create(AuthenticatedUser user, String name, SavingScenarioParameters parameters) {
        UUID key;
        try {
            key = savingsPotentialRepository.create(user.getId(), name, parameters);

            Map<String, String> jobParameters = new HashMap<String, String>();

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

            Map<String, String> jobParameters = new HashMap<String, String>();

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

    /**
     * Given a scenario {@link SavingScenarioParameters}, return all selected
     * smart water meter serial numbers.
     *
     * @param parameters scenario parameters.
     * @return a list of strings.
     */
    @Override
    public List<String> expandMeters(SavingScenarioParameters parameters) {
        List<String> values =  new ArrayList<String>();

        if(parameters == null) {
            return values;
        }

        // Separate spatial filters from simple spatial constraints
        List<ConstraintSpatialFilter> spatialConstraints = new ArrayList<ConstraintSpatialFilter>();
        List<SpatialFilter> spatialFilters = new ArrayList<SpatialFilter>();

        if (parameters.getSpatial() != null) {
            for (SpatialFilter spatialFilter : parameters.getSpatial()) {
                switch (spatialFilter.getType()) {
                    case CONSTRAINT:
                        spatialConstraints.add((ConstraintSpatialFilter) spatialFilter);
                        break;
                    default:
                        spatialFilters.add(spatialFilter);
                        break;
                }
            }
        }

        // Expand spatial filters
        List<Geometry> areas = new ArrayList<Geometry>();

        for (SpatialFilter spatialFilter : spatialFilters) {
            switch (spatialFilter.getType()) {
                case AREA:
                    AreaSpatialFilter areaSpatialQuery = (AreaSpatialFilter) spatialFilter;

                    for (UUID key : areaSpatialQuery.getAreas()) {
                        AreaGroupMemberEntity areaEntity = spatialRepository.getAreaByKey(key);

                        if (filterArea(spatialConstraints, areaEntity.getGeometry())) {
                            areas.add(areaEntity.getGeometry());
                        }
                    }

                    break;
                case GROUP:
                    GroupSpatialFilter groupSpatialFilter = (GroupSpatialFilter) spatialFilter;

                    for (AreaGroupMemberEntity areaEntity : spatialRepository.getAreasByAreaGroupKey(groupSpatialFilter.getGroup())) {
                        if (filterArea(spatialConstraints, areaEntity.getGeometry())) {
                            areas.add(areaEntity.getGeometry());
                        }
                    }
                    break;
                default:
                    // Ignore
                    break;
            }
        }

        Set<String> selected = new HashSet<String>();

        if ((parameters.getPopulation() != null) && (!parameters.getPopulation().isEmpty())) {
            Set<String> current = new HashSet<String>();

            for (int index = 0; index < parameters.getPopulation().size(); index++) {
                SavingsPopulationFilter filter = parameters.getPopulation().get(index);

                List<UUID> users = null;

                switch (filter.getType()) {
                    case GROUP:
                        users = groupRepository.getGroupMemberKeys(filter.getKey());
                        break;
                    case UTILITY:
                        users = utilityRepository.getMembers(filter.getKey());
                        break;
                    default:
                        // Ignore
                }

                if ((users == null) || (users.isEmpty())) {
                    continue;
                }

                for(UUID userKey: users) {
                    WaterMeterDevice meter = getUserWaterMeter(userKey);

                    if ((meter != null) && (filterMeterLocation(areas, meter.getLocation()))) {
                        current.add(meter.getSerial());
                    }
                }

                if(index == 0) {
                    selected.addAll(current);
                } else {
                    selected.retainAll(current);
                }
            }
        }
        for(String serial : selected) {
            values.add(serial);
        }
        return values;
    }

    /**
     * Filters a geometry using a list of {@link ConstraintSpatialFilter}.
     *
     * @param constraints the list of {@link ConstraintSpatialFilter}.
     * @param geometry the geometry.
     * @return true if all the spatial constraints are valid.
     */
    private boolean filterArea(List<ConstraintSpatialFilter> constraints, Geometry area) {
        boolean result = true;

        if ((constraints != null) && (!constraints.isEmpty())) {
            for (ConstraintSpatialFilter constraint : constraints) {
                switch (constraint.getOperation()) {
                    case CONTAINS:
                        result = result && constraint.getGeometry().contains(area);
                        break;
                    case INTERSECT:
                        result = result && constraint.getGeometry().intersects(area);
                        break;
                    case DISTANCE:
                        result = result && (distance(constraint.getGeometry(), area) < constraint.getDistance());
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Spatial constraint operation [%s] is not supported",
                                                                         constraint.getOperation()));
                }
                if (!result) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Filters a geometry using a list of areas. The location must be contained
     * into at least one of the given areas.
     *
     * @param areas a list of areas.
     * @param location the location.
     * @return true if at least one area contains the location or the area list is empty.
     */
    private boolean filterMeterLocation(List<Geometry> areas, Geometry location) {
        if (areas.isEmpty()) {
            return true;
        }
        if (location == null) {
            return false;
        }
        for (Geometry area : areas) {
            if (area.contains(location)) {
                return true;
            }
        }
        return false;
    }

    private double distance(Geometry g1, Geometry g2) {
        double distance = g1.distance(g2);

        return distance * (Math.PI / 180) * 6378137;
    }

    private WaterMeterDevice getUserWaterMeter(UUID userKey) {
        DeviceRegistrationQuery meterQuery = new DeviceRegistrationQuery();
        meterQuery.setType(EnumDeviceType.METER);

        List<Device> devices = deviceRepository.getUserDevices(userKey, meterQuery);

        if (devices.isEmpty()) {
            return null;
        }

        return (WaterMeterDevice) devices.get(0);
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
