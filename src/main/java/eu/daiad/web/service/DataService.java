package eu.daiad.web.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ibm.icu.text.MessageFormat;
import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AreaGroupMemberEntity;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.Error;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.QueryErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.query.AreaSpatialFilter;
import eu.daiad.web.model.query.ClusterPopulationFilter;
import eu.daiad.web.model.query.ConstraintSpatialFilter;
import eu.daiad.web.model.query.CustomSpatialFilter;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.DataQueryResponse;
import eu.daiad.web.model.query.EnumClusterType;
import eu.daiad.web.model.query.EnumDataField;
import eu.daiad.web.model.query.EnumMeasurementDataSource;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.EnumRankingType;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.ExpandedPopulationFilter;
import eu.daiad.web.model.query.ForecastQuery;
import eu.daiad.web.model.query.ForecastQueryResponse;
import eu.daiad.web.model.query.GroupPopulationFilter;
import eu.daiad.web.model.query.GroupSpatialFilter;
import eu.daiad.web.model.query.NamedDataQuery;
import eu.daiad.web.model.query.PopulationFilter;
import eu.daiad.web.model.query.SpatialFilter;
import eu.daiad.web.model.query.UserPopulationFilter;
import eu.daiad.web.model.query.UtilityPopulationFilter;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.spatial.LabeledGeometry;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IFavouriteRepository;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.ISpatialRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IWaterMeterForecastRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

@Service
public class DataService extends BaseService implements IDataService {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IGroupRepository groupRepository;

    @Autowired
    private IDeviceRepository deviceRepository;

    @Autowired
    private ISpatialRepository spatialRepository;

    @Autowired
    IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    @Autowired
    IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

    @Autowired
    IWaterMeterForecastRepository waterMeterForecastRepository;

    @Autowired
    IFavouriteRepository favouriteRepository;

    protected String getMessage(ErrorCode error) {
        return messageSource.getMessage(error.getMessageKey(), null, error.getMessageKey(), null);
    }

