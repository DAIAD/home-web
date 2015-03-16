package eu.daiad.web.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.access.annotation.Secured;

import eu.daiad.web.model.*;
import eu.daiad.web.data.hbase.*;

@RestController
public class DataCollectionController {

	private static final int ERROR_PARSING_FAILED = 1;
	private static final int ERROR_UNKNOWN = 2;

	private static final Log logger = LogFactory
			.getLog(DataCollectionController.class);

	@Autowired
	private HbaseConnection hbase;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(
				dateFormat, false));
	}

	@RequestMapping(value = "/api/v1/amphiro", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse insert(@RequestBody MeasurementCollection data,
			BindingResult results) {

		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				return new RestResponse(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				hbase.storeDataAmphiro(data);
				return new RestResponse();
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.");
			logger.error(ex);
		}
		return new RestResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}
	
	@RequestMapping(value = "/api/v1/swm", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse insert(@RequestBody SmartMeterMeasurementCollection data,
			BindingResult results) {

		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				return new RestResponse(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				hbase.storeDataSmartMeter(data);
				return new RestResponse();
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.");
			logger.error(ex);
		}
		return new RestResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}

	@RequestMapping(value = "/swm/current", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("USER")
	public SmartMeterResult query(@RequestBody SmartMeterQuery query,
			BindingResult results) {
		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				return new SmartMeterResult(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				// TODO: Return series, not response object
				SmartMeterResult data = hbase.query(query);

				return data;
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.");
			logger.error(ex);
		}
		return new SmartMeterResult(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}
	
	@RequestMapping(value = "/swm/history", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("USER")
	public SmartMeterCollectionResult query(@RequestBody SmartMeterIntervalQuery query,
			BindingResult results) {
		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				return new SmartMeterCollectionResult(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				// TODO: Return series, not response object
				SmartMeterCollectionResult data = hbase.query(query);

				return data;
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.");
			logger.error(ex);
		}
		return new SmartMeterCollectionResult(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}
	
	@RequestMapping(value = "/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("USER")
	public MeasurementResult query(@RequestBody MeasurementQuery query,
			BindingResult results) {
		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				return new MeasurementResult(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				// TODO: Return series, not response object
				MeasurementResult data = hbase.query(query);

				return data;
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.");
			logger.error(ex);
		}
		return new MeasurementResult(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}

	@RequestMapping(value = "/showers", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("USER")
	public ShowerCollectionResult query(
			@RequestBody ShowerCollectionQuery query, BindingResult results) {
		try {

			if (results.hasErrors()) {
				// TODO: Add logging
				return new ShowerCollectionResult(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				// TODO: Return series, not response object
				ShowerCollectionResult data = hbase.query(query);

				return data;
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.");
			logger.error(ex);
		}
		return new ShowerCollectionResult(ERROR_UNKNOWN,
				"Unhandled exception has occured.");

	}

	@RequestMapping(value = "/shower", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured("USER")
	public ShowerResult query(@RequestBody ShowerQuery query,
			BindingResult results) {
		try {

			if (results.hasErrors()) {
				// TODO: Add logging
				return new ShowerResult(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			} else {
				// TODO: Return series, not response object
				ShowerResult data = hbase.query(query);

				return data;
			}
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.");
			logger.error(ex);
		}
		return new ShowerResult(ERROR_UNKNOWN,
				"Unhandled exception has occured.");

	}
}
