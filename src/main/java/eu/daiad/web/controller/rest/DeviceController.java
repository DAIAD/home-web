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

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.data.IDeviceRepository;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.device.AmphiroDeviceRegistrationRequest;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceConfigurationRequest;
import eu.daiad.web.model.device.DeviceConfigurationResponse;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.DeviceRegistrationQueryResult;
import eu.daiad.web.model.device.DeviceRegistrationRequest;
import eu.daiad.web.model.device.DeviceRegistrationResponse;
import eu.daiad.web.model.device.ShareDeviceRequest;
import eu.daiad.web.model.device.WaterMeterDeviceRegistrationRequest;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;

@RestController("RestDeviceController")
public class DeviceController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(DeviceController.class);

	@Autowired
	private IDeviceRepository repository;

	@RequestMapping(value = "/api/v1/device/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse register(@RequestBody DeviceRegistrationRequest data) {
		RestResponse response = new RestResponse();

		UUID deviceKey = null;

		try {
			AuthenticatedUser user = this.authenticate(data.getCredentials(), EnumRole.ROLE_USER);

			switch (data.getType()) {
			case AMPHIRO:
				if (data instanceof AmphiroDeviceRegistrationRequest) {
					AmphiroDeviceRegistrationRequest amphiroData = (AmphiroDeviceRegistrationRequest) data;

					Device device = repository.getUserAmphiroDeviceByMacAddress(user.getKey(),
									amphiroData.getMacAddress());

					if (device != null) {
						throw new ApplicationException(DeviceErrorCode.ALREADY_EXISTS).set("id",
										amphiroData.getMacAddress());
					}

					deviceKey = repository.createAmphiroDevice(user.getKey(), amphiroData.getName(),
									amphiroData.getMacAddress(), amphiroData.getAesKey(), amphiroData.getProperties());
				}
				break;
			case METER:
				if (data instanceof WaterMeterDeviceRegistrationRequest) {
					WaterMeterDeviceRegistrationRequest meterData = (WaterMeterDeviceRegistrationRequest) data;

					Device device = repository.getUserWaterMeterDeviceBySerial(user.getKey(), meterData.getSerial());

					if (device != null) {
						throw new ApplicationException(DeviceErrorCode.ALREADY_EXISTS).set("id", meterData.getSerial());
					}

					deviceKey = repository.createMeterDevice(user.getKey(), meterData.getSerial(),
									meterData.getProperties());
				}
				break;
			default:
				break;
			}

			DeviceRegistrationResponse deviceResponse = new DeviceRegistrationResponse();
			deviceResponse.setDeviceKey(deviceKey.toString());

			return deviceResponse;
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/device/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse list(@RequestBody DeviceRegistrationQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			ArrayList<Device> devices = repository.getUserDevices(user.getKey(), query);

			DeviceRegistrationQueryResult queryResponse = new DeviceRegistrationQueryResult();
			queryResponse.setDevices(devices);

			return queryResponse;
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/device/share", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse share(@RequestBody ShareDeviceRequest request) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(request.getCredentials(), EnumRole.ROLE_USER);

			repository.shareDevice(user.getKey(), request.getAssignee(), request.getDevice(), request.isShared());
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/device/config", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse configuration(@RequestBody DeviceConfigurationRequest request) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(request.getCredentials(), EnumRole.ROLE_USER);

			DeviceConfigurationResponse configuration = new DeviceConfigurationResponse();

			configuration.setDevices(repository.getConfiguration(user.getKey(), request.getDeviceKey()));

			return configuration;
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}
