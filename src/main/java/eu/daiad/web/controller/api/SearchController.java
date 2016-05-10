package eu.daiad.web.controller.api;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionTimeIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionIndexIntervalQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionTimeIntervalQueryResult;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatus;
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IAmphiroTimeOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

@RestController("RestSearchController")
public class SearchController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(SearchController.class);

	@Autowired
	private IDeviceRepository deviceRepository;

	@Autowired
	private IAmphiroTimeOrderedRepository amphiroTimeOrderedRepository;

	@Autowired
	private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

	@Autowired
	private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@RequestMapping(value = "/api/v1/meter/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getWaterMeterStatus(@RequestBody WaterMeterStatusQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new WaterMeterStatusQueryResult();
			}

			String[] serials = this.checkMeterOwnership(user.getKey(), query.getDeviceKey());

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
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/meter/history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchWaterMeterMeasurements(@RequestBody WaterMeterMeasurementQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new WaterMeterMeasurementQueryResult();
			}

			String[] serials = this.checkMeterOwnership(user.getKey(), query.getDeviceKey());

			WaterMeterMeasurementQueryResult data = waterMeterMeasurementRepository.searchMeasurements(serials,
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/device/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroMeasurements1(@RequestBody AmphiroMeasurementTimeIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new WaterMeterStatusQueryResult();
			}

			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroMeasurementTimeIntervalQueryResult data = amphiroTimeOrderedRepository.searchMeasurements(
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v2/device/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroMeasurements2(@RequestBody AmphiroMeasurementIndexIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new WaterMeterStatusQueryResult();
			}

			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroMeasurementIndexIntervalQueryResult data = amphiroIndexOrderedRepository.searchMeasurements(
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/device/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroSessions1(@RequestBody AmphiroSessionCollectionTimeIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new WaterMeterStatusQueryResult();
			}

			String[] names = this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroSessionCollectionTimeIntervalQueryResult data = amphiroTimeOrderedRepository.searchSessions(names,
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v2/device/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroSessions2(@RequestBody AmphiroSessionCollectionIndexIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if ((query.getDeviceKey() == null) || (query.getDeviceKey().length == 0)) {
				return new WaterMeterStatusQueryResult();
			}

			String[] names = this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroSessionCollectionIndexIntervalQueryResult data = amphiroIndexOrderedRepository.searchSessions(names,
							DateTimeZone.forID(user.getTimezone()), query);

			return data;
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/device/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getAmphiroSession1(@RequestBody AmphiroSessionTimeIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if (query.getDeviceKey() == null) {
				return new WaterMeterStatusQueryResult();
			}

			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroSessionTimeIntervalQueryResult data = amphiroTimeOrderedRepository.getSession(query);

			return data;
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v2/device/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getAmphiroSession2(@RequestBody AmphiroSessionIndexIntervalQuery query) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(query.getCredentials(), EnumRole.ROLE_USER);

			query.setUserKey(user.getKey());

			if (query.getDeviceKey() == null) {
				return new WaterMeterStatusQueryResult();
			}

			this.checkAmphiroOwnership(user.getKey(), query.getDeviceKey());

			AmphiroSessionIndexIntervalQueryResult data = amphiroIndexOrderedRepository.getSession(query);

			return data;
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

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

	private String[] checkAmphiroOwnership(UUID userKey, UUID[] devices) {
		ArrayList<String> nameList = new ArrayList<String>();

		if (devices != null) {
			for (UUID deviceKey : devices) {
				Device device = this.deviceRepository.getUserDeviceByKey(userKey, deviceKey);

				if (device == null) {
					throw new ApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
				}

				nameList.add(((AmphiroDevice) device).getName());
			}
		}

		String[] nameArray = new String[nameList.size()];

		return nameList.toArray(nameArray);
	}

	private String[] checkMeterOwnership(UUID userKey, UUID[] devices) {
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
