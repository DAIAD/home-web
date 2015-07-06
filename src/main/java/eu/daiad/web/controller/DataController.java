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

import eu.daiad.web.model.*;
import eu.daiad.web.security.Authenticator;
import eu.daiad.web.security.model.DaiadUser;
import eu.daiad.web.data.*;

@RestController
public class DataController {

	private static final int ERROR_PARSING_FAILED = 1;
	private static final int ERROR_AUTH_FAILED = 2;
	private static final int ERROR_UNKNOWN = 3;

	private static final Log logger = LogFactory
			.getLog(DataController.class);

	@Autowired
	private MeasurementRepository storage;
	
	@Autowired
	private DeviceRepository devices;

    @Autowired
    private Authenticator authenticator;
    
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(
				dateFormat, false));
	}

	@RequestMapping(value = "/api/v1/data/storage", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse insert(@RequestBody MeasurementCollection data,
			BindingResult results) {

		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				return new RestResponse(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			}
			
			DaiadUser user = this.authenticator.authenticateAndGetUser(data.getCredentials());
			if(user == null) {
				return new RestResponse(ERROR_AUTH_FAILED,
							"Authentication has failed.");
			}
			
			switch(data.getType()) {
				case AMPHIRO:
					if(data instanceof DeviceMeasurementCollection) {
						DeviceMeasurementCollection deviceData = (DeviceMeasurementCollection) data;
						
						Device device = this.devices.getUserDeviceByKey(deviceData.getDeviceKey(), data.getUserKey());
						if(device == null) {
							return new RestResponse(ERROR_AUTH_FAILED,
									"User or device does not exist.");
						}
						storage.storeDataAmphiro((DeviceMeasurementCollection) data);
					}
					break;
				case METER:
					if(data instanceof MeterMeasurementCollection) {
						storage.storeDataSmartMeter((MeterMeasurementCollection) data);
					}
					break;
				default:
					break;
			}
				
			return new RestResponse();
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.");
			logger.error(ex);
		}
		return new RestResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}
}
