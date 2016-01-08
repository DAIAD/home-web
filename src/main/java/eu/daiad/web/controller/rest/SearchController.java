package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.AmphiroMeasurementRepository;
import eu.daiad.web.data.WaterMeterMeasurementRepository;
import eu.daiad.web.model.AmphiroMeasurementQuery;
import eu.daiad.web.model.AmphiroSessionCollectionQuery;
import eu.daiad.web.model.AmphiroSessionQuery;
import eu.daiad.web.model.Error;
import eu.daiad.web.model.AmphiroMeasurementQueryResult;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.AmphiroSessionCollectionQueryResult;
import eu.daiad.web.model.AmphiroSessionQueryResult;
import eu.daiad.web.model.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.WaterMeterMeasurementQuery;
import eu.daiad.web.model.WaterMeterStatusQuery;
import eu.daiad.web.model.WaterMeterStatusQueryResult;
import eu.daiad.web.security.AuthenticationService;
import eu.daiad.web.security.model.ApplicationUser;

@RestController("RestSearchController")
public class SearchController {

	private static final Log logger = LogFactory.getLog(SearchController.class);

	@Autowired
	private AmphiroMeasurementRepository amphiroMeasurementRepository;

	@Autowired
	private WaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@Autowired
	private AuthenticationService authenticationService;

	@RequestMapping(value = "/api/v1/meter/status", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getWaterMeterStatus(@RequestBody WaterMeterStatusQuery query) {
		try {
			ApplicationUser user = this.authenticationService
					.authenticateAndGetUser(query.getCredentials());
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed");
			}
			if (!user.hasRole("ROLE_USER")) {
				return new RestResponse(Error.ERROR_FORBIDDEN,
						"Unauthhorized request");
			}

			if ((query.getDeviceKey() == null)
					|| (query.getDeviceKey().length == 0)) {
				return new WaterMeterStatusQueryResult();
			}

			query.setUserKey(user.getKey());

			WaterMeterStatusQueryResult data = waterMeterMeasurementRepository
					.getStatus(query);

			return data;
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");
	}

	@RequestMapping(value = "/api/v1/meter/history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchWaterMeterMeasurements(
			@RequestBody WaterMeterMeasurementQuery query) {
		try {
			ApplicationUser user = this.authenticationService
					.authenticateAndGetUser(query.getCredentials());
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed.");
			}
			if (!user.hasRole("ROLE_USER")) {
				return new RestResponse(Error.ERROR_FORBIDDEN,
						"Unauthhorized request.");
			}

			query.setUserKey(user.getKey());

			WaterMeterMeasurementQueryResult data = waterMeterMeasurementRepository
					.searchMeasurements(query);

			return data;
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");
	}

	@RequestMapping(value = "/api/v1/device/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroMeasurements(
			@RequestBody AmphiroMeasurementQuery query) {
		try {
			ApplicationUser user = this.authenticationService
					.authenticateAndGetUser(query.getCredentials());
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed.");
			}
			if (!user.hasRole("ROLE_USER")) {
				return new RestResponse(Error.ERROR_FORBIDDEN,
						"Unauthhorized request.");
			}

			query.setUserKey(user.getKey());

			AmphiroMeasurementQueryResult data = amphiroMeasurementRepository
					.searchMeasurements(query);

			return data;
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new AmphiroMeasurementQueryResult(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");
	}

	@RequestMapping(value = "/api/v1/device/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse searchAmphiroSessions(
			@RequestBody AmphiroSessionCollectionQuery query) {
		try {
			ApplicationUser user = this.authenticationService
					.authenticateAndGetUser(query.getCredentials());
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed.");
			}
			if (!user.hasRole("ROLE_USER")) {
				return new RestResponse(Error.ERROR_FORBIDDEN,
						"Unauthhorized request.");
			}

			query.setUserKey(user.getKey());

			AmphiroSessionCollectionQueryResult data = amphiroMeasurementRepository
					.searchSessions(query);

			return data;
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new AmphiroSessionCollectionQueryResult(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");

	}

	@RequestMapping(value = "/api/v1/device/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getAmphiroSession(@RequestBody AmphiroSessionQuery query) {
		try {
			ApplicationUser user = this.authenticationService
					.authenticateAndGetUser(query.getCredentials());
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed.");
			}
			if (!user.hasRole("ROLE_USER")) {
				return new RestResponse(Error.ERROR_FORBIDDEN,
						"Unauthhorized request.");
			}

			query.setUserKey(user.getKey());

			AmphiroSessionQueryResult data = amphiroMeasurementRepository.getSession(query);

			return data;
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new AmphiroSessionQueryResult(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");

	}
}