    private String getMessage(ErrorCode error, Map<String, Object> properties) {
        String message = messageSource.getMessage(error.getMessageKey(), null, error.getMessageKey(), null);

        MessageFormat msgFmt = new MessageFormat(message);

        return msgFmt.format(properties);
    }

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
                    if ((query.getSource().equals(EnumMeasurementDataSource.METER))
                                    || (query.getSource().equals(EnumMeasurementDataSource.BOTH))) {
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

    @Override
    public DataQueryResponse execute(DataQuery query) {
        try {
            // Get authenticated user if any exists
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser authenticatedUser = null;

            if ((authentication != null) && (authentication.getPrincipal() instanceof AuthenticatedUser)) {
                authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
            }

            // If time zone is not set, use the current user's time zone
            DateTimeZone timezone = null;

            if (StringUtils.isBlank(query.getTimezone())) {
                if (authenticatedUser != null) {
                    timezone = DateTimeZone.forID(authenticatedUser.getTimezone());
                } else {
                    // If there is no authenticated user, user default UTC time
                    // zone
                    timezone = DateTimeZone.UTC;
                }
            } else {
                timezone = DateTimeZone.forID(query.getTimezone());
            }

            DataQueryResponse response = new DataQueryResponse(timezone);

            // Validate query
            this.validate(query, response);
            if (!response.getSuccess()) {
                return response;
            }

            // Create new query
            ExpandedDataQuery expandedQuery = new ExpandedDataQuery(timezone);

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

            // Spatial groups
            List<LabeledGeometry> areas = new ArrayList<LabeledGeometry>();

            // Expand spatial filters
            for (SpatialFilter spatialFilter : spatialFilters) {
                switch (spatialFilter.getType()) {
                    case CUSTOM:
                        CustomSpatialFilter customSpatialFilter = (CustomSpatialFilter) spatialFilter;
                        for (LabeledGeometry area : customSpatialFilter.getGeometries()) {
                            areas.add(area);
                        }
                        break;
                    case AREA:
                        AreaSpatialFilter areaSpatialQuery = (AreaSpatialFilter) spatialFilter;

                        for (UUID key : areaSpatialQuery.getAreas()) {
                            AreaGroupMemberEntity areaEntity = spatialRepository.getAreaByKey(key);

                            areas.add(new LabeledGeometry(areaEntity.getTitle(), areaEntity.getGeometry()));
                        }

                        break;
                    case GROUP:
                        GroupSpatialFilter groupSpatialFilter = (GroupSpatialFilter) spatialFilter;

                        for (AreaGroupMemberEntity areaEntity : spatialRepository.getAreasByAreaGroupKey(groupSpatialFilter.getGroup())) {
                            areas.add(new LabeledGeometry(areaEntity.getTitle(), areaEntity.getGeometry()));
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
                            break;
                        case GROUP:
                            filterUsers = groupRepository.getGroupMemberKeys(((GroupPopulationFilter) filter).getGroup());
                            break;
                        case CLUSTER:
                            ClusterPopulationFilter clusterFilter = (ClusterPopulationFilter) filter;

                            List<eu.daiad.web.model.group.Group> groups = null;

                            if (clusterFilter.getCluster() != null) {
                                groups = groupRepository.getClusterByKeySegments(clusterFilter.getCluster());
                            } else if ((clusterFilter.getClusterType() != null) && (!clusterFilter.getClusterType().equals(EnumClusterType.UNDEFINED))) {
                                groups = groupRepository.getClusterByTypeSegments(clusterFilter.getClusterType());
                            } else if (!StringUtils.isBlank(clusterFilter.getName())) {
                                groups = groupRepository.getClusterByNameSegments(clusterFilter.getName());
                            }

                            for (eu.daiad.web.model.group.Group group : groups) {
                                if (clusterFilter.getRanking() == null) {
                                    query.getPopulation().add(new GroupPopulationFilter(group.getName(), group.getKey()));
                                } else {
                                    query.getPopulation().add(new GroupPopulationFilter(group.getName(), group.getKey(), clusterFilter.getRanking()));
                                }
                            }
                            continue;
                        case UTILITY:
                            filterUsers = groupRepository.getUtilityByKeyMemberKeys(((UtilityPopulationFilter) filter).getUtility());
                            break;
                        default:
                            // Ignore
                    }

                    // Construct expanded spatial and population filter
                    ExpandedPopulationFilter expandedPopulationFilter;

                    if (filter.getRanking() == null) {
                        expandedPopulationFilter = new ExpandedPopulationFilter(filter.getLabel());
                    } else {
                        expandedPopulationFilter = new ExpandedPopulationFilter(filter.getLabel(), filter.getRanking());
                    }

                    if (filterUsers.size() > 0) {
                        for (UUID userKey : filterUsers) {
                            AuthenticatedUser user = authorizeUser(authenticatedUser, userKey);

                            // Decide if the user must be included in the group
                            boolean includeUser = true;
                            Geometry userLocation = null;
                            WaterMeterDevice userMeter = null;

                            // Fetch meter only if it is needed
                            if ((query.getSource() == EnumMeasurementDataSource.BOTH) || (query.getSource() == EnumMeasurementDataSource.METER)) {
                                userMeter = getUserWaterMeter(userKey);
                                if (userMeter == null) {
                                    includeUser = false;
                                }
                            }

                            // Filter only if not already rejected
                            if (includeUser) {
                                // Fetch location only if it is needed
                                if ((!spatialConstraints.isEmpty()) || (!areas.isEmpty())) {
                                    userLocation = getUserLocation(userLocations, userKey);
                                }

                                for (ConstraintSpatialFilter spatialConstraint : spatialConstraints) {
                                    if (!filterUserWithConstraintSpatialFilter(userLocation, spatialConstraint)) {
                                        includeUser = false;
                                        break;
                                    }
                                }
                            }

                            // Decide if user should be added to the final
                            // result
                            if (includeUser) {
                                expandedPopulationFilter.getUsers().add(userKey);
                                expandedPopulationFilter.getLabels().add(user.getUsername());
                                expandedPopulationFilter.getHashes().add(md.digest(userKey.toString().getBytes("UTF-8")));
                                if (userMeter != null) {
                                    expandedPopulationFilter.getSerials().add(md.digest(userMeter.getSerial().getBytes("UTF-8")));
                                } else {
                                    expandedPopulationFilter.getSerials().add(null);
                                }
                            }

                        }
                    }

                    // Add group only if it has at least one user
                    if (!expandedPopulationFilter.getUsers().isEmpty()) {
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
                        ExpandedPopulationFilter expandedPopulationFilter;

                        if (population.getRanking() == null) {
                            expandedPopulationFilter = new ExpandedPopulationFilter(population.getLabel());
                        } else {
                            expandedPopulationFilter = new ExpandedPopulationFilter(population.getLabel(), population.getRanking());
                        }

                        expandedPopulationFilter.setAreaId(areaCounter);

                        for (int i = 0, count = population.getUsers().size(); i < count; i++) {
                            if (area.contains(getUserLocation(userLocations, population.getUsers().get(i)))) {
                                // Copy fields
                                expandedPopulationFilter.getUsers().add(population.getUsers().get(i));
                                expandedPopulationFilter.getLabels().add(population.getLabels().get(i));
                                expandedPopulationFilter.getHashes().add(population.getHashes().get(i));
                                expandedPopulationFilter.getSerials().add(population.getSerials().get(i));
                            }
                        }

                        if (!expandedPopulationFilter.getUsers().isEmpty()) {
                            expandedQuery.getGroups().add(expandedPopulationFilter);
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
                            return response;
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
                    return response;
            }

            // Set metrics and add any required dependencies
            List<EnumMetric> metrics = new ArrayList<EnumMetric>();

            for (EnumMetric m : query.getMetrics()) {
                metrics.add(m);
            }

            if (metrics.contains(EnumMetric.AVERAGE)) {
                if (!metrics.contains(EnumMetric.COUNT)) {
                    metrics.add(EnumMetric.COUNT);
                }
                if (!metrics.contains(EnumMetric.SUM)) {
                    metrics.add(EnumMetric.SUM);
                }
            }
            if (metrics.contains(EnumMetric.MIN)) {
                if (!metrics.contains(EnumMetric.MAX)) {
                    metrics.add(EnumMetric.MAX);
                }
            }
            if (metrics.contains(EnumMetric.MAX)) {
                if (!metrics.contains(EnumMetric.MIN)) {
                    metrics.add(EnumMetric.MIN);
                }
            }

            // Construct expanded query
            expandedQuery.setStartDateTime(startDateTime);
            expandedQuery.setEndDateTime(endDateTime);
            expandedQuery.setGranularity(query.getTime().getGranularity());
            expandedQuery.setMetrics(metrics);

            switch (query.getSource()) {
                case BOTH:
                    response.setDevices(amphiroIndexOrderedRepository.query(expandedQuery));
                    response.setMeters(waterMeterMeasurementRepository.query(expandedQuery));
                    break;
                case AMPHIRO: case DEVICE:
                    response.setDevices(amphiroIndexOrderedRepository.query(expandedQuery));
                    break;
                case METER:
                    response.setMeters(waterMeterMeasurementRepository.query(expandedQuery));
                    break;
                case NONE:
                    // Ignore
                    break;
            }

            return response;
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    @Override
    public ForecastQueryResponse execute(ForecastQuery query) {
        try {
            // Get authenticated user if any exists
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser authenticatedUser = null;

            if ((authentication != null) && (authentication.getPrincipal() instanceof AuthenticatedUser)) {
                authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
            }

            // If time zone is not set, use the current user's time zone
            DateTimeZone timezone = null;

            if (StringUtils.isBlank(query.getTimezone())) {
                if (authenticatedUser != null) {
                    timezone = DateTimeZone.forID(authenticatedUser.getTimezone());
                } else {
                    // If there is no authenticated user, user default UTC time
                    // zone
                    timezone = DateTimeZone.UTC;
                }
            } else {
                timezone = DateTimeZone.forID(query.getTimezone());
            }

            ForecastQueryResponse response = new ForecastQueryResponse(timezone);

            // Validate query
            this.validate(query, response);
            if (!response.getSuccess()) {
                return response;
            }

            // Create new query
            ExpandedDataQuery expandedQuery = new ExpandedDataQuery(timezone);

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

            // Spatial groups
            List<LabeledGeometry> areas = new ArrayList<LabeledGeometry>();

            // Expand spatial filters
            for (SpatialFilter spatialFilter : spatialFilters) {
                switch (spatialFilter.getType()) {
                    case CUSTOM:
                        CustomSpatialFilter customSpatialFilter = (CustomSpatialFilter) spatialFilter;
                        for (LabeledGeometry area : customSpatialFilter.getGeometries()) {
                            areas.add(area);
                        }
                        break;
                    case AREA:
                        AreaSpatialFilter areaSpatialQuery = (AreaSpatialFilter) spatialFilter;

                        for (UUID key : areaSpatialQuery.getAreas()) {
                            AreaGroupMemberEntity areaEntity = spatialRepository.getAreaByKey(key);

                            areas.add(new LabeledGeometry(areaEntity.getTitle(), areaEntity.getGeometry()));
                        }

                        break;
                    case GROUP:
                        GroupSpatialFilter groupSpatialFilter = (GroupSpatialFilter) spatialFilter;

                        for (AreaGroupMemberEntity areaEntity : spatialRepository
                                        .getAreasByAreaGroupKey(groupSpatialFilter.getGroup())) {
                            areas.add(new LabeledGeometry(areaEntity.getTitle(), areaEntity.getGeometry()));
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
                            break;
                        case GROUP:
                            filterUsers = groupRepository.getGroupMemberKeys(((GroupPopulationFilter) filter).getGroup());
                            break;
                        case CLUSTER:
                            ClusterPopulationFilter clusterFilter = (ClusterPopulationFilter) filter;

                            List<eu.daiad.web.model.group.Group> groups = null;

                            if (clusterFilter.getCluster() != null) {
                                groups = groupRepository.getClusterByKeySegments(clusterFilter.getCluster());
                            } else if ((clusterFilter.getClusterType() != null) && (!clusterFilter.getClusterType().equals(EnumClusterType.UNDEFINED))) {
                                groups = groupRepository.getClusterByTypeSegments(clusterFilter.getClusterType());
                            } else if (!StringUtils.isBlank(clusterFilter.getName())) {
                                groups = groupRepository.getClusterByNameSegments(clusterFilter.getName());
                            }

                            for (eu.daiad.web.model.group.Group group : groups) {
                                if (clusterFilter.getRanking() == null) {
                                    query.getPopulation().add(
                                                    new GroupPopulationFilter(group.getName(), group.getKey()));
                                } else {
                                    query.getPopulation().add(
                                                    new GroupPopulationFilter(group.getName(), group.getKey(),
                                                                    clusterFilter.getRanking()));
                                }
                            }
                            continue;
                        case UTILITY:
                            filterUsers = groupRepository.getUtilityByKeyMemberKeys(((UtilityPopulationFilter) filter).getUtility());
                            break;
                        default:
                            // Ignore
                    }

                    // Construct expanded spatial and population filter
                    ExpandedPopulationFilter expandedPopulationFilter;

                    if (filter.getRanking() == null) {
                        expandedPopulationFilter = new ExpandedPopulationFilter(filter.getLabel());
                    } else {
                        expandedPopulationFilter = new ExpandedPopulationFilter(filter.getLabel(), filter.getRanking());
                    }

                    if (filterUsers.size() > 0) {
                        for (UUID userKey : filterUsers) {
                            AuthenticatedUser user = authorizeUser(authenticatedUser, userKey);

                            // Decide if the user must be included in the group
                            boolean includeUser = true;
                            Geometry userLocation = null;
                            WaterMeterDevice userMeter = null;

                            // Fetch meters
                            userMeter = getUserWaterMeter(userKey);
                            if (userMeter == null) {
                                includeUser = false;
                            }

                            // Filter only if not already rejected
                            if (includeUser) {
                                // Fetch location only if it is needed
                                if ((!spatialConstraints.isEmpty()) || (!areas.isEmpty())) {
                                    userLocation = getUserLocation(userLocations, userKey);
                                }

                                for (ConstraintSpatialFilter spatialConstraint : spatialConstraints) {
                                    if (!filterUserWithConstraintSpatialFilter(userLocation, spatialConstraint)) {
                                        includeUser = false;
                                        break;
                                    }
                                }
                            }

                            // Decide if user should be added to the final
                            // result
                            if (includeUser) {
                                expandedPopulationFilter.getUsers().add(userKey);
                                expandedPopulationFilter.getLabels().add(user.getUsername());
                                expandedPopulationFilter.getHashes().add(md.digest(userKey.toString().getBytes("UTF-8")));
                                if (userMeter != null) {
                                    expandedPopulationFilter.getSerials().add(md.digest(userMeter.getSerial().getBytes("UTF-8")));
                                } else {
                                    expandedPopulationFilter.getSerials().add(null);
                                }
                            }

                        }
                    }

                    // Add group only if it has at least one user
                    if (!expandedPopulationFilter.getUsers().isEmpty()) {
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
                        ExpandedPopulationFilter expandedPopulationFilter;

                        if (population.getRanking() == null) {
                            expandedPopulationFilter = new ExpandedPopulationFilter(population.getLabel());
                        } else {
                            expandedPopulationFilter = new ExpandedPopulationFilter(population.getLabel(), population.getRanking());
                        }

                        expandedPopulationFilter.setAreaId(areaCounter);

                        for (int i = 0, count = population.getUsers().size(); i < count; i++) {
                            if (area.contains(getUserLocation(userLocations, population.getUsers().get(i)))) {
                                // Copy fields
                                expandedPopulationFilter.getUsers().add(population.getUsers().get(i));
                                expandedPopulationFilter.getLabels().add(population.getLabels().get(i));
                                expandedPopulationFilter.getHashes().add(population.getHashes().get(i));
                                expandedPopulationFilter.getSerials().add(population.getSerials().get(i));
                            }
                        }

                        if (!expandedPopulationFilter.getUsers().isEmpty()) {
                            expandedQuery.getGroups().add(expandedPopulationFilter);
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
                            return response;
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
                    return response;
            }

            // Set metrics and add any required dependencies
            List<EnumMetric> metrics = new ArrayList<EnumMetric>();

            for (EnumMetric m : query.getMetrics()) {
                metrics.add(m);
            }

            // Construct expanded query
            expandedQuery.setStartDateTime(startDateTime);
            expandedQuery.setEndDateTime(endDateTime);
            expandedQuery.setGranularity(query.getTime().getGranularity());
            expandedQuery.setMetrics(metrics);

            response.setMeters(waterMeterForecastRepository.forecast(expandedQuery));

            return response;
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

    private boolean filterUserWithConstraintSpatialFilter(Geometry userLocation, ConstraintSpatialFilter filter) {
        if (userLocation == null) {
            return false;
        }
        switch (filter.getOperation()) {
            case CONTAINS:
                return filter.getGeometry().contains(userLocation);
            case INTERSECT:
                return filter.getGeometry().intersects(userLocation);
            case DISTANCE:
                return (distance(filter.getGeometry(), userLocation) < filter.getDistance());
            default:
                return false;
        }
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
     * Searches for a user and checks if the query executor has permissions to access the selected user.
     *
     * @param executor the user who executes the query.
     * @param key the user key to check.
     * @return user information for the given {@code key}.
     * @throws ApplicationException when a user does not exists or the executor has not the required permissions.
     */
    private AuthenticatedUser authorizeUser(AuthenticatedUser executor, UUID key) throws ApplicationException {
        if ((executor != null) &&
            (!executor.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) &&
            (!executor.getKey().equals(key))) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        AuthenticatedUser user = userRepository.getUserByKey(key);

        if (user == null) {
            throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", key);
        }

        // Filter users based on the utility only when an authenticated user exists
        if ((executor != null) && (!executor.getUtilities().contains(user.getUtilityId()))) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        return user;
    }
}
