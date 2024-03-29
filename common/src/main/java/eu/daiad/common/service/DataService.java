package eu.daiad.common.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.daiad.common.domain.application.AccountEntity;
import eu.daiad.common.domain.application.AreaGroupMemberEntity;
import eu.daiad.common.model.device.Device;
import eu.daiad.common.model.device.DeviceRegistrationQuery;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.device.WaterMeterDevice;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.Error;
import eu.daiad.common.model.error.ErrorCode;
import eu.daiad.common.model.error.QueryErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.error.UserErrorCode;
import eu.daiad.common.model.group.Group;
import eu.daiad.common.model.group.Set;
import eu.daiad.common.model.query.AbstractDataQuery;
import eu.daiad.common.model.query.AreaSpatialFilter;
import eu.daiad.common.model.query.ClusterPopulationFilter;
import eu.daiad.common.model.query.ConstraintSpatialFilter;
import eu.daiad.common.model.query.DataQuery;
import eu.daiad.common.model.query.DataQueryResponse;
import eu.daiad.common.model.query.EnumClusterType;
import eu.daiad.common.model.query.EnumDataField;
import eu.daiad.common.model.query.EnumMeasurementDataSource;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.EnumPopulationFilterType;
import eu.daiad.common.model.query.EnumRankingType;
import eu.daiad.common.model.query.ExpandedDataQuery;
import eu.daiad.common.model.query.ExpandedPopulationFilter;
import eu.daiad.common.model.query.ForecastQuery;
import eu.daiad.common.model.query.ForecastQueryResponse;
import eu.daiad.common.model.query.GroupDataSeries;
import eu.daiad.common.model.query.GroupPopulationFilter;
import eu.daiad.common.model.query.GroupSpatialFilter;
import eu.daiad.common.model.query.NamedDataQuery;
import eu.daiad.common.model.query.PopulationFilter;
import eu.daiad.common.model.query.SpatialFilter;
import eu.daiad.common.model.query.UserPopulationFilter;
import eu.daiad.common.model.query.UtilityPopulationFilter;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.model.spatial.LabeledGeometry;
import eu.daiad.common.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.common.repository.application.ICommonsRepository;
import eu.daiad.common.repository.application.IDeviceRepository;
import eu.daiad.common.repository.application.IFavouriteRepository;
import eu.daiad.common.repository.application.IGroupRepository;
import eu.daiad.common.repository.application.IMeterAggregateDataRepository;
import eu.daiad.common.repository.application.IMeterDataRepository;
import eu.daiad.common.repository.application.IMeterForecastingAggregateDataRepository;
import eu.daiad.common.repository.application.IMeterForecastingDataRepository;
import eu.daiad.common.repository.application.ISpatialRepository;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.repository.application.IUtilityRepository;

@Service
public class DataService extends BaseService implements IDataService {

    @Autowired
    private IUtilityRepository utilityRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IGroupRepository groupRepository;

    @Autowired
    private ICommonsRepository commonsRepository;

    @Autowired
    private IDeviceRepository deviceRepository;

    @Autowired
    private ISpatialRepository spatialRepository;

    @Autowired
    IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    @Autowired
    IMeterDataRepository meterDataRepository;

    @Autowired
    IMeterAggregateDataRepository meterAggregateDataRepository;

    @Autowired
    IMeterForecastingDataRepository meterForecastingDataRepository;

    @Autowired
    IMeterForecastingAggregateDataRepository meterForecastingAggregateDataRepository;

    @Autowired
    IFavouriteRepository favouriteRepository;

    protected Error getError(ErrorCode error) {
        return new Error(error.getMessageKey(), this.getMessage(error));
    }

    protected Error getError(ErrorCode error, Map<String, Object> properties) {
        return new Error(error.getMessageKey(), this.getMessage(error, properties));
    }

    private void validate(DataQuery query, DataQueryResponse response) {
        // Time
        if (query.getTime() == null) {
            response.add(this.getError(QueryErrorCode.TIME_FILTER_NOT_SET));
        } else {
            // Granularity
            switch (query.getTime().getType()) {
                case ABSOLUTE:
                    if (query.getTime().getEnd() == null) {
                        response.add(this.getError(QueryErrorCode.TIME_FILTER_ABSOLUTE_END_NOT_SET));
                    }
                    break;
                case SLIDING:
                    if (query.getTime().getDuration() == null) {
                        response.add(this.getError(QueryErrorCode.TIME_FILTER_SLIDING_DURATION_NOT_SET));
                    }
                    break;
                default:
                    response.add(this.getError(QueryErrorCode.TIME_FILTER_INVALID));
                    break;
            }
        }

        // Spatial
        if ((query.getSpatial() != null) && (!query.getSpatial().isEmpty())) {
            for (SpatialFilter spatialFilter : query.getSpatial()) {
                switch (spatialFilter.getType()) {
                    case CONSTRAINT:
                        ConstraintSpatialFilter constraint = (ConstraintSpatialFilter) spatialFilter;

                        if (constraint.getGeometry() == null) {
                            response.add(this.getError(QueryErrorCode.SPATIAL_FILTER_GEOMETRY_NOT_SET));
                        }
                        switch (constraint.getOperation()) {
                            case CONTAINS:
                                break;
                            case INTERSECT:
                                break;
                            case DISTANCE:
                                if (constraint.getDistance() == null) {
                                    response.add(this.getError(QueryErrorCode.SPATIAL_FILTER_DISTANCE_NOT_SET));
                                }
                                break;
                            default:
                                break;
                        }

                        break;
                    default:
                        // Ignore
                        break;
                }
            }
        }

        // Population
        if ((query.getPopulation() == null) || (query.getPopulation().size() == 0)) {
            response.add(this.getError(QueryErrorCode.POPULATION_FILTER_NOT_SET));
        } else {
            for (PopulationFilter filter : query.getPopulation()) {
                switch (filter.getType()) {
                    case USER:
                        UserPopulationFilter userFilter = (UserPopulationFilter) filter;
                        if ((userFilter.getUsers() == null) || (userFilter.getUsers().size() == 0)) {
                            response.add(this.getError(QueryErrorCode.POPULATION_FILTER_IS_EMPTY));
                        }
                        break;
                    case GROUP:
                        GroupPopulationFilter groupFilter = (GroupPopulationFilter) filter;
                        if (groupFilter.getGroup() == null) {
                            response.add(this.getError(QueryErrorCode.POPULATION_FILTER_IS_EMPTY));
                        }
                        break;
                    case CLUSTER:
                        ClusterPopulationFilter clusterFilter = (ClusterPopulationFilter) filter;
                        int propertyCount = 0;
                        if (clusterFilter.getCluster() != null) {
                            propertyCount++;
                        }
                        if (!StringUtils.isBlank(clusterFilter.getName())) {
                            propertyCount++;
                        }
                        if ((clusterFilter.getClusterType() != null) && (!clusterFilter.getClusterType().equals(EnumClusterType.UNDEFINED))) {
                            propertyCount++;
                        }
                        if (propertyCount != 1) {
                            response.add(this.getError(QueryErrorCode.POPULATION_FILTER_INVALID_CLUSTER));
                        }
                        break;
                    case UTILITY:
                        UtilityPopulationFilter utilityFilter = (UtilityPopulationFilter) filter;
                        if (utilityFilter.getUtility() == null) {
                            response.add(this.getError(QueryErrorCode.POPULATION_FILTER_IS_EMPTY));
                        }
                        break;
                    default:
                        response.add(this.getError(QueryErrorCode.POPULATION_FILTER_INVALID));
                        break;

                }

                // Ranking
                if (filter.getRanking() != null) {
                    if (filter.getRanking().getType().equals(EnumRankingType.UNDEFINED)) {
                        response.add(this.getError(QueryErrorCode.RANKING_TYPE_NOT_SET));
                    }
                    if ((filter.getRanking().getLimit() == null) || (filter.getRanking().getLimit() < 1)) {
                        response.add(this.getError(QueryErrorCode.RANKING_INVALID_LIMIT));
                    }
                    if (filter.getRanking().getField().equals(EnumDataField.UNDEFINED)) {
                        response.add(this.getError(QueryErrorCode.RANKING_INVALID_FIELD));
                    }
                    if (filter.getRanking().getMetric().equals(EnumMetric.UNDEFINED)) {
                        response.add(this.getError(QueryErrorCode.RANKING_INVALID_METRIC));
                    }
                    if ((query.getSource().equals(EnumMeasurementDataSource.METER)) || (query.getSource().equals(EnumMeasurementDataSource.BOTH))) {
                        if (!filter.getRanking().getMetric().equals(EnumMetric.SUM)) {
                            response.add(this.getError(QueryErrorCode.RANKING_INVALID_METRIC));
                        }
                        if (!filter.getRanking().getField().equals(EnumDataField.VOLUME)) {
                            response.add(this.getError(QueryErrorCode.RANKING_INVALID_FIELD));
                        }
                    }
                }
            }
        }

        // Metrics
        for (EnumMetric m : query.getMetrics()) {
            if (m.equals(EnumMetric.UNDEFINED)) {
                response.add(this.getError(QueryErrorCode.METRIC_INVALID));
            }
        }

        // Source
        if(query.getSource().equals(EnumMeasurementDataSource.NONE)) {
            response.add(this.getError(QueryErrorCode.SOURCE_INVALID));
        }
    }

