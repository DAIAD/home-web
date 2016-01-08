package eu.daiad.web.controller.rest;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.DeviceRepository;
import eu.daiad.web.model.AmphiroDeviceRegistrationRequest;
import eu.daiad.web.model.Device;
import eu.daiad.web.model.DeviceRegistrationQueryResult;
import eu.daiad.web.model.DeviceRegistrationQuery;
import eu.daiad.web.model.DeviceRegistrationRequest;
import eu.daiad.web.model.DeviceRegistrationResponse;
import eu.daiad.web.model.Error;
import eu.daiad.web.model.MeterDeviceRegistrationRequest;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.security.AuthenticationService;
import eu.daiad.web.security.model.ApplicationUser;

@RestController("RestDeviceController")
public class DeviceController {

	private static final int ERROR_DEVICE_EXISTS = 101;

	private static final Log logger = LogFactory.getLog(DeviceController.class);

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private DeviceRepository repository;

	@RequestMapping(value = "/api/v1/device/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse register(@RequestBody DeviceRegistrationRequest data) {

		try {
			ApplicationUser user = this.authenticationService
					.authenticateAndGetUser(data.getCredentials());
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed.");
			}

			Device device = repository.getUserDeviceById(data.getDeviceId(),
					user.getKey());
			if (device != null) {
				return new RestResponse(ERROR_DEVICE_EXISTS, String.format(
						"Device [%s] already exists.", data.getDeviceId()));
			}

			switch (data.getType()) {
			case AMPHIRO:
				if (data instanceof AmphiroDeviceRegistrationRequest) {
					AmphiroDeviceRegistrationRequest amphiroData = (AmphiroDeviceRegistrationRequest) data;
					device = repository.createAmphiroDevice(user.getKey(),
							amphiroData.getDeviceId(), amphiroData.getName(),
							amphiroData.getProperties());
				}
				break;
			case METER:
				if (data instanceof MeterDeviceRegistrationRequest) {
					MeterDeviceRegistrationRequest meterData = (MeterDeviceRegistrationRequest) data;
					device = repository.createMeterDevice(user.getKey(),
							meterData.getDeviceId(), meterData.getProperties());
				}
				break;
			default:
				break;
			}

			DeviceRegistrationResponse response = new DeviceRegistrationResponse();
			response.setDeviceKey(device.getKey().toString());

			return response;
		} catch (Exception ex) {
			logger.error("An unhandled exception has occurred", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");
	}

	@RequestMapping(value = "/api/v1/device/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse list(@RequestBody DeviceRegistrationQuery query) {

		try {
			ApplicationUser user = this.authenticationService
					.authenticateAndGetUser(query.getCredentials());
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed.");
			}

			ArrayList<Device> devices = repository.getUserDevices(
					user.getKey(), query);

			DeviceRegistrationQueryResult response = new DeviceRegistrationQueryResult();
			response.setDevices(devices);

			return response;
		} catch (Exception ex) {
			logger.error("An unhandled exception has occurred", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");
	}
}
