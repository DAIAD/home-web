package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.AmphiroMeasurementRepository;
import eu.daiad.web.data.DeviceRepository;
import eu.daiad.web.data.WaterMeterMeasurementRepository;
import eu.daiad.web.model.AmphiroDevice;
import eu.daiad.web.model.Device;
import eu.daiad.web.model.AmphiroMeasurementCollection;
import eu.daiad.web.model.EnumDeviceType;
import eu.daiad.web.model.Error;
import eu.daiad.web.model.DeviceMeasurementCollection;
import eu.daiad.web.model.WaterMeterMeasurementCollection;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.security.AuthenticationService;
import eu.daiad.web.security.model.ApplicationUser;
import eu.daiad.web.service.IExportService;

@RestController("RestDataController")
public class DataController {

	private static final int ERROR_TYPE_NOT_SUPPORTED = 3;
	private static final int ERROR_DEVICE_NOT_FOUND = 4;

	private static final Log logger = LogFactory.getLog(DataController.class);

	@Value("${tmp.folder}")
	private String temporaryPath;

	@Autowired
	private AmphiroMeasurementRepository amphiroMeasurementRepository;

	@Autowired
	private WaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@Autowired
	private IExportService exportService;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private AuthenticationService authenticator;

	@RequestMapping(value = "/api/v1/data/store", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse store(@RequestBody DeviceMeasurementCollection data) {
		try {
			ApplicationUser user = this.authenticator
					.authenticateAndGetUser(data.getCredentials());
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed.");
			}
			if (!user.hasRole("ROLE_USER")) {
				return new RestResponse(Error.ERROR_FORBIDDEN,
						"Unauthhorized request.");
			}

			data.setUserKey(user.getKey());

			Device device = this.deviceRepository.getUserDeviceByKey(
					data.getDeviceKey(), data.getUserKey());

			if (device == null) {
				return new RestResponse(ERROR_DEVICE_NOT_FOUND,
						"Device does not exist.");
			}

			switch (data.getType()) {
			case AMPHIRO:
				if (data instanceof AmphiroMeasurementCollection) {
					if (!device.getType().equals(EnumDeviceType.AMPHIRO)) {
						return new RestResponse(ERROR_TYPE_NOT_SUPPORTED,
								"Invalid device type.");
					}
					amphiroMeasurementRepository.storeData(
							(ApplicationUser) user, (AmphiroDevice) device,
							(AmphiroMeasurementCollection) data);
				}
				break;
			case METER:
				if (data instanceof WaterMeterMeasurementCollection) {
					waterMeterMeasurementRepository
							.storeData((WaterMeterMeasurementCollection) data);
				}
				break;
			default:
				break;
			}

			return new RestResponse();
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);

		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}

}
