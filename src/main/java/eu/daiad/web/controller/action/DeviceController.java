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
 * Provides actions for configuring Amphiro B1 devices and smart water meters.
 */
@RestController()
public class DeviceController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(DeviceController.class);

	@Autowired
	private IDeviceRepository deviceRepository;

    /**
     * Updates a device properties.
     *  
     * @param request the update request.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/device/update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ "ROLE_RUSER" })
    public RestResponse update(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody DeviceUpdateRequest request) {
        RestResponse response = new RestResponse();

        try {
            deviceRepository.updateDevice(user.getKey(), request);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

}
