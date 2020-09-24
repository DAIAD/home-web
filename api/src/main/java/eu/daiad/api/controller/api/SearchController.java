package eu.daiad.api.controller.api;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.api.controller.BaseRestController;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.amphiro.AmphiroMeasurementIndexIntervalQuery;
import eu.daiad.common.model.amphiro.AmphiroMeasurementIndexIntervalQueryResult;
import eu.daiad.common.model.amphiro.AmphiroMeasurementTimeIntervalQuery;
import eu.daiad.common.model.amphiro.AmphiroMeasurementTimeIntervalQueryResult;
import eu.daiad.common.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.common.model.amphiro.AmphiroSessionCollectionIndexIntervalQueryResult;
import eu.daiad.common.model.amphiro.AmphiroSessionCollectionTimeIntervalQuery;
import eu.daiad.common.model.amphiro.AmphiroSessionCollectionTimeIntervalQueryResult;
import eu.daiad.common.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.common.model.amphiro.AmphiroSessionIndexIntervalQueryResult;
import eu.daiad.common.model.amphiro.AmphiroSessionTimeIntervalQuery;
import eu.daiad.common.model.amphiro.AmphiroSessionTimeIntervalQueryResult;
import eu.daiad.common.model.device.AmphiroDevice;
import eu.daiad.common.model.device.Device;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.device.WaterMeterDevice;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.DeviceErrorCode;
import eu.daiad.common.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.common.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.common.model.meter.WaterMeterStatus;
import eu.daiad.common.model.meter.WaterMeterStatusQuery;
import eu.daiad.common.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.common.repository.application.IAmphiroTimeOrderedRepository;
import eu.daiad.common.repository.application.IDeviceRepository;
import eu.daiad.common.repository.application.IMeterDataRepository;
import eu.daiad.common.repository.application.IUserRepository;

/**
 * Provides actions for searching Amphiro B1 sessions and smart water meter readings.
 */
