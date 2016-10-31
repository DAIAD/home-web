package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.device.DeviceUpdateRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IDeviceRepository;

/**
 * Provides actions for configuring amphiro b1 devices and smart water meters.
 */
@RestController()
public class DeviceController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DeviceController.class);

    /**
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

    /**
     * Updates the properties of a device.
     *
     * @param request the device and the properties to update.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/device/update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ "ROLE_RUSER" })
    public RestResponse update(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody DeviceUpdateRequest request) {
        try {
            deviceRepository.updateDevice(user.getKey(), request);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

}