    private void validate(ForecastQuery query, ForecastQueryResponse response) {
        // Time
        if (query.getTime() == null) {
            response.add(this.getError(QueryErrorCode.TIME_FILTER_NOT_SET));
        } else {
            switch (query.getTime().getType()) {
                case ABSOLUTE:
                    if (query.getTime().getEnd() == null) {
                        response.add(this.getError(QueryErrorCode.TIME_FILTER_ABSOLUTE_END_NOT_SET));
                    }
                    break;
                case SLIDING:
                    if (query.getTime().getDuration() == null) {
                        response.add(this.getError(QueryErrorCode.TIME_FILTER_SLIDING_DURATION_NOT_SET));
                    }
                    break;
                default:
                    response.add(this.getError(QueryErrorCode.TIME_FILTER_INVALID));
                    break;
            }
        }

        // Spatial
        if ((query.getSpatial() != null) && (!query.getSpatial().isEmpty())) {
            for (SpatialFilter spatialFilter : query.getSpatial()) {
                switch (spatialFilter.getType()) {
                    case CONSTRAINT:
                        ConstraintSpatialFilter constraint = (ConstraintSpatialFilter) spatialFilter;

                        if (constraint.getGeometry() == null) {
                            response.add(this.getError(QueryErrorCode.SPATIAL_FILTER_GEOMETRY_NOT_SET));
                        }
                        switch (constraint.getOperation()) {
                            case CONTAINS:
                                break;
                            case INTERSECT:
                                break;
                            case DISTANCE:
                                if (constraint.getDistance() == null) {
                                    response.add(this.getError(QueryErrorCode.SPATIAL_FILTER_DISTANCE_NOT_SET));
                                }
                                break;
                            default:
                                break;
                        }

                        break;
                    default:
                        // Ignore
                        break;
                }
            }
        }

        // Population
        if ((query.getPopulation() == null) || (query.getPopulation().size() == 0)) {
            response.add(this.getError(QueryErrorCode.POPULATION_FILTER_NOT_SET));
        } else {
            for (PopulationFilter filter : query.getPopulation()) {
                switch (filter.getType()) {
                    case USER:
                        UserPopulationFilter userFilter = (UserPopulationFilter) filter;
                        if ((userFilter.getUsers() == null) || (userFilter.getUsers().size() == 0)) {
                            response.add(this.getError(QueryErrorCode.POPULATION_FILTER_IS_EMPTY));
                        }
                        break;
                    case GROUP:
                        GroupPopulationFilter groupFilter = (GroupPopulationFilter) filter;
                        if (groupFilter.getGroup() == null) {
                            response.add(this.getError(QueryErrorCode.POPULATION_FILTER_IS_EMPTY));
                        }
                        break;
                    case CLUSTER:
                        ClusterPopulationFilter clusterFilter = (ClusterPopulationFilter) filter;
                        int propertyCount = 0;
                        if (clusterFilter.getCluster() != null) {
                            propertyCount++;
                        }
                        if (!StringUtils.isBlank(clusterFilter.getName())) {
                            propertyCount++;
                        }
                        if ((clusterFilter.getClusterType() != null)
                                        && (!clusterFilter.getClusterType().equals(EnumClusterType.UNDEFINED))) {
                            propertyCount++;
                        }
                        if (propertyCount != 1) {
                            response.add(this.getError(QueryErrorCode.POPULATION_FILTER_INVALID_CLUSTER));
                        }
                        break;
                    case UTILITY:
                        UtilityPopulationFilter utilityFilter = (UtilityPopulationFilter) filter;
                        if (utilityFilter.getUtility() == null) {
                            response.add(this.getError(QueryErrorCode.POPULATION_FILTER_IS_EMPTY));
                        }
                        break;
                    default:
                        response.add(this.getError(QueryErrorCode.POPULATION_FILTER_INVALID));
                        break;

                }

                // Ranking
                if (filter.getRanking() != null) {
                    if (filter.getRanking().getType().equals(EnumRankingType.UNDEFINED)) {
                        response.add(this.getError(QueryErrorCode.RANKING_TYPE_NOT_SET));
                    }
                    if ((filter.getRanking().getLimit() == null) || (filter.getRanking().getLimit() < 1)) {
                        response.add(this.getError(QueryErrorCode.RANKING_INVALID_LIMIT));
                    }
                    if (filter.getRanking().getField().equals(EnumDataField.UNDEFINED)) {
                        response.add(this.getError(QueryErrorCode.RANKING_INVALID_FIELD));
                    }
                    if (filter.getRanking().getMetric().equals(EnumMetric.UNDEFINED)) {
                        response.add(this.getError(QueryErrorCode.RANKING_INVALID_METRIC));
                    }
                    if (!filter.getRanking().getMetric().equals(EnumMetric.SUM)) {
                        response.add(this.getError(QueryErrorCode.RANKING_INVALID_METRIC));
                    }
                    if (!filter.getRanking().getField().equals(EnumDataField.VOLUME)) {
                        response.add(this.getError(QueryErrorCode.RANKING_INVALID_FIELD));
                    }
                }
            }
        }

        // Metrics
        for (EnumMetric m : query.getMetrics()) {
            if (m.equals(EnumMetric.UNDEFINED)) {
                response.add(this.getError(QueryErrorCode.METRIC_INVALID));
            }
        }
    }