@RestController
public class SearchController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(SearchController.class);

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

    /**
     * Repository for accessing amphiro b1 data indexed by time.
     */
    @Autowired
    private IAmphiroTimeOrderedRepository amphiroTimeOrderedRepository;

    /**
     * Repository for accessing amphiro b1 data indexed by shower id.
     */
    @Autowired
    private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    /**
     * Repository for accessing smart water meter data.
     */
    @Autowired
    private IMeterDataRepository waterMeterMeasurementRepository;

    /**
     * Returns the status of one or more smart water meters.
     *
     * @param query the query.
     * @return the meter status.
     */
    @PostMapping(value = "/api/v1/meter/status")
    public RestResponse getWaterMeterStatus(@RequestBody WaterMeterStatusQuery query) {
        try {
            AuthenticatedUser user = authenticate(query.getCredentials(), EnumRole.ROLE_USER);

            if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
                return new WaterMeterStatusQueryResult();
            }

            String[] serials = checkMeterOwnership(user.getKey(), query.getDeviceKey());

            WaterMeterStatusQueryResult result = waterMeterMeasurementRepository.getStatus(serials);

            for (WaterMeterStatus status : result.getDevices()) {
                for (int i = 0, count = serials.length; i < count; i++) {
                    if (status.getSerial().equals(serials[i])) {
                        status.setDeviceKey(query.getDeviceKey()[i]);
                        break;
                    }
                }
            }

            return result;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads smart water meter readings on a given query.
     *
     * @param query the query.
     * @return the meter readings.
     */
    @PostMapping(value = "/api/v1/meter/history")
    public RestResponse searchWaterMeterMeasurements(@RequestBody WaterMeterMeasurementQuery query) {
        try {
            AuthenticatedUser user = authenticate(query.getCredentials(), EnumRole.ROLE_USER);

            if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
                return new WaterMeterMeasurementQueryResult();
            }

            String[] serials = checkMeterOwnership(user.getKey(), query.getDeviceKey());

            WaterMeterMeasurementQueryResult data = waterMeterMeasurementRepository.searchMeasurements(serials,
                            DateTimeZone.forID(user.getTimezone()), query);

            return data;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads Amphiro B1 session measurements based on a given query. Amphiro B1 sessions are indexed by time.
     *
     * @param query the query.
     * @return the measurements.
     */
    @PostMapping(value = "/api/v1/device/measurement/query")
    public RestResponse searchAmphiroMeasurementsByTime(@RequestBody AmphiroMeasurementTimeIntervalQuery query) {
        try {
            AuthenticatedUser user = authenticate(query.getCredentials(), EnumRole.ROLE_USER);

            query.setUserKey(user.getKey());

            if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
                return new AmphiroMeasurementTimeIntervalQueryResult();
            }

            this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

            AmphiroMeasurementTimeIntervalQueryResult data = amphiroTimeOrderedRepository.searchMeasurements(
                            DateTimeZone.forID(user.getTimezone()), query);

            return data;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads Amphiro B1 session measurements based on a given query. Amphiro B1 sessions are indexed by id.
     *
     * @param query the query.
     * @return the measurements.
     */
    @PostMapping(value = "/api/v2/device/measurement/query")
    public RestResponse searchAmphiroMeasurementsByIndex(@RequestBody AmphiroMeasurementIndexIntervalQuery query) {
        try {
            AuthenticatedUser user = authenticate(query.getCredentials(), EnumRole.ROLE_USER);

            query.setUserKey(user.getKey());

            if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
                return new AmphiroMeasurementIndexIntervalQueryResult();
            }

            this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

            AmphiroMeasurementIndexIntervalQueryResult data = amphiroIndexOrderedRepository.getMeasurements(DateTimeZone.forID(user.getTimezone()), query);

            return data;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads Amphiro B1 sessions based on a given query. Amphiro B1 sessions are indexed by time.
     *
     * @param query the query.
     * @return the measurements.
     */
    @PostMapping(value = "/api/v1/device/session/query")
    public RestResponse searchAmphiroSessionsWithTimeOrdering(
                    @RequestBody AmphiroSessionCollectionTimeIntervalQuery query) {
        try {
            AuthenticatedUser user = authenticate(query.getCredentials(), EnumRole.ROLE_USER);

            query.setUserKey(user.getKey());

            if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
                return new AmphiroSessionCollectionTimeIntervalQueryResult();
            }

            String[] names = this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

            AmphiroSessionCollectionTimeIntervalQueryResult data = amphiroTimeOrderedRepository.searchSessions(names, DateTimeZone.forID(user.getTimezone()), query);

            return data;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads Amphiro B1 sessions based on a given query. Amphiro B1 sessions are indexed by id.
     *
     * @param query the query.
     * @return the measurements.
     */
    @PostMapping(value = "/api/v2/device/session/query")
    public RestResponse searchAmphiroSessionsWithIndexOrdering(@RequestBody AmphiroSessionCollectionIndexIntervalQuery query) {
        try {
            AuthenticatedUser user = authenticate(query.getCredentials(), EnumRole.ROLE_USER, EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN);

            // If user has not administrative permissions or user key is null,
            // use the key of the authenticated user
            if ((query.getUserKey() == null) || (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN))) {
                query.setUserKey(user.getKey());
            }

            // Check utility access
            if (!user.getKey().equals(query.getUserKey())) {
                AuthenticatedUser deviceOwner = userRepository.getUserByKey(query.getUserKey());

                if (!user.getUtilities().contains(deviceOwner.getUtilityId())) {
                    throw createApplicationException(DeviceErrorCode.DEVICE_ACCESS_DENIED).set("user", user.getKey() + " - " + user.getUsername())
                                                                                          .set("owner", query.getUserKey() + " - " + deviceOwner.getUsername());
                }
            }

            if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
                return new AmphiroSessionCollectionIndexIntervalQueryResult();
            }

            String[] names = this.checkAmphiroOwnership(query.getUserKey(), query.getDeviceKey());

            return amphiroIndexOrderedRepository.getSessions(names, DateTimeZone.forID(user.getTimezone()), query);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads an Amphiro B1 session based on a given query. Amphiro B1 session is indexed by time.
     *
     * @param query the query
     * @return the sessions
     */
    @PostMapping(value = "/api/v1/device/session")
    public RestResponse getAmphiroSessionByTime(@RequestBody AmphiroSessionTimeIntervalQuery query) {
        try {
            AuthenticatedUser user = authenticate(query.getCredentials(), EnumRole.ROLE_USER);

            query.setUserKey(user.getKey());

            if (query.getDeviceKey() == null) {
                return new AmphiroSessionTimeIntervalQueryResult();
            }

            this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

            AmphiroSessionTimeIntervalQueryResult data = amphiroTimeOrderedRepository.getSession(query);

            return data;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads an Amphiro B1 session based on a given query. Amphiro B1 session is indexed by id.
     *
     * @param query the query
     * @return the sessions
     */
    @PostMapping(value = "/api/v2/device/session")
    public RestResponse getAmphiroSessionByIndex(@RequestBody AmphiroSessionIndexIntervalQuery query) {
        try {
            AuthenticatedUser user = authenticate(query.getCredentials(), EnumRole.ROLE_USER);

            query.setUserKey(user.getKey());

            if (query.getDeviceKey() == null) {
                return new AmphiroSessionIndexIntervalQueryResult();
            }

            this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

            AmphiroSessionIndexIntervalQueryResult data = amphiroIndexOrderedRepository.getSession(query);

            return data;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Checks if the user can access a specific amphiro b1 device.
     *
     * @param userKey the user key.
     * @param deviceKey the device key.
     * @return the name of the device if the user can access the device.
     * @throws ApplicationException if the user can not access the device.
     */
    private String[] checkAmphiroOwnership(UUID userKey, UUID deviceKey) throws ApplicationException {
        if (deviceKey != null) {
            return this.checkAmphiroOwnership(userKey, new UUID[] { deviceKey });
        }

        return new String[] { null };
    }

    /**
     * Checks if the user can access the specific amphiro b1 devices.
     *
     * @param userKey the user key.
     * @param devices the device keys.
     * @return the names of the devices if the user can access the devices.
     * @throws ApplicationException if the user can not access the devices.
     */
    private String[] checkAmphiroOwnership(UUID userKey, UUID[] devices) throws ApplicationException  {
        ArrayList<String> nameList = new ArrayList<String>();

        if (devices != null) {
            for (UUID deviceKey : devices) {
                Device device = deviceRepository.getUserDeviceByKey(userKey, deviceKey);

                if ((device == null) || (!device.getType().equals(EnumDeviceType.AMPHIRO))) {
                    throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
                }

                nameList.add(((AmphiroDevice) device).getName());
            }
        }

        String[] nameArray = new String[nameList.size()];

        return nameList.toArray(nameArray);
    }

    /**
     * Checks if the user can access the specific smart water meters.
     *
     * @param userKey the user key.
     * @param devices the smart water meter keys.
     * @return the serial numbers of the smart water meters if the user can access the devices.
     * @throws ApplicationException if the user can not access the devices.
     */
    private String[] checkMeterOwnership(UUID userKey, UUID[] devices) throws ApplicationException {
        ArrayList<String> serialList = new ArrayList<String>();

        if (devices != null) {
            for (UUID deviceKey : devices) {
                Device device = deviceRepository.getUserDeviceByKey(userKey, deviceKey);

                if ((device == null) || (!device.getType().equals(EnumDeviceType.METER))) {
                    throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
                }

                serialList.add(((WaterMeterDevice) device).getSerial());
            }
        }

        String[] serialArray = new String[serialList.size()];

        return serialList.toArray(serialArray);
    }

}
