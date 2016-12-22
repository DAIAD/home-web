package eu.daiad.web.controller.api;

import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.AmphiroDeviceRegistrationRequest;
import eu.daiad.web.model.device.AmphiroDeviceRegistrationResponse;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceAmphiroConfiguration;
import eu.daiad.web.model.device.DeviceConfigurationCollection;
import eu.daiad.web.model.device.DeviceConfigurationRequest;
import eu.daiad.web.model.device.DeviceConfigurationResponse;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.DeviceRegistrationQueryResult;
import eu.daiad.web.model.device.DeviceRegistrationRequest;
import eu.daiad.web.model.device.DeviceRegistrationResponse;
import eu.daiad.web.model.device.DeviceResetRequest;
import eu.daiad.web.model.device.DeviceUpdateRequest;
import eu.daiad.web.model.device.NotifyConfigurationRequest;
import eu.daiad.web.model.device.ShareDeviceRequest;
import eu.daiad.web.model.device.WaterMeterDeviceRegistrationRequest;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;

/**
 * Provides actions for configuring Amphiro B1 devices and smart water meters.
 */
@RestController("RestDeviceController")
public class DeviceController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DeviceController.class);

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
     * Registers an Amphiro B1 devices.
     *
     * @param data the registration data.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/device/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse registerAmphiro(@RequestBody DeviceRegistrationRequest data) {
        try {
            AuthenticatedUser user = authenticate(data.getCredentials(), EnumRole.ROLE_USER);

            switch (data.getType()) {
                case AMPHIRO:
                    if (data instanceof AmphiroDeviceRegistrationRequest) {
                        AmphiroDeviceRegistrationRequest amphiroData = (AmphiroDeviceRegistrationRequest) data;

                        // Check if a device with the same MAC Address is already registered
                        AmphiroDevice existingDevice = (AmphiroDevice) deviceRepository.getUserAmphiroDeviceByMacAddress(user.getKey(),
                                        amphiroData.getMacAddress());

                        if (existingDevice != null) {
                            throw createApplicationException(DeviceErrorCode.ALREADY_EXISTS).set("id",
                                            amphiroData.getMacAddress());
                        }

                        // Create new device
                        AmphiroDevice newDevice = deviceRepository.createAmphiroDevice(user.getKey(), amphiroData
                                        .getName(), amphiroData.getMacAddress(), amphiroData.getAesKey(), amphiroData
                                        .getProperties());

                        // Get device configuration
                        List<DeviceConfigurationCollection> deviceConfigurationCollection = deviceRepository
                                        .getConfiguration(user.getKey(), new UUID[] { newDevice.getKey() });

                        // Update response
                        AmphiroDeviceRegistrationResponse deviceResponse = new AmphiroDeviceRegistrationResponse();
                        deviceResponse.setDeviceKey(newDevice.getKey());
                        deviceResponse.setAesKey(newDevice.getAesKey());
                        deviceResponse.setMacAddress(newDevice.getMacAddress());
                        deviceResponse.setName(newDevice.getName());
                        deviceResponse.setRegisteredOn(newDevice.getRegisteredOn());


                        if (deviceConfigurationCollection.size() == 1) {
                            for (DeviceAmphiroConfiguration configuration : deviceConfigurationCollection.get(0)
                                            .getConfigurations()) {
                                deviceResponse.getConfigurations().add(configuration);
                            }
                        }

                        for (KeyValuePair p : newDevice.getProperties()) {
                            deviceResponse.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                        }

                        return deviceResponse;
                    } else {
                        throw createApplicationException(SharedErrorCode.INVALID_PARSED_OBJECT);
                    }
                default:
                    throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type",
                                    data.getType().toString());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Registers a smart water meter to a user.
     *
     * @param data the smart water meter registration data.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/meter/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse registerMeter(@RequestBody DeviceRegistrationRequest data) {
        UUID deviceKey = null;

        try {
            AuthenticatedUser user = authenticate(data.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

            switch (data.getType()) {
                case METER:
                    if (data instanceof WaterMeterDeviceRegistrationRequest) {
                        WaterMeterDeviceRegistrationRequest meterData = (WaterMeterDeviceRegistrationRequest) data;

                        AuthenticatedUser owner = userRepository.getUserByUtilityAndKey(user.getUtilityId(), meterData.getUserKey());

                        if (owner == null) {
                            throw createApplicationException(DeviceErrorCode.DEVICE_OWNER_NOT_FOUND).set("meter",
                                            meterData.getSerial()).set("key",
                                            (meterData.getUserKey() == null ? "" : meterData.getUserKey().toString()));
                        }

                        Device device = deviceRepository.getWaterMeterDeviceBySerial(meterData.getSerial());

                        if (device != null) {
                            throw createApplicationException(DeviceErrorCode.ALREADY_EXISTS).set("id",
                                            meterData.getSerial());
                        }

                        deviceKey = deviceRepository.createMeterDevice(owner.getUsername(), meterData.getSerial(),
                                        meterData.getProperties(), meterData.getLocation());

                        DeviceRegistrationResponse deviceResponse = new DeviceRegistrationResponse();
                        deviceResponse.setDeviceKey(deviceKey);

                        return deviceResponse;
                    } else {
                        throw createApplicationException(SharedErrorCode.INVALID_PARSED_OBJECT);
                    }
                default:
                    throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type", data.getType().toString());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads all registered devices including Amphiro B1 devices and smart water meters. Optionally
     * the devices are filtered.
     *
     * @param query the query to filter the devices.
     * @return the devices.
     */
    @RequestMapping(value = "/api/v1/device/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse list(@RequestBody DeviceRegistrationQuery query) {
        try {
            AuthenticatedUser user = authenticate(query.getCredentials(), EnumRole.ROLE_USER);

            List<Device> devices = deviceRepository.getUserDevices(user.getKey(), query);

            DeviceRegistrationQueryResult queryResponse = new DeviceRegistrationQueryResult();
            queryResponse.setDevices(devices);

            return queryResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Shares data of an Amphiro B1 device between two user accounts.
     *
     * @param request the share request.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/device/share", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse share(@RequestBody ShareDeviceRequest request) {
        RestResponse response = new RestResponse();

        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            deviceRepository.shareDevice(user.getKey(), request.getAssignee(), request.getDevice(), request.isShared());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Loads all Amphiro B1 configurations.
     *
     * @param request the configuration request.
     * @return the Amphiro B1 configurations.
     */
    @RequestMapping(value = "/api/v1/device/config", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse configuration(@RequestBody DeviceConfigurationRequest request) {
        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            DeviceConfigurationResponse configurationResponse = new DeviceConfigurationResponse();

            List<DeviceConfigurationCollection> deviceConfigurations = deviceRepository.getConfiguration(user.getKey(), request.getDeviceKey());

            for (int c = deviceConfigurations.size() - 1; c >= 0; c--) {
                for (int i = deviceConfigurations.get(c).getConfigurations().size() - 1; i >= 0; i--) {
                    if (deviceConfigurations.get(c).getConfigurations().get(i).getAcknowledgedOn() != null) {
                        deviceConfigurations.get(c).getConfigurations().remove(i);
                    }
                }
                if (deviceConfigurations.get(c).getConfigurations().size() == 0) {
                    deviceConfigurations.remove(c);
                }
            }

            configurationResponse.setDevices(deviceConfigurations);

            return configurationResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Notifies the server that a set of configuration parameters has been applied by the
     * mobile client to an Amphiro device.
     *
     * @param request the notification request.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/device/notify", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse notify(@RequestBody NotifyConfigurationRequest request) {
        RestResponse response = new RestResponse();

        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            deviceRepository.notifyConfiguration(user.getKey(), request.getDeviceKey(), request.getVersion(),
                            new DateTime(request.getUpdatedOn()));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Deletes a device registration from a user's profile.
     *
     * @param request the registration delete request.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/device/reset", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse remove(@RequestBody DeviceResetRequest request) {
        RestResponse response = new RestResponse();

        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

            deviceRepository.removeDevice(request.getDeviceKey());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Updates a device properties.
     *
     * @param request the update request.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/device/update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse update(@RequestBody DeviceUpdateRequest request) {
        RestResponse response = new RestResponse();

        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            deviceRepository.updateDevice(user.getKey(), request);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

}
