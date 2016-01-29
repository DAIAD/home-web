package eu.daiad.web.controller.rest;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.IDeviceRepository;
import eu.daiad.web.model.ApplicationUser;
import eu.daiad.web.model.Error;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.device.AmphiroDeviceRegistrationRequest;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.DeviceRegistrationQueryResult;
import eu.daiad.web.model.device.DeviceRegistrationRequest;
import eu.daiad.web.model.device.DeviceRegistrationResponse;
import eu.daiad.web.model.device.WaterMeterDeviceRegistrationRequest;
import eu.daiad.web.security.AuthenticationService;

@RestController("RestDeviceController")
public class DeviceController {

	private static final int ERROR_DEVICE_EXISTS = 101;

	private static final Log logger = LogFactory.getLog(DeviceController.class);

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private IDeviceRepository repository;

	@RequestMapping(value = "/api/v1/device/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse register(@RequestBody DeviceRegistrationRequest data) {
		UUID deviceKey = null;

		try {
			ApplicationUser user = this.authenticationService
					.authenticateAndGetUser(data.getCredentials());
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed.");
			}

			switch (data.getType()) {
			case AMPHIRO:
				if (data instanceof AmphiroDeviceRegistrationRequest) {
					AmphiroDeviceRegistrationRequest amphiroData = (AmphiroDeviceRegistrationRequest) data;

					Device device = repository
							.getUserAmphiroDeviceByMacAddress(user.getKey(),
									amphiroData.getMacAddress());

					if (device != null) {
						return new RestResponse(ERROR_DEVICE_EXISTS,
								String.format("Device [%s] already exists.",
										amphiroData.getMacAddress()));
					}

					deviceKey = repository.createAmphiroDevice(user.getKey(),
							amphiroData.getName(), amphiroData.getMacAddress(),
							amphiroData.getProperties());
				}
				break;
			case METER:
				if (data instanceof WaterMeterDeviceRegistrationRequest) {
					WaterMeterDeviceRegistrationRequest meterData = (WaterMeterDeviceRegistrationRequest) data;

					Device device = repository.getUserWaterMeterDeviceBySerial(
							user.getKey(), meterData.getSerial());

					if (device != null) {
						return new RestResponse(ERROR_DEVICE_EXISTS,
								String.format("Device [%s] already exists.",
										meterData.getSerial()));
					}

					deviceKey = repository.createMeterDevice(user.getKey(),
							meterData.getSerial(), meterData.getProperties());
				}
				break;
			default:
				break;
			}

			DeviceRegistrationResponse response = new DeviceRegistrationResponse();
			response.setDeviceKey(deviceKey.toString());

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
