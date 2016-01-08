package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.AmphiroMeasurementRepository;
import eu.daiad.web.data.WaterMeterMeasurementRepository;
import eu.daiad.web.model.Error;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQuery;
import eu.daiad.web.model.amphiro.AmphiroMeasurementQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionQueryResult;
import eu.daiad.web.model.amphiro.AmphiroSessionQuery;
import eu.daiad.web.model.amphiro.AmphiroSessionQueryResult;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatusQuery;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.security.AuthenticationService;

@RestController
public class SearchController {

	private static final Log logger = LogFactory.getLog(SearchController.class);

	@Autowired
	private AmphiroMeasurementRepository amphiroMeasurementRepository;

	@Autowired
	private WaterMeterMeasurementRepository waterMeterMeasurementRepository;

	@Autowired
	private AuthenticationService authenticator;

	@RequestMapping(value = "/action/meter/current", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse query(@RequestBody WaterMeterStatusQuery query) {
		try {
			WaterMeterStatusQueryResult data = waterMeterMeasurementRepository
					.getStatus(query);

			return data;
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");
	}

	@RequestMapping(value = "/action/meter/history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse query(@RequestBody WaterMeterMeasurementQuery query) {
		try {
			WaterMeterMeasurementQueryResult data = waterMeterMeasurementRepository
					.searchMeasurements(query);

			return data;
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");
	}

	@RequestMapping(value = "/action/device/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse query(@RequestBody AmphiroMeasurementQuery query) {
		try {
			// TODO: Return series, not response object
			AmphiroMeasurementQueryResult data = amphiroMeasurementRepository.searchMeasurements(query);

			return data;
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");
	}

	@RequestMapping(value = "/action/device/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse searchAmphiroSessions(
			@RequestBody AmphiroSessionCollectionQuery query) {
		try {
			// TODO: Return series, not response object
			AmphiroSessionCollectionQueryResult data = amphiroMeasurementRepository
					.searchSessions(query);

			return data;
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");

	}

	@RequestMapping(value = "/action/device/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse query(@RequestBody AmphiroSessionQuery query) {
		try {
			// TODO: Return series, not response object
			AmphiroSessionQueryResult data = amphiroMeasurementRepository.getSession(query);

			return data;
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");

	}
}
