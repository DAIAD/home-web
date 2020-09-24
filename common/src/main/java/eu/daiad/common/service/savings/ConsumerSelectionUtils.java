package eu.daiad.common.service.savings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.common.domain.application.AreaGroupMemberEntity;
import eu.daiad.common.model.device.Device;
import eu.daiad.common.model.device.DeviceRegistrationQuery;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.device.WaterMeterDevice;
import eu.daiad.common.model.query.AreaSpatialFilter;
import eu.daiad.common.model.query.ConstraintSpatialFilter;
import eu.daiad.common.model.query.GroupSpatialFilter;
import eu.daiad.common.model.query.SpatialFilter;
import eu.daiad.common.model.query.savings.SavingsConsumerSelectionFilter;
import eu.daiad.common.model.query.savings.SavingsPopulationFilter;
import eu.daiad.common.repository.application.IDeviceRepository;
import eu.daiad.common.repository.application.IGroupRepository;
import eu.daiad.common.repository.application.ISpatialRepository;
import eu.daiad.common.repository.application.IUtilityRepository;

@Service
public class ConsumerSelectionUtils {

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
     * Given a {@link SavingsConsumerSelectionFilter} filter, return all selected
     * user keys.
     *
     * @param parameters consumer selection filter.
     * @return a list of {@link UUID}.
     */
    public List<UUID> expandUsers(SavingsConsumerSelectionFilter parameters) {
        return expand(parameters).userKeys;
    }

    /**
     * Given a {@link SavingsConsumerSelectionFilter} filter, return all selected
     * smart water meter serial numbers.
     *
     * @param parameters consumer selection filter.
     * @return a list of strings.
     */
    public List<String> expandMeters(SavingsConsumerSelectionFilter parameters) {
        return expand(parameters).meterSerials;
    }

    /**
     * Given a {@link SavingsConsumerSelectionFilter} filter, return all selected
     * smart water meter serial numbers and user keys.
     *
     * @param parameters consumer selection filter.
     * @return a list of strings.
     */
    private Result expand(SavingsConsumerSelectionFilter parameters) {
        Result result =  new Result();

        if(parameters == null) {
            return result;
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

        Set<UUID> selectedUsers = new HashSet<UUID>();
        Set<String> selectedMeters = new HashSet<String>();

        if ((parameters.getPopulation() != null) && (!parameters.getPopulation().isEmpty())) {
            Set<UUID> currentUsers = new HashSet<UUID>();
            Set<String> currentMeters = new HashSet<String>();

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
                        currentUsers.add(userKey);
                        currentMeters.add(meter.getSerial());
                    }
                }

                if(index == 0) {
                    selectedUsers.addAll(currentUsers);
                    selectedMeters.addAll(currentMeters);
                } else {
                    selectedUsers.retainAll(currentUsers);
                    selectedMeters.retainAll(currentMeters);
                }
            }
        }
        for(UUID key : selectedUsers) {
            result.userKeys.add(key);
        }
        for(String serial : selectedMeters) {
            result.meterSerials.add(serial);
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

    /**
     * Computes distance of two geometries.
     *
     * @param g1 the first geometry.
     * @param g2 the second geometry.
     * @return the distance in meters.
     */
    private double distance(Geometry g1, Geometry g2) {
        double distance = g1.distance(g2);

        return distance * (Math.PI / 180) * 6378137;
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

    private WaterMeterDevice getUserWaterMeter(UUID userKey) {
        DeviceRegistrationQuery meterQuery = new DeviceRegistrationQuery();
        meterQuery.setType(EnumDeviceType.METER);

        List<Device> devices = deviceRepository.getUserDevices(userKey, meterQuery);

        if (devices.isEmpty()) {
            return null;
        }

        return (WaterMeterDevice) devices.get(0);
    }

    private static class Result {

        List<UUID> userKeys =  new ArrayList<UUID>();

        List<String> meterSerials =  new ArrayList<String>();

    }
}
