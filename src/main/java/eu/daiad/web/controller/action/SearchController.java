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
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IAmphiroMeasurementRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

@RestController
public class SearchController extends BaseController {

	private static final Log logger = LogFactory.getLog(SearchController.class);

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IAmphiroMeasurementRepository amphiroMeasurementRepository;

	@Autowired
	private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@RequestMapping(value = "/action/meter/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getMeterStatus(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody WaterMeterStatusQuery query) {
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
	public RestResponse getMeterMeasurements(@AuthenticationPrincipal AuthenticatedUser user,
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
	public RestResponse getAmphiroMeasurements(@AuthenticationPrincipal AuthenticatedUser user,
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
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public RestResponse getAmphiroSessions(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AmphiroSessionCollectionQuery query) {
		RestResponse response = new RestResponse();

		try {
			// If user has not administrative permissions or user key is null,
			// use the key of the authenticated user
			if ((query.getUserKey() == null) || (!user.hasRole(EnumRole.ROLE_ADMIN))) {
				query.setUserKey(user.getKey());
			}

			// Check utility access
			if (!user.getKey().equals(query.getUserKey())) {
				AuthenticatedUser deviceOwner = userRepository.getUserByUtilityAndKey(user.getUtilityId(),
								query.getUserKey());
				if (deviceOwner == null) {
					throw new ApplicationException(DeviceErrorCode.DEVICE_ACCESS_DENIED).set("user", user.getKey())
									.set("owner", query.getUserKey());
				}
			}

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
				deviceQuery.setType(EnumDeviceType.AMPHIRO);

				ArrayList<UUID> deviceKeys = new ArrayList<UUID>();

				for (Device d : this.deviceRepository.getUserDevices(query.getUserKey(), deviceQuery)) {
					deviceKeys.add(d.getKey());
				}

				UUID[] deviceKeyArray = new UUID[deviceKeys.size()];

				query.setDeviceKey(deviceKeys.toArray(deviceKeyArray));
			}

			this.checkAmphiroOwnership(query.getUserKey(), query.getDeviceKey());

			return amphiroMeasurementRepository.searchSessions(query);
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/device/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getAmphiroSession(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AmphiroSessionQuery query) {
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
