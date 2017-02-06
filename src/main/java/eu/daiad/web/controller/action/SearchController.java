package eu.daiad.web.controller.action;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterStatus;
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

@RestController
public class SearchController extends BaseController {

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
     * Repository for accessing amphiro b1 data indexed by shower id.
     */
    @Autowired
    private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    /**
     * Repository for accessing smart water meter data.
     */
    @Autowired
    private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

    /**
     * Returns the status of one or more smart water meters.
     *
     * @param user the currently authenticated user.
     * @param query the query.
     * @return the meter status.
     */
    @RequestMapping(value = "/action/meter/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getMeterStatus(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody WaterMeterStatusQuery query) {
        RestResponse response = new RestResponse();

        try {
            // If user has not administrative permissions or user key is null,
            // use the key of the authenticated user
            if ((query.getUserKey() == null) || (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN))) {
                query.setUserKey(user.getKey());
            }

            // Check utility access
            if (!user.getKey().equals(query.getUserKey())) {
                AuthenticatedUser deviceOwner = userRepository.getUserByUtilityAndKey(user.getUtilityId(), query.getUserKey());
                if (deviceOwner == null) {
                    throw createApplicationException(DeviceErrorCode.DEVICE_ACCESS_DENIED).set("user", user.getKey())
                                                                                          .set("owner", query.getUserKey());
                }
            }

            if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
                DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
                deviceQuery.setType(EnumDeviceType.METER);

                ArrayList<UUID> deviceKeys = new ArrayList<UUID>();

                for (Device d : deviceRepository.getUserDevices(query.getUserKey(), deviceQuery)) {
                    deviceKeys.add(d.getKey());
                }

                UUID[] deviceKeyArray = new UUID[deviceKeys.size()];

                query.setDeviceKey(deviceKeys.toArray(deviceKeyArray));
            }

            String[] serials = checkMeterOwnership(query.getUserKey(), query.getDeviceKey());

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

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Loads smart water meter readings on a given query.
     *
     * @param user the currently authenticated user.
     * @param query the query.
     * @return the meter readings.
     */
    @RequestMapping(value = "/action/meter/history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getMeterMeasurements(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody WaterMeterMeasurementQuery query) {
        RestResponse response = new RestResponse();

        try {
            // If user has not administrative permissions or user key is null,
            // use the key of the authenticated user
            if ((query.getUserKey() == null) || (!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN))) {
                query.setUserKey(user.getKey());
            }

            // Check utility access
            if (!user.getKey().equals(query.getUserKey())) {
                AuthenticatedUser deviceOwner = userRepository.getUserByKey(query.getUserKey());
                if (!user.getUtilities().contains(deviceOwner.getUtilityId())) {
                    throw createApplicationException(DeviceErrorCode.DEVICE_ACCESS_DENIED).set("user", user.getKey())
                                                                                          .set("owner", query.getUserKey());
                }
            }

            if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
                DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
                deviceQuery.setType(EnumDeviceType.METER);

                ArrayList<UUID> deviceKeys = new ArrayList<UUID>();

                for (Device d : deviceRepository.getUserDevices(query.getUserKey(), deviceQuery)) {
                    deviceKeys.add(d.getKey());
                }

                UUID[] deviceKeyArray = new UUID[deviceKeys.size()];

                query.setDeviceKey(deviceKeys.toArray(deviceKeyArray));
            }

            String[] serials = checkMeterOwnership(query.getUserKey(), query.getDeviceKey());

            return waterMeterMeasurementRepository.searchMeasurements(serials, DateTimeZone.forID(user.getTimezone()),
                            query);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Loads Amphiro B1 sessions based on a given query. Amphiro B1 sessions are
     * indexed by id.
     *
     * @param user the currently authenticated user.
     * @param query the query.
     * @return the measurements.
     */
    @RequestMapping(value = "/action/device/index/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getSessions(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody AmphiroSessionCollectionIndexIntervalQuery query) {
        try {
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
                DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
                deviceQuery.setType(EnumDeviceType.AMPHIRO);

                List<UUID> deviceKeys = new ArrayList<UUID>();

                for (Device d : deviceRepository.getUserDevices(query.getUserKey(), deviceQuery)) {
                    deviceKeys.add(d.getKey());
                }

                query.setDeviceKey(deviceKeys.toArray(new UUID[deviceKeys.size()]));
            }

            String[] names = this.checkAmphiroOwnership(query.getUserKey(), query.getDeviceKey());

            return amphiroIndexOrderedRepository.getSessions(names, DateTimeZone.forID(user.getTimezone()), query);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads an Amphiro B1 session based on a given query. Amphiro B1 session is
     * indexed by id.
     *
     * @param user the currently authenticated user.
     * @param query the query
     * @return the sessions
     */
    @RequestMapping(value = "/action/device/index/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse getSession(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody AmphiroSessionIndexIntervalQuery query) {
        try {
            this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

            query.setUserKey(user.getKey());

            return amphiroIndexOrderedRepository.getSession(query);
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
    private String[] checkAmphiroOwnership(UUID userKey, UUID devices[]) throws ApplicationException {
        ArrayList<String> nameList = new ArrayList<String>();

        if (devices != null) {
            for (UUID deviceKey : devices) {
                Device device = deviceRepository.getUserDeviceByKey(userKey, deviceKey);

                if (device == null) {
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
    private String[] checkMeterOwnership(UUID userKey, UUID devices[]) throws ApplicationException {
        ArrayList<String> serialList = new ArrayList<String>();

        if (devices != null) {
            for (UUID deviceKey : devices) {
                Device device = deviceRepository.getUserDeviceByKey(userKey, deviceKey);

                if (device == null) {
                    throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
                }

                serialList.add(((WaterMeterDevice) device).getSerial());
            }
        }

        String[] serialArray = new String[serialList.size()];

        return serialList.toArray(serialArray);
    }
}
