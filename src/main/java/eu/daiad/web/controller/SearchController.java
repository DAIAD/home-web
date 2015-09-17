package eu.daiad.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.access.annotation.Secured;

import eu.daiad.web.model.*;
import eu.daiad.web.security.AuthenticationService;
import eu.daiad.web.data.*;

@RestController
public class SearchController {

	private static final int ERROR_PARSING_FAILED = 1;
	private static final int ERROR_UNKNOWN = 3;

	private static final Log logger = LogFactory.getLog(SearchController.class);

	@Autowired
	private MeasurementRepository measurementRepository;

	@Autowired
	private AuthenticationService authenticator;

	@RequestMapping(value = "/action/meter/current", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public SmartMeterStatusCollectionResult query(@RequestBody SmartMeterQuery query,
			BindingResult results) {
		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				return new SmartMeterStatusCollectionResult(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				// TODO: Return series, not response object
				SmartMeterStatusCollectionResult data = measurementRepository.query(query);

				return data;
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new SmartMeterStatusCollectionResult(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}

	@RequestMapping(value = "/action/meter/history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public SmartMeterDataSeriesCollectionResult query(
			@RequestBody SmartMeterIntervalQuery query, BindingResult results) {
		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				return new SmartMeterDataSeriesCollectionResult(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				// TODO: Return series, not response object
				SmartMeterDataSeriesCollectionResult data = measurementRepository.query(query);

				return data;
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new SmartMeterDataSeriesCollectionResult(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}

	@RequestMapping(value = "/action/device/measurement/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public MeasurementResult query(@RequestBody MeasurementQuery query,
			BindingResult results) {
		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				return new MeasurementResult(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				// TODO: Return series, not response object
				MeasurementResult data = measurementRepository.query(query);

				return data;
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new MeasurementResult(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}

	@RequestMapping(value = "/action/device/session/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public SessionCollectionResult searchAmphiroSessions(
			@RequestBody ShowerCollectionQuery query, BindingResult results) {
		try {

			if (results.hasErrors()) {
				// TODO: Add logging
				return new SessionCollectionResult(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				// TODO: Return series, not response object
				SessionCollectionResult data = measurementRepository.searchAmphiroSessions(query);

				return data;
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new SessionCollectionResult(ERROR_UNKNOWN,
				"Unhandled exception has occured.");

	}

	@RequestMapping(value = "/action/device/session", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("ROLE_USER")
	public ShowerResult query(@RequestBody ShowerQuery query,
			BindingResult results) {
		try {

			if (results.hasErrors()) {
				// TODO: Add logging
				return new ShowerResult(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				// TODO: Return series, not response object
				ShowerResult data = measurementRepository.getAmphiroSession(query);

				return data;
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);
		}
		return new ShowerResult(ERROR_UNKNOWN,
				"Unhandled exception has occured.");

	}
}
