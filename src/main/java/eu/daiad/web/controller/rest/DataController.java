package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.DeviceMeasurementCollection;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.query.DataQueryRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IAmphiroMeasurementRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;
import eu.daiad.web.service.IDataService;

@RestController("RestDataController")
public class DataController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(DataController.class);

	@Value("${tmp.folder}")
	private String temporaryPath;

	@Autowired
	private IAmphiroMeasurementRepository amphiroMeasurementRepository;

	@Autowired
	private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IDataService dataService;

	@RequestMapping(value = "/api/v1/data/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse query(@RequestBody DataQueryRequest data) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data.getCredentials(), EnumRole.ROLE_ADMIN);

			return dataService.execute(data.getQuery());
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/data/store", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse store(@RequestBody DeviceMeasurementCollection data) {
		RestResponse response = new RestResponse();

		AuthenticatedUser user = null;
		Device device = null;

		boolean success = true;

		try {
			user = this.authenticate(data.getCredentials(), EnumRole.ROLE_USER);

			device = this.deviceRepository.getUserDeviceByKey(user.getKey(), data.getDeviceKey());

			if (device == null) {
				throw new ApplicationException(DeviceErrorCode.NOT_FOUND).set("key", data.getDeviceKey().toString());
			}

			switch (data.getType()) {
				case AMPHIRO:
					if (data instanceof AmphiroMeasurementCollection) {
						if (!device.getType().equals(EnumDeviceType.AMPHIRO)) {
							throw new ApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type", data.getType()
											.toString());
						}
						amphiroMeasurementRepository.storeData(((AuthenticatedUser) user).getKey(),
										(AmphiroMeasurementCollection) data);
					}
					break;
				case METER:
					if (data instanceof WaterMeterMeasurementCollection) {
						if (!device.getType().equals(EnumDeviceType.METER)) {
							throw new ApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type", data.getType()
											.toString());
						}

						waterMeterMeasurementRepository.storeData(((WaterMeterDevice) device).getSerial(),
										(WaterMeterMeasurementCollection) data);
					}
					break;
				default:
					break;
			}
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));

			success = false;
		} finally {
			logDataUploadSession(user, device, success);
		}

		return response;
	}

	private void logDataUploadSession(AuthenticatedUser user, Device device, boolean success) {
		try {
			if ((user == null) || (device == null)) {
				return;
			}
			deviceRepository.setLastDataUploadDate(user.getKey(), device.getKey(), new DateTime(), success);
		} catch (Exception ex) {
			// Ignore exceptions
		}

	}
}