    /**
     * Executes a generic query for amphiro b1 sessions and smart water meter readings.
     *
     * @param query the query to execute.
     * @return a collection of {@link GroupDataSeries}.
     */
    @Override
    public DataQueryResponse execute(DataQuery query) {
        long startTime = System.currentTimeMillis();

        // Set executor
        setExecutor(query);

        // Set date time zone
        ensureDateTimeZone(query);

        // Initialize response
        DataQueryResponse response = new DataQueryResponse(query.getTimezone());

        // Validate query
        this.validate(query, response);

        if (response.getSuccess()) {
            // Amphiro data does not support aggregation
            if (query.getSource() != EnumMeasurementDataSource.METER) {
                query.setUsingPreAggregation(false);
            }

            if(query.isUsingPreAggregation()){
                executeWithPreAggregation(query, response);
            } else {
                executeWithoutPreAggregation(query, response);
            }
        }

        long stopTime = System.currentTimeMillis();
        response.getExecution().setDuration(stopTime - startTime);

        return response;
    }

    private void executeWithPreAggregation(DataQuery query, DataQueryResponse response) {
        try {
            // Create new query
            ExpandedDataQuery expandedQuery = new ExpandedDataQuery(query.getTimezone());

            // Separate spatial filters from simple constraints
            List<ConstraintSpatialFilter> spatialConstraints = new ArrayList<ConstraintSpatialFilter>();
            List<SpatialFilter> spatialFilters = new ArrayList<SpatialFilter>();

            if (query.getSpatial() != null) {
                for (SpatialFilter spatialFilter : query.getSpatial()) {
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
            List<LabeledGeometry> areas = new ArrayList<LabeledGeometry>();

            for (SpatialFilter spatialFilter : spatialFilters) {
                switch (spatialFilter.getType()) {
                    case AREA:
                        AreaSpatialFilter areaSpatialQuery = (AreaSpatialFilter) spatialFilter;

                        for (UUID key : areaSpatialQuery.getAreas()) {
                            AreaGroupMemberEntity areaEntity = spatialRepository.getAreaByKey(key);

                            if (filterArea(spatialConstraints, areaEntity.getGeometry())) {
                                areas.add(new LabeledGeometry(areaEntity));
                            }
                        }

                        break;
                    case GROUP:
                        GroupSpatialFilter groupSpatialFilter = (GroupSpatialFilter) spatialFilter;

                        for (AreaGroupMemberEntity areaEntity : spatialRepository.getAreasByAreaGroupKey(groupSpatialFilter.getGroup())) {
                            if (filterArea(spatialConstraints, areaEntity.getGeometry())) {
                                areas.add(new LabeledGeometry(areaEntity));
                            }
                        }
                        break;
                    default:
                        // Ignore
                        break;
                }
            }

            // Helper store for caching user location
            Map<UUID, Geometry> userLocations = new HashMap<UUID, Geometry>();

            // Step 1: Generate groups based on the population
            ArrayList<ExpandedPopulationFilter> populationGroups = new ArrayList<ExpandedPopulationFilter>();

            if ((query.getPopulation() != null) && (!query.getPopulation().isEmpty())) {
                MessageDigest md = MessageDigest.getInstance("MD5");

                for (int p = 0; p < query.getPopulation().size(); p++) {
                    PopulationFilter filter = query.getPopulation().get(p);

                    List<UUID> filterUsers = null;

                    switch (filter.getType()) {
                        case USER:
                            filterUsers = ((UserPopulationFilter) filter).getUsers();
                            if (filterUsers != null) {
                                for (UUID userKey : filterUsers) {
                                    authorize(query.getExecutor(), EnumPopulationFilterType.USER, userKey);
                                }
                            }
                            break;
                        case GROUP:
                            UUID groupKey = ((GroupPopulationFilter) filter).getGroup();

                            authorize(query.getExecutor(), EnumPopulationFilterType.GROUP, groupKey);

                            filterUsers = groupRepository.getGroupMemberKeys(groupKey);
                            break;
                        case CLUSTER:
                            ClusterPopulationFilter clusterFilter = (ClusterPopulationFilter) filter;

                            List<Group> groups = null;

                            if (clusterFilter.getCluster() != null) {
                                groups = groupRepository.getClusterSegmentsByKey(clusterFilter.getCluster());
                            } else if ((clusterFilter.getClusterType() != null) && (!clusterFilter.getClusterType().equals(EnumClusterType.UNDEFINED))) {
                                groups = groupRepository.getClusterSegmentsByType(clusterFilter.getClusterType());
                            } else if (!StringUtils.isBlank(clusterFilter.getName())) {
                                groups = groupRepository.getClusterSegmentsByName(clusterFilter.getName());
                            }

                            for (Group group : groups) {
                                if (clusterFilter.getRanking() == null) {
                                    query.getPopulation().add(new GroupPopulationFilter(group.getName(), group.getKey()));
                                } else {
                                    query.getPopulation().add(new GroupPopulationFilter(group.getName(), group.getKey(), clusterFilter.getRanking()));
                                }
                            }
                            continue;
                        case UTILITY:
                            UUID utilityKey = ((UtilityPopulationFilter) filter).getUtility();

                            authorize(query.getExecutor(), EnumPopulationFilterType.UTILITY, utilityKey);

                            filterUsers = groupRepository.getUtilityByKeyMemberKeys(utilityKey);
                            break;
                        default:
                            throw new IllegalArgumentException(String.format("Filter of type [%s] is not supported.", filter.getType()));
                    }

                    // Construct expanded spatial and population filter
                    ExpandedPopulationFilter expandedPopulationFilter = new ExpandedPopulationFilter(filter, filterUsers.size());

                    if ((filter.getType() == EnumPopulationFilterType.USER) && (filterUsers.size() > 0)) {
                        for (UUID userKey : filterUsers) {
                            AuthenticatedUser user = userRepository.getUserByKey(userKey);
                            if (user == null) {
                                throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", userKey);
                            }


                            // Decide if the user must be included in the group
                            boolean includeUser = true;
                            WaterMeterDevice userMeter = null;

                            // Fetch meter only if it is needed
                            if ((query.getSource() == EnumMeasurementDataSource.BOTH) || (query.getSource() == EnumMeasurementDataSource.METER)) {
                                userMeter = getUserWaterMeter(userKey);
                                includeUser = userMeter != null;
                            }

                            // Decide if user should be included in the query
                            if (includeUser) {
                                expandedPopulationFilter.getUserKeys().add(userKey);
                                expandedPopulationFilter.getLabels().add(user.getUsername());
                                expandedPopulationFilter.getUserKeyHashes().add(md.digest(userKey.toString().getBytes("UTF-8")));
                                if (userMeter != null) {
                                    expandedPopulationFilter.getSerialHashes().add(md.digest(userMeter.getSerial().getBytes("UTF-8")));
                                } else {
                                    expandedPopulationFilter.getSerialHashes().add(null);
                                }
                            }

                        }
                    }

                    // Include population groups that either are not of type
                    // USER or have at least one user
                    if ((filter.getType() != EnumPopulationFilterType.USER) ||
                        (!expandedPopulationFilter.getUserKeys().isEmpty())) {
                        populationGroups.add(expandedPopulationFilter);
                    }
                }
            }

            // Step 2: Split population groups depending on the areas
            if (areas.isEmpty()) {
                expandedQuery.getGroups().addAll(populationGroups);
            } else {
                long areaCounter = 0;

                for (LabeledGeometry area : areas) {
                    response.getAreas().put(++areaCounter, area);

                    for (ExpandedPopulationFilter population : populationGroups) {
                        ExpandedPopulationFilter areaPopulation = new ExpandedPopulationFilter(population, areaCounter, area.getKey());

                        for (int i = 0, count = population.getUserKeys().size(); i < count; i++) {
                            if (area.contains(getUserLocation(userLocations, population.getUserKeys().get(i)))) {
                                // Copy fields
                                areaPopulation.getUserKeys().add(population.getUserKeys().get(i));
                                areaPopulation.getLabels().add(population.getLabels().get(i));
                                areaPopulation.getUserKeyHashes().add(population.getUserKeyHashes().get(i));
                                areaPopulation.getSerialHashes().add(population.getSerialHashes().get(i));
                            }
                        }

                        // Include population groups that either are not of type
                        // USER or have at least one user
                        if ((areaPopulation.getType() != EnumPopulationFilterType.USER) ||
                            (!areaPopulation.getUserKeys().isEmpty())) {
                            expandedQuery.getGroups().add(areaPopulation);
                        }
                    }
                }
            }

            // Compute time constraints
            long startDateTime, endDateTime;

            startDateTime = query.getTime().getStart();

            switch (query.getTime().getType()) {
                case ABSOLUTE:
                    endDateTime = query.getTime().getEnd();
                    break;
                case SLIDING:
                    switch (query.getTime().getDurationTimeUnit()) {
                        case HOUR:
                            endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusHours(
                                           query.getTime().getDuration()).getMillis());
                            break;
                        case DAY:
                            endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusDays(
                                           query.getTime().getDuration()).getMillis());
                            break;
                        case WEEK:
                            endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusWeeks(
                                           query.getTime().getDuration()).getMillis());
                            break;
                        case MONTH:
                            endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusMonths(
                                           query.getTime().getDuration()).getMillis());
                            break;
                        case YEAR:
                            endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusYears(
                                           query.getTime().getDuration()).getMillis());
                            break;
                        default:
                            return;
                    }

