package eu.daiad.web.controller.action;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionQuery;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IAmphiroMeasurementRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;
import eu.daiad.web.security.AuthenticationService;

@RestController
public class SearchController extends BaseController {

	private static final Log logger = LogFactory.getLog(SearchController.class);

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IAmphiroMeasurementRepository amphiroMeasurementRepository;

	@Autowired
	private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@Autowired
	private AuthenticationService authenticator;

	@RequestMapping(value = "/action/meter/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse query(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody WaterMeterStatusQuery query) {
		RestResponse response = new RestResponse();

		try {
			String[] serials = this.checkMeterOwnership(user.getKey(), query.getDeviceKey());

			return waterMeterMeasurementRepository.getStatus(serials, query);
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/meter/history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse query(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody WaterMeterMeasurementQuery query) {
		RestResponse response = new RestResponse();

		try {
			String[] serials = this.checkMeterOwnership(user.getKey(), query.getDeviceKey());

			return waterMeterMeasurementRepository.searchMeasurements(serials, query);
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/device/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse query(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AmphiroMeasurementQuery query) {
		RestResponse response = new RestResponse();

		try {
			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			query.setUserKey(user.getKey());

			AmphiroMeasurementQueryResult data = amphiroMeasurementRepository.searchMeasurements(query);

			return data;
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/device/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse searchAmphiroSessions(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AmphiroSessionCollectionQuery query) {
		RestResponse response = new RestResponse();

		try {
			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			query.setUserKey(user.getKey());

			return amphiroMeasurementRepository.searchSessions(query);
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/device/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse query(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody AmphiroSessionQuery query) {
		RestResponse response = new RestResponse();

		try {
			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			query.setUserKey(user.getKey());

			return amphiroMeasurementRepository.getSession(query);
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	private void checkAmphiroOwnership(UUID userKey, UUID deviceKey) {
		if (deviceKey != null) {
			this.checkAmphiroOwnership(userKey, new UUID[] { deviceKey });
		}
	}

	private void checkAmphiroOwnership(UUID userKey, UUID devices[]) {
		if (devices != null) {
			for (UUID deviceKey : devices) {
				Device device = this.deviceRepository.getUserDeviceByKey(userKey, deviceKey);

				if (device == null) {
					throw new ApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
				}
			}
		}
	}

	private String[] checkMeterOwnership(UUID userKey, UUID devices[]) {
		ArrayList<String> serialList = new ArrayList<String>();

		if (devices != null) {
			for (UUID deviceKey : devices) {
				Device device = this.deviceRepository.getUserDeviceByKey(userKey, deviceKey);

				if (device == null) {
					throw new ApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
				}

				serialList.add(((WaterMeterDevice) device).getSerial());
			}
		}

		String[] serialArray = new String[serialList.size()];

		return serialList.toArray(serialArray);
	}
}
