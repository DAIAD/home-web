package eu.daiad.web.controller.action;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQuery;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterStatus;
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IAmphiroTimeOrderedRepository;
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
	private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

	@Autowired
	private IAmphiroTimeOrderedRepository amphiroTimeOrderedRepository;

	@Autowired
	private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@RequestMapping(value = "/action/meter/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public RestResponse getMeterStatus(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody WaterMeterStatusQuery query) {
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
					throw createApplicationException(DeviceErrorCode.DEVICE_ACCESS_DENIED).set("user", user.getKey())
									.set("owner", query.getUserKey());
				}
			}

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
				deviceQuery.setType(EnumDeviceType.METER);

				ArrayList<UUID> deviceKeys = new ArrayList<UUID>();

				for (Device d : this.deviceRepository.getUserDevices(query.getUserKey(), deviceQuery)) {
					deviceKeys.add(d.getKey());
				}

				UUID[] deviceKeyArray = new UUID[deviceKeys.size()];

				query.setDeviceKey(deviceKeys.toArray(deviceKeyArray));
			}

			String[] serials = this.checkMeterOwnership(query.getUserKey(), query.getDeviceKey());

			WaterMeterStatusQueryResult result = waterMeterMeasurementRepository.getStatus(serials);

			for (WaterMeterStatus status : result.getDevices()) {
				for (int i = 0, count = serials.length; i < count; i++) {
					if (status.getSerial().equals(serials[i])) {
						status.setDeviceKey(query.getDeviceKey()[i]);
						break;
					}
				}
			}

			return result;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/meter/history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public RestResponse getMeterMeasurements(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody WaterMeterMeasurementQuery query) {
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
					throw createApplicationException(DeviceErrorCode.DEVICE_ACCESS_DENIED).set("user", user.getKey())
									.set("owner", query.getUserKey());
				}
			}

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
				deviceQuery.setType(EnumDeviceType.METER);

				ArrayList<UUID> deviceKeys = new ArrayList<UUID>();

				for (Device d : this.deviceRepository.getUserDevices(query.getUserKey(), deviceQuery)) {
					deviceKeys.add(d.getKey());
				}

				UUID[] deviceKeyArray = new UUID[deviceKeys.size()];

				query.setDeviceKey(deviceKeys.toArray(deviceKeyArray));
			}

			String[] serials = this.checkMeterOwnership(query.getUserKey(), query.getDeviceKey());

			return waterMeterMeasurementRepository.searchMeasurements(serials, DateTimeZone.forID(user.getTimezone()),
							query);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/device/index/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getAmphiroMeasurements1(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AmphiroMeasurementIndexIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			query.setUserKey(user.getKey());

			AmphiroMeasurementIndexIntervalQueryResult data = amphiroIndexOrderedRepository.searchMeasurements(
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/device/time/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getAmphiroMeasurements2(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AmphiroMeasurementTimeIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			query.setUserKey(user.getKey());

			AmphiroMeasurementTimeIntervalQueryResult data = amphiroTimeOrderedRepository.searchMeasurements(
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/device/index/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public RestResponse getAmphiroSessions1(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AmphiroSessionCollectionIndexIntervalQuery query) {
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
					throw createApplicationException(DeviceErrorCode.DEVICE_ACCESS_DENIED).set("user", user.getKey())
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

			String[] names = this.checkAmphiroOwnership(query.getUserKey(), query.getDeviceKey());

			return amphiroIndexOrderedRepository.searchSessions(names, DateTimeZone.forID(user.getTimezone()), query);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/device/time/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({ "ROLE_USER", "ROLE_ADMIN" })
	public RestResponse getAmphiroSessions2(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AmphiroSessionCollectionTimeIntervalQuery query) {
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
					throw createApplicationException(DeviceErrorCode.DEVICE_ACCESS_DENIED).set("user", user.getKey())
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

			String[] names = this.checkAmphiroOwnership(query.getUserKey(), query.getDeviceKey());

			return amphiroTimeOrderedRepository.searchSessions(names, DateTimeZone.forID(user.getTimezone()), query);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/device/index/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getAmphiroSession1(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AmphiroSessionIndexIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			query.setUserKey(user.getKey());

			return amphiroIndexOrderedRepository.getSession(query);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/device/time/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getAmphiroSession2(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AmphiroSessionTimeIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			query.setUserKey(user.getKey());

			return amphiroTimeOrderedRepository.getSession(query);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	private String[] checkAmphiroOwnership(UUID userKey, UUID deviceKey) {
		if (deviceKey != null) {
			return this.checkAmphiroOwnership(userKey, new UUID[] { deviceKey });
		}

		return new String[] { null };
	}

	private String[] checkAmphiroOwnership(UUID userKey, UUID devices[]) {
		ArrayList<String> nameList = new ArrayList<String>();

		if (devices != null) {
			for (UUID deviceKey : devices) {
				Device device = this.deviceRepository.getUserDeviceByKey(userKey, deviceKey);

				if (device == null) {
					throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
				}

				nameList.add(((AmphiroDevice) device).getName());
			}
		}

		String[] nameArray = new String[nameList.size()];

		return nameList.toArray(nameArray);
	}

	private String[] checkMeterOwnership(UUID userKey, UUID devices[]) {
		ArrayList<String> serialList = new ArrayList<String>();

		if (devices != null) {
			for (UUID deviceKey : devices) {
				Device device = this.deviceRepository.getUserDeviceByKey(userKey, deviceKey);

				if (device == null) {
					throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
				}

				serialList.add(((WaterMeterDevice) device).getSerial());
			}
		}

		String[] serialArray = new String[serialList.size()];

		return serialList.toArray(serialArray);
	}
}