                    // Invert start/end dates if needed e.g. a negative interval
                    // is selected for a sliding time window
                    if (endDateTime < startDateTime) {
                        long temp = startDateTime;
                        startDateTime = endDateTime;
                        endDateTime = temp;
                    }
                    break;
                default:
                    return;
            }

            // Construct expanded query
            expandedQuery.setStartDateTime(startDateTime);
            expandedQuery.setEndDateTime(endDateTime);
            expandedQuery.setGranularity(query.getTime().getGranularity());

            response.setMeters(meterAggregateDataRepository.query(expandedQuery));
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    private void executeWithoutPreAggregation(DataQuery query, DataQueryResponse response) {
        try {
            // Create new query
            ExpandedDataQuery expandedQuery = new ExpandedDataQuery(query.getTimezone());

            // Separate spatial filters from simple constraints
            List<ConstraintSpatialFilter> spatialConstraints = new ArrayList<ConstraintSpatialFilter>();
            List<SpatialFilter> spatialFilters = new ArrayList<SpatialFilter>();

            if (query.getSpatial() != null) {
                for (SpatialFilter spatialFilter : query.getSpatial()) {
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
            List<LabeledGeometry> areas = new ArrayList<LabeledGeometry>();

            for (SpatialFilter spatialFilter : spatialFilters) {
                switch (spatialFilter.getType()) {
                    case AREA:
                        AreaSpatialFilter areaSpatialQuery = (AreaSpatialFilter) spatialFilter;

                        for (UUID key : areaSpatialQuery.getAreas()) {
                            AreaGroupMemberEntity areaEntity = spatialRepository.getAreaByKey(key);

                            if (filterArea(spatialConstraints, areaEntity.getGeometry())) {
                                areas.add(new LabeledGeometry(areaEntity));
                            }
                        }

                        break;
                    case GROUP:
                        GroupSpatialFilter groupSpatialFilter = (GroupSpatialFilter) spatialFilter;

                        for (AreaGroupMemberEntity areaEntity : spatialRepository.getAreasByAreaGroupKey(groupSpatialFilter.getGroup())) {
                            if (filterArea(spatialConstraints, areaEntity.getGeometry())) {
                                areas.add(new LabeledGeometry(areaEntity));
                            }
                        }
                        break;
                    default:
                        // Ignore
                        break;
                }
            }

            // Helper store for caching user location
            Map<UUID, Geometry> userLocations = new HashMap<UUID, Geometry>();

            // Step 1: Generate groups based on the population
            ArrayList<ExpandedPopulationFilter> populationGroups = new ArrayList<ExpandedPopulationFilter>();

            if ((query.getPopulation() != null) && (!query.getPopulation().isEmpty())) {
                MessageDigest md = MessageDigest.getInstance("MD5");

                for (int p = 0; p < query.getPopulation().size(); p++) {
                    PopulationFilter filter = query.getPopulation().get(p);

                    List<UUID> filterUsers = null;

                    switch (filter.getType()) {
                        case USER:
                            filterUsers = ((UserPopulationFilter) filter).getUsers();
                            if (filterUsers != null) {
                                for (UUID userKey : filterUsers) {
                                    authorize(query.getExecutor(), EnumPopulationFilterType.USER, userKey);
                                }
                            }
                            break;
                        case GROUP:
                            UUID groupKey = ((GroupPopulationFilter) filter).getGroup();

                            authorize(query.getExecutor(), EnumPopulationFilterType.GROUP, groupKey);

                            filterUsers = groupRepository.getGroupMemberKeys(groupKey);
                            break;
                        case CLUSTER:
                            ClusterPopulationFilter clusterFilter = (ClusterPopulationFilter) filter;

                            List<eu.daiad.common.model.group.Group> groups = null;

                            if (clusterFilter.getCluster() != null) {
                                groups = groupRepository.getClusterSegmentsByKey(clusterFilter.getCluster());
                            } else if ((clusterFilter.getClusterType() != null) && (!clusterFilter.getClusterType().equals(EnumClusterType.UNDEFINED))) {
                                groups = groupRepository.getClusterSegmentsByType(clusterFilter.getClusterType());
                            } else if (!StringUtils.isBlank(clusterFilter.getName())) {
                                groups = groupRepository.getClusterSegmentsByName(clusterFilter.getName());
                            }

                            for (eu.daiad.common.model.group.Group group : groups) {
                                if (clusterFilter.getRanking() == null) {
                                    query.getPopulation().add(new GroupPopulationFilter(group.getName(), group.getKey()));
                                } else {
                                    query.getPopulation().add(new GroupPopulationFilter(group.getName(), group.getKey(), clusterFilter.getRanking()));
                                }
                            }
                            continue;
                        case UTILITY:
                            UUID utilityKey = ((UtilityPopulationFilter) filter).getUtility();

                            authorize(query.getExecutor(), EnumPopulationFilterType.UTILITY, utilityKey);

                            filterUsers = groupRepository.getUtilityByKeyMemberKeys(utilityKey);
                            break;
                        default:
                            // Ignore
                    }

                    // Construct expanded spatial and population filter
                    ExpandedPopulationFilter expandedPopulationFilter = new ExpandedPopulationFilter(filter, filterUsers.size());

                    if (filterUsers.size() > 0) {
                        for (UUID userKey : filterUsers) {
                            AuthenticatedUser user = userRepository.getUserByKey(userKey);
                            if (user == null) {
                                throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", userKey);
                            }

                            // Decide if the user must be included in the group
                            boolean includeUser = true;
                            WaterMeterDevice userMeter = null;

                            // Fetch meter only if it is needed
                            if ((query.getSource() == EnumMeasurementDataSource.BOTH) || (query.getSource() == EnumMeasurementDataSource.METER)) {
                                userMeter = getUserWaterMeter(userKey);
                                if (userMeter == null) {
                                    includeUser = false;
                                }
                            }

                            // Decide if user should be included in the query
                            if (includeUser) {
                                expandedPopulationFilter.getUserKeys().add(userKey);
                                expandedPopulationFilter.getLabels().add(user.getUsername());
                                expandedPopulationFilter.getUserKeyHashes().add(md.digest(userKey.toString().getBytes("UTF-8")));
                                if (userMeter != null) {
                                    expandedPopulationFilter.getSerialHashes().add(md.digest(userMeter.getSerial().getBytes("UTF-8")));
                                } else {
                                    expandedPopulationFilter.getSerialHashes().add(null);
                                }
                            }
                        }
                    }

                    // Add group only if it has at least one user
                    if (!expandedPopulationFilter.getUserKeys().isEmpty()) {
                        populationGroups.add(expandedPopulationFilter);
                    }
                }
            }

            // Step 2: Split population groups depending on the areas
            if (areas.isEmpty()) {
                expandedQuery.getGroups().addAll(populationGroups);
            } else {
                long areaCounter = 0;

                for (LabeledGeometry area : areas) {
                    response.getAreas().put(++areaCounter, area);

                    for (ExpandedPopulationFilter population : populationGroups) {
                        ExpandedPopulationFilter areaPopulation = new ExpandedPopulationFilter(population, areaCounter, area.getKey());

                        for (int i = 0, count = population.getUserKeys().size(); i < count; i++) {
                            if (area.contains(getUserLocation(userLocations, population.getUserKeys().get(i)))) {
                                // Copy fields
                                areaPopulation.getUserKeys().add(population.getUserKeys().get(i));
                                areaPopulation.getLabels().add(population.getLabels().get(i));
                                areaPopulation.getUserKeyHashes().add(population.getUserKeyHashes().get(i));
                                areaPopulation.getSerialHashes().add(population.getSerialHashes().get(i));
                            }
                        }

                        if (!areaPopulation.getUserKeys().isEmpty()) {
                            expandedQuery.getGroups().add(areaPopulation);
                        }
                    }
                }
            }

            // Compute time constraints
            DateTime dateFrom = new DateTime(query.getTime().getStart(), DateTimeZone.forID(query.getTimezone()));
            DateTime dateTo;

            switch (query.getTime().getType()) {
                case ABSOLUTE:
                    dateTo = new DateTime(query.getTime().getEnd(), DateTimeZone.forID(query.getTimezone()));
                    break;
                case SLIDING:
                    switch (query.getTime().getDurationTimeUnit()) {
                        case HOUR:
                            dateTo = dateFrom.plusHours(query.getTime().getDuration());
                            break;
                        case DAY:
                            dateTo = dateFrom.plusDays(query.getTime().getDuration());
                            break;
                        case WEEK:
                            dateTo = dateFrom.plusWeeks(query.getTime().getDuration());
                            break;
                        case MONTH:
                            dateTo = dateFrom.plusMonths(query.getTime().getDuration());
                            break;
                        case YEAR:
                            dateTo = dateFrom.plusYears(query.getTime().getDuration());
                            break;
                        default:
                            return;
                    }

                    // Invert start/end dates if needed e.g. a negative interval
                    // is selected for a sliding time window
                    if (dateTo.isBefore(dateFrom)) {
                        DateTime temp = dateFrom;
                        dateFrom = dateTo;
                        dateTo = temp;
                    }
                    break;
                default:
                    return;
            }

            // Construct expanded query
            expandedQuery.setStartDateTime(dateFrom.getMillis());
            expandedQuery.setEndDateTime(dateTo.getMillis());
            expandedQuery.setGranularity(query.getTime().getGranularity());

            switch (query.getSource()) {
                case BOTH:
                    response.setDevices(amphiroIndexOrderedRepository.query(expandedQuery));
                    response.setMeters(meterDataRepository.query(expandedQuery));
                    break;
                case AMPHIRO: case DEVICE:
                    response.setDevices(amphiroIndexOrderedRepository.query(expandedQuery));
                    break;
                case METER:
                    response.setMeters(meterDataRepository.query(expandedQuery));
                    break;
                case NONE:
                    // Ignore
                    break;
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    /**
     * Executes a generic query for smart water meter forecasting data.
     *
     * @param query the query to execute.
     * @return a collection of {@link GroupDataSeries}.
     */
    @Override
    public ForecastQueryResponse execute(ForecastQuery query) {
        long startTime = System.currentTimeMillis();

        // Set executor
        setExecutor(query);

        // Set date time zone
        ensureDateTimeZone(query);

        // Initialize response
        ForecastQueryResponse response = new ForecastQueryResponse(query.getTimezone());

        // Validate query
        this.validate(query, response);

        if (response.getSuccess()) {
            if(query.isUsingPreAggregation()){
                executeWithPreAggregation(query, response);
            } else {
                executeWithoutPreAggregation(query, response);
            }
        }

        long stopTime = System.currentTimeMillis();
        response.getExecution().setDuration(stopTime - startTime);

        return response;
    }

    private void executeWithPreAggregation(ForecastQuery query, ForecastQueryResponse response) {
        try {
            // Create new query
            ExpandedDataQuery expandedQuery = new ExpandedDataQuery(query.getTimezone());

            // Separate spatial filters from simple constraints
            List<ConstraintSpatialFilter> spatialConstraints = new ArrayList<ConstraintSpatialFilter>();
            List<SpatialFilter> spatialFilters = new ArrayList<SpatialFilter>();

            if (query.getSpatial() != null) {
                for (SpatialFilter spatialFilter : query.getSpatial()) {
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
            List<LabeledGeometry> areas = new ArrayList<LabeledGeometry>();

            for (SpatialFilter spatialFilter : spatialFilters) {
                switch (spatialFilter.getType()) {
                    case AREA:
                        AreaSpatialFilter areaSpatialQuery = (AreaSpatialFilter) spatialFilter;

                        for (UUID key : areaSpatialQuery.getAreas()) {
                            AreaGroupMemberEntity areaEntity = spatialRepository.getAreaByKey(key);

                            if (filterArea(spatialConstraints, areaEntity.getGeometry())) {
                                areas.add(new LabeledGeometry(areaEntity));
                            }
                        }

                        break;
                    case GROUP:
                        GroupSpatialFilter groupSpatialFilter = (GroupSpatialFilter) spatialFilter;

                        for (AreaGroupMemberEntity areaEntity : spatialRepository.getAreasByAreaGroupKey(groupSpatialFilter.getGroup())) {
                            if (filterArea(spatialConstraints, areaEntity.getGeometry())) {
                                areas.add(new LabeledGeometry(areaEntity));
                            }
                        }
                        break;
                    default:
                        // Ignore
                        break;
                }
            }

            // Helper store for caching user location
            Map<UUID, Geometry> userLocations = new HashMap<UUID, Geometry>();

            // Step 1: Generate groups based on the population
            ArrayList<ExpandedPopulationFilter> populationGroups = new ArrayList<ExpandedPopulationFilter>();

            if ((query.getPopulation() != null) && (!query.getPopulation().isEmpty())) {
                MessageDigest md = MessageDigest.getInstance("MD5");

                for (int p = 0; p < query.getPopulation().size(); p++) {
                    PopulationFilter filter = query.getPopulation().get(p);

                    List<UUID> filterUsers = null;

                    switch (filter.getType()) {
                        case USER:
                            filterUsers = ((UserPopulationFilter) filter).getUsers();
                            if (filterUsers != null) {
                                for (UUID userKey : filterUsers) {
                                    authorize(query.getExecutor(), EnumPopulationFilterType.USER, userKey);
                                }
                            }
                            break;
                        case GROUP:
                            UUID groupKey = ((GroupPopulationFilter) filter).getGroup();

                            authorize(query.getExecutor(), EnumPopulationFilterType.GROUP, groupKey);

                            filterUsers = groupRepository.getGroupMemberKeys(groupKey);
                            break;
                        case CLUSTER:
                            ClusterPopulationFilter clusterFilter = (ClusterPopulationFilter) filter;

                            List<Group> groups = null;

                            if (clusterFilter.getCluster() != null) {
                                groups = groupRepository.getClusterSegmentsByKey(clusterFilter.getCluster());
                            } else if ((clusterFilter.getClusterType() != null) && (!clusterFilter.getClusterType().equals(EnumClusterType.UNDEFINED))) {
                                groups = groupRepository.getClusterSegmentsByType(clusterFilter.getClusterType());
                            } else if (!StringUtils.isBlank(clusterFilter.getName())) {
                                groups = groupRepository.getClusterSegmentsByName(clusterFilter.getName());
                            }

                            for (Group group : groups) {
                                if (clusterFilter.getRanking() == null) {
                                    query.getPopulation().add(new GroupPopulationFilter(group.getName(), group.getKey()));
                                } else {
                                    query.getPopulation().add(new GroupPopulationFilter(group.getName(), group.getKey(), clusterFilter.getRanking()));
                                }
                            }
                            continue;
                        case UTILITY:
                            UUID utilityKey = ((UtilityPopulationFilter) filter).getUtility();

                            authorize(query.getExecutor(), EnumPopulationFilterType.UTILITY, utilityKey);

                            filterUsers = groupRepository.getUtilityByKeyMemberKeys(utilityKey);
                            break;
                        default:
                            throw new IllegalArgumentException(String.format("Filter of type [%s] is not supported.", filter.getType()));
                    }

                    // Construct expanded spatial and population filter
                    ExpandedPopulationFilter expandedPopulationFilter = new ExpandedPopulationFilter(filter, filterUsers.size());

                    if ((filter.getType() == EnumPopulationFilterType.USER) && (filterUsers.size() > 0)) {
                        for (UUID userKey : filterUsers) {
                            AuthenticatedUser user = userRepository.getUserByKey(userKey);
                            if (user == null) {
                                throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", userKey);
                            }


                            // Decide if the user must be included in the group
                            boolean includeUser = true;

                            WaterMeterDevice userMeter = getUserWaterMeter(userKey);
                            if (userMeter == null) {
                                includeUser = false;
                            }

                            // Decide if user should be added to the final
                            // result
                            if (includeUser) {
                                expandedPopulationFilter.getUserKeys().add(userKey);
                                expandedPopulationFilter.getLabels().add(user.getUsername());
                                expandedPopulationFilter.getUserKeyHashes().add(md.digest(userKey.toString().getBytes("UTF-8")));
                                expandedPopulationFilter.getSerialHashes().add(md.digest(userMeter.getSerial().getBytes("UTF-8")));
                            }

                        }
                    }

                    // Include population groups that either are not of type
                    // USER or have at least one user
                    if ((filter.getType() != EnumPopulationFilterType.USER) ||
                        (!expandedPopulationFilter.getUserKeys().isEmpty())) {
                        populationGroups.add(expandedPopulationFilter);
                    }
                }
            }

            // Step 2: Split population groups depending on the areas
            if (areas.isEmpty()) {
                expandedQuery.getGroups().addAll(populationGroups);
            } else {
                long areaCounter = 0;

                for (LabeledGeometry area : areas) {
                    response.getAreas().put(++areaCounter, area);

                    for (ExpandedPopulationFilter population : populationGroups) {
                        ExpandedPopulationFilter areaPopulation = new ExpandedPopulationFilter(population, areaCounter, area.getKey());

                        for (int i = 0, count = population.getUserKeys().size(); i < count; i++) {
                            if (area.contains(getUserLocation(userLocations, population.getUserKeys().get(i)))) {
                                // Copy fields
                                areaPopulation.getUserKeys().add(population.getUserKeys().get(i));
                                areaPopulation.getLabels().add(population.getLabels().get(i));
                                areaPopulation.getUserKeyHashes().add(population.getUserKeyHashes().get(i));
                                areaPopulation.getSerialHashes().add(population.getSerialHashes().get(i));
                            }
                        }

                        // Include population groups that either are not of type
                        // USER or have at least one user
                        if ((areaPopulation.getType() != EnumPopulationFilterType.USER) ||
                            (!areaPopulation.getUserKeys().isEmpty())) {
                            expandedQuery.getGroups().add(areaPopulation);
                        }
                    }
                }
            }

            // Compute time constraints
            long startDateTime, endDateTime;

            startDateTime = query.getTime().getStart();

            switch (query.getTime().getType()) {
                case ABSOLUTE:
                    endDateTime = query.getTime().getEnd();
                    break;
                case SLIDING:
                    switch (query.getTime().getDurationTimeUnit()) {
                        case HOUR:
                            endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusHours(
                                           query.getTime().getDuration()).getMillis());
                            break;
                        case DAY:
                            endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusDays(
                                           query.getTime().getDuration()).getMillis());
                            break;
                        case WEEK:
                            endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusWeeks(
                                           query.getTime().getDuration()).getMillis());
                            break;
                        case MONTH:
                            endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusMonths(
                                           query.getTime().getDuration()).getMillis());
                            break;
                        case YEAR:
                            endDateTime = (new DateTime(startDateTime, DateTimeZone.UTC).plusYears(
                                           query.getTime().getDuration()).getMillis());
                            break;
                        default:
                            return;
                    }

                    // Invert start/end dates if needed e.g. a negative interval
                    // is selected for a sliding time window
                    if (endDateTime < startDateTime) {
                        long temp = startDateTime;
                        startDateTime = endDateTime;
                        endDateTime = temp;
                    }
                    break;
                default:
                    return;
            }

            // Construct expanded query
            expandedQuery.setStartDateTime(startDateTime);
            expandedQuery.setEndDateTime(endDateTime);
            expandedQuery.setGranularity(query.getTime().getGranularity());

            response.setMeters(meterForecastingAggregateDataRepository.forecast(expandedQuery));
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    private void executeWithoutPreAggregation(ForecastQuery query, ForecastQueryResponse response) {
        try {
            // Create new query
            ExpandedDataQuery expandedQuery = new ExpandedDataQuery(query.getTimezone());

            // Separate spatial filters from simple constraints
            List<ConstraintSpatialFilter> spatialConstraints = new ArrayList<ConstraintSpatialFilter>();
            List<SpatialFilter> spatialFilters = new ArrayList<SpatialFilter>();

            if (query.getSpatial() != null) {
                for (SpatialFilter spatialFilter : query.getSpatial()) {
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
            List<LabeledGeometry> areas = new ArrayList<LabeledGeometry>();

            for (SpatialFilter spatialFilter : spatialFilters) {
                switch (spatialFilter.getType()) {
                    case AREA:
                        AreaSpatialFilter areaSpatialQuery = (AreaSpatialFilter) spatialFilter;

                        for (UUID key : areaSpatialQuery.getAreas()) {
                            AreaGroupMemberEntity areaEntity = spatialRepository.getAreaByKey(key);

                            if (filterArea(spatialConstraints, areaEntity.getGeometry())) {
                                areas.add(new LabeledGeometry(areaEntity));
                            }
                        }

                        break;
                    case GROUP:
                        GroupSpatialFilter groupSpatialFilter = (GroupSpatialFilter) spatialFilter;

                        for (AreaGroupMemberEntity areaEntity : spatialRepository.getAreasByAreaGroupKey(groupSpatialFilter.getGroup())) {
                            if (filterArea(spatialConstraints, areaEntity.getGeometry())) {
                                areas.add(new LabeledGeometry(areaEntity));
                            }
                        }
                        break;
                    default:
                        // Ignore
                        break;
                }
            }

            // Helper store for caching user location
            Map<UUID, Geometry> userLocations = new HashMap<UUID, Geometry>();

            // Step 1: Generate groups based on the population
            ArrayList<ExpandedPopulationFilter> populationGroups = new ArrayList<ExpandedPopulationFilter>();

            if ((query.getPopulation() != null) && (!query.getPopulation().isEmpty())) {
                MessageDigest md = MessageDigest.getInstance("MD5");

                for (int p = 0; p < query.getPopulation().size(); p++) {
                    PopulationFilter filter = query.getPopulation().get(p);

                    List<UUID> filterUsers = null;

                    switch (filter.getType()) {
                        case USER:
                            filterUsers = ((UserPopulationFilter) filter).getUsers();
                            if (filterUsers != null) {
                                for (UUID userKey : filterUsers) {
                                    authorize(query.getExecutor(), EnumPopulationFilterType.USER, userKey);
                                }
                            }
                            break;
                        case GROUP:
                            UUID groupKey = ((GroupPopulationFilter) filter).getGroup();

                            authorize(query.getExecutor(), EnumPopulationFilterType.GROUP, groupKey);

                            filterUsers = groupRepository.getGroupMemberKeys(groupKey);
                            break;
                        case CLUSTER:
                            ClusterPopulationFilter clusterFilter = (ClusterPopulationFilter) filter;

                            List<eu.daiad.common.model.group.Group> groups = null;

                            if (clusterFilter.getCluster() != null) {
                                groups = groupRepository.getClusterSegmentsByKey(clusterFilter.getCluster());
                            } else if ((clusterFilter.getClusterType() != null) && (!clusterFilter.getClusterType().equals(EnumClusterType.UNDEFINED))) {
                                groups = groupRepository.getClusterSegmentsByType(clusterFilter.getClusterType());
                            } else if (!StringUtils.isBlank(clusterFilter.getName())) {
                                groups = groupRepository.getClusterSegmentsByName(clusterFilter.getName());
                            }

                            for (eu.daiad.common.model.group.Group group : groups) {
                                if (clusterFilter.getRanking() == null) {
                                    query.getPopulation().add(new GroupPopulationFilter(group.getName(), group.getKey()));
                                } else {
                                    query.getPopulation().add(new GroupPopulationFilter(group.getName(), group.getKey(), clusterFilter.getRanking()));
                                }
                            }
                            continue;
                        case UTILITY:
                            UUID utilityKey = ((UtilityPopulationFilter) filter).getUtility();

                            authorize(query.getExecutor(), EnumPopulationFilterType.UTILITY, utilityKey);

                            filterUsers = groupRepository.getUtilityByKeyMemberKeys(utilityKey);
                            break;
                        default:
                            // Ignore
                    }

                    // Construct expanded spatial and population filter
                    ExpandedPopulationFilter expandedPopulationFilter = new ExpandedPopulationFilter(filter, filterUsers.size());

                    if (filterUsers.size() > 0) {
                        for (UUID userKey : filterUsers) {
                            AuthenticatedUser user = userRepository.getUserByKey(userKey);
                            if (user == null) {
                                throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", userKey);
                            }

                            // Decide if the user must be included in the group
                            boolean includeUser = true;

                            WaterMeterDevice userMeter = getUserWaterMeter(userKey);
                            if (userMeter == null) {
                                includeUser = false;
                            }

                            // Decide if user should be added to the final
                            // result
                            if (includeUser) {
                                expandedPopulationFilter.getUserKeys().add(userKey);
                                expandedPopulationFilter.getLabels().add(user.getUsername());
                                expandedPopulationFilter.getUserKeyHashes().add(md.digest(userKey.toString().getBytes("UTF-8")));
                                if (userMeter != null) {
                                    expandedPopulationFilter.getSerialHashes().add(md.digest(userMeter.getSerial().getBytes("UTF-8")));
                                } else {
                                    expandedPopulationFilter.getSerialHashes().add(null);
                                }
                            }
                        }
                    }

                    // Add group only if it has at least one user
                    if (!expandedPopulationFilter.getUserKeys().isEmpty()) {
                        populationGroups.add(expandedPopulationFilter);
                    }
                }
            }

            // Step 2: Split population groups depending on the areas
            if (areas.isEmpty()) {
                expandedQuery.getGroups().addAll(populationGroups);
            } else {
                long areaCounter = 0;

                for (LabeledGeometry area : areas) {
                    response.getAreas().put(++areaCounter, area);

                    for (ExpandedPopulationFilter population : populationGroups) {
                        ExpandedPopulationFilter areaPopulation = new ExpandedPopulationFilter(population, areaCounter, area.getKey());

                        for (int i = 0, count = population.getUserKeys().size(); i < count; i++) {
                            if (area.contains(getUserLocation(userLocations, population.getUserKeys().get(i)))) {
                                // Copy fields
                                areaPopulation.getUserKeys().add(population.getUserKeys().get(i));
                                areaPopulation.getLabels().add(population.getLabels().get(i));
                                areaPopulation.getUserKeyHashes().add(population.getUserKeyHashes().get(i));
                                areaPopulation.getSerialHashes().add(population.getSerialHashes().get(i));
                            }
                        }

                        if (!areaPopulation.getUserKeys().isEmpty()) {
                            expandedQuery.getGroups().add(areaPopulation);
                        }
                    }
                }
            }

            // Compute time constraints
            DateTime dateFrom = new DateTime(query.getTime().getStart(), DateTimeZone.forID(query.getTimezone()));
            DateTime dateTo;

            switch (query.getTime().getType()) {
                case ABSOLUTE:
                    dateTo = new DateTime(query.getTime().getEnd(), DateTimeZone.forID(query.getTimezone()));
                    break;
                case SLIDING:
                    switch (query.getTime().getDurationTimeUnit()) {
                        case HOUR:
                            dateTo = dateFrom.plusHours(query.getTime().getDuration());
                            break;
                        case DAY:
                            dateTo = dateFrom.plusDays(query.getTime().getDuration());
                            break;
                        case WEEK:
                            dateTo = dateFrom.plusWeeks(query.getTime().getDuration());
                            break;
                        case MONTH:
                            dateTo = dateFrom.plusMonths(query.getTime().getDuration());
                            break;
                        case YEAR:
                            dateTo = dateFrom.plusYears(query.getTime().getDuration());
                            break;
                        default:
                            return;
                    }

                    // Invert start/end dates if needed e.g. a negative interval
                    // is selected for a sliding time window
                    if (dateTo.isBefore(dateFrom)) {
                        DateTime temp = dateFrom;
                        dateFrom = dateTo;
                        dateTo = temp;
                    }
                    break;
                default:
                    return;
            }

            // Construct expanded query
            expandedQuery.setStartDateTime(dateFrom.getMillis());
            expandedQuery.setEndDateTime(dateTo.getMillis());
            expandedQuery.setGranularity(query.getTime().getGranularity());

            response.setMeters(meterForecastingDataRepository.forecast(expandedQuery));
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    private Geometry getUserLocation(Map<UUID, Geometry> userLocations, UUID userKey) {
        if (userLocations.containsKey(userKey)) {
            return userLocations.get(userKey);
        }

        Geometry location = spatialRepository.getUserLocationByUserKey(userKey);

        userLocations.put(userKey, location);

        return location;
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

    private double distance(Geometry g1, Geometry g2) {
        double distance = g1.distance(g2);

        return distance * (Math.PI / 180) * 6378137;
    }

    @Override
    public void storeQuery(NamedDataQuery query, UUID key) {
        AccountEntity account = userRepository.getAccountByKey(key);
        favouriteRepository.insertFavouriteQuery(query, account);

    }

    @Override
    public void updateStoredQuery(NamedDataQuery query, UUID key) {
        AccountEntity account = userRepository.getAccountByKey(key);
        favouriteRepository.updateFavouriteQuery(query, account);

    }

    @Override
    public void deleteStoredQuery(NamedDataQuery query, UUID key) {
        AccountEntity account = userRepository.getAccountByKey(key);
        favouriteRepository.deleteFavouriteQuery(query, account);

    }

    @Override
    public void storeQuery(NamedDataQuery query, String username) {
        AccountEntity account = userRepository.getAccountByUsername(username);
        favouriteRepository.insertFavouriteQuery(query, account);

    }

    @Override
    public void pinStoredQuery(long id, UUID key) {
        AccountEntity account = userRepository.getAccountByKey(key);
        favouriteRepository.pinFavouriteQuery(id, account);

    }

    @Override
    public void unpinStoredQuery(long id, UUID key) {
        AccountEntity account = userRepository.getAccountByKey(key);
        favouriteRepository.unpinFavouriteQuery(id, account);

    }

    @Override
    public List<NamedDataQuery> getQueriesForOwner(int accountId)
            throws JsonMappingException, JsonParseException, IOException{
        List<NamedDataQuery> namedQueries = favouriteRepository.getFavouriteQueriesForOwner(accountId);
        return namedQueries;
    }

    @Override
    public List<NamedDataQuery> getAllQueries() {
        List<NamedDataQuery> namedQueries = favouriteRepository.getAllFavouriteQueries();
        return namedQueries;
    }

    /**
     * Sets the executor for the given query.
     *
     * @param query an instance of {@link AbstractDataQuery}.
     */
    private void setExecutor(AbstractDataQuery query) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if ((authentication != null) && (authentication.getPrincipal() instanceof AuthenticatedUser)) {
            query.setExecutor((AuthenticatedUser) authentication.getPrincipal());
        }
    }

    /**
     * Sets the date time zone for the query if not already set by the caller.
     *
     * @param query an instance of {@link AbstractDataQuery}.
     */
    private void ensureDateTimeZone(AbstractDataQuery query) {
        // If time zone is not set, use the current user's time zone
        if (StringUtils.isBlank(query.getTimezone())) {
            if (query.getExecutor() == null) {
                // If there is no authenticated user, set default UTC time zone
                query.setTimezone("UTC");
            } else {
                query.setTimezone(query.getExecutor().getTimezone());
            }
        }
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
     * Checks access permissions of a user for a population filter.
     *
     * @param executor the user who executes the query.
     * @param type the type of population filter.
     * @param key the key of the filter.
     * @throws IllegalArgumentException if type is not supported.
     * @throws ApplicationException if authorization check fails.
     */
    private void authorize(AuthenticatedUser executor, EnumPopulationFilterType type, UUID key) throws IllegalArgumentException, ApplicationException {
        if (executor == null) {
            return;
        }

        switch (type) {
            case USER:
                // User is the executor
                if (executor.getKey().equals(key)) {
                    return;
                }
                // Executor is a system administrator
                if (executor.hasRole(EnumRole.ROLE_SYSTEM_ADMIN)) {
                    return;
                }
                // Executor is a utility administrator for the user's utility
                if (executor.hasRole(EnumRole.ROLE_UTILITY_ADMIN)) {
                    if (executor.getUtilities().contains(userRepository.getUserByKey(key).getUtilityId())) {
                        return;
                    }
                }
                // Executor and user participate in a common DAIAD@commons group
                if (commonsRepository.shareCommonsMembership(executor.getKey(), key)) {
                    return;
                }
                break;
            case GROUP:
                Group group = groupRepository.getByKey(key, false);

                switch (group.getType()) {
                    case SEGMENT:
                        // Executor is a system administrator
                        if (executor.hasRole(EnumRole.ROLE_SYSTEM_ADMIN)) {
                            return;
                        }
                        // Executor is a utility administrator for the user's
                        // utility
                        if (executor.hasRole(EnumRole.ROLE_UTILITY_ADMIN)) {
                            if (executor.getUtilities().contains(utilityRepository.getUtilityByKey(group.getUtilityKey()).getId())) {
                                return;
                            }
                        }
                        break;
                    case SET:
                        // The executor is the owner of the set
                        if (executor.getKey().equals(((Set) group).getOwnerKey())) {
                            return;
                        }
                        // Executor is a system administrator
                        if (executor.hasRole(EnumRole.ROLE_SYSTEM_ADMIN)) {
                            return;
                        }
                        // Executor is a utility administrator for the user's
                        // utility
                        if (executor.hasRole(EnumRole.ROLE_UTILITY_ADMIN)) {
                            if (executor.getUtilities().contains(utilityRepository.getUtilityByKey(group.getUtilityKey()).getId())) {
                                return;
                            }
                        }
                        break;
                    case COMMONS:
                         // Executor is a system administrator
                        if (executor.hasRole(EnumRole.ROLE_SYSTEM_ADMIN)) {
                            return;
                        }
                        // Executor is a utility administrator for the user's
                        // utility
                        if (executor.hasRole(EnumRole.ROLE_UTILITY_ADMIN)) {
                            if (executor.getUtilities().contains(utilityRepository.getUtilityByKey(group.getUtilityKey()).getId())) {
                                return;
                            }
                        }
                        // Check if the executor is a member of the DAIAD@commons group
                        if(commonsRepository.getAccountCommons(executor.getKey()).contains(key)) {
                            return;
                        }
                        break;
                    default:
                        break;
                }
                break;
            case UTILITY:
                // Executor is a system administrator
                if (executor.hasRole(EnumRole.ROLE_SYSTEM_ADMIN)) {
                    return;
                }
                // Executor is a utility administrator for the user's
                // utility
                if (executor.hasRole(EnumRole.ROLE_UTILITY_ADMIN)) {
                    if (executor.getUtilities().contains(utilityRepository.getUtilityByKey(key).getId())) {
                        return;
                    }
                }
                // Allow user to access utility data
                if(executor.getUtilityKey().equals(key)) {
                    return;
                }
                break;
            default:
                throw new IllegalArgumentException(String.format("Filter of type [%s] is not supported.", type));
        }

        throw createApplicationException(SharedErrorCode.AUTHORIZATION);
    }
}
