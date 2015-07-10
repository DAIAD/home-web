package eu.daiad.web.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import eu.daiad.web.model.*;
import eu.daiad.web.security.Authenticator;
import eu.daiad.web.security.model.DaiadUser;
import eu.daiad.web.data.*;

@RestController
public class DataRestApiController {

	private static final int ERROR_PARSING_FAILED = 1;
	private static final int ERROR_AUTH_FAILED = 2;
	private static final int ERROR_TYPE_NOT_SUPPORTED = 3;
	
	private static final int ERROR_UNKNOWN = 100;

	private static final Log logger = LogFactory
			.getLog(DataRestApiController.class);

	@Value("${tmp.folder}")
	private String temporaryPath;
	
	@Autowired
	private MeasurementRepository measurementRepository;
	
	@Autowired
	private ExportService exportService;
	
	@Autowired
	private DeviceRepository deviceRepository;

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
						
						Device device = this.deviceRepository.getUserDeviceByKey(deviceData.getDeviceKey(), data.getUserKey());
						if(device == null) {
							return new RestResponse(ERROR_AUTH_FAILED,
									"User or device does not exist.");
						}
						measurementRepository.storeDataAmphiro((DaiadUser) user, (AmphiroDevice) device, (DeviceMeasurementCollection) data);
					}
					break;
				case METER:
					if(data instanceof MeterMeasurementCollection) {
						measurementRepository.storeDataSmartMeter((MeterMeasurementCollection) data);
					}
					break;
				default:
					break;
			}
				
			return new RestResponse();
		} catch (Exception ex) {
			logger.error("Failed to insert measurement data.", ex);

		}
		return new RestResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}
	
	@RequestMapping(value = "/api/v1/data/export", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public DownloadResponse export(@RequestBody ExportData data,
			BindingResult results) {

		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				return new DownloadResponse(ERROR_PARSING_FAILED,
						"Input parsing has failed.");
			}
			
			DaiadUser user = this.authenticator.authenticateAndGetUser(data.getCredentials());
			if(user == null) {
				return new DownloadResponse(ERROR_AUTH_FAILED,
							"Authentication has failed.");
			}
			
			switch(data.getType()) {
				case SESSION:
					String token = this.exportService.export(data);
		    		
		    		// Create response				
					return new DownloadResponse(token.toString());
				default:
					break;
			}
				
			return new DownloadResponse(ERROR_TYPE_NOT_SUPPORTED,
					String.format("Export type [%s] is not supported.",  data.getType().toString()));
		} catch (Exception ex) {
			logger.error("Failed to export measurement data.", ex);
		}
		return new DownloadResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}

	@RequestMapping(value = "/api/v1/data/download", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<InputStreamResource> download(@RequestBody DownloadFileRequest data,
			BindingResult results) throws ResourceNotFoundException, AccessDeniedException {
		try {
			if (results.hasErrors()) {
				// TODO: Add logging
				throw new HttpMessageNotReadableException("Input parsing has failed.");
			}
			
			DaiadUser user = this.authenticator.authenticateAndGetUser(data.getCredentials());
			if(user == null) {
				throw new AccessDeniedException("Authentication has failed.");
			}
			
			File path = new File(temporaryPath);
			
			File file = new File(path, data.getToken() + ".zip");
			
			if(file.exists()) {
				FileSystemResource fileResource = new FileSystemResource(file);
			
			    HttpHeaders headers = new HttpHeaders();
			    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			    headers.add("Pragma", "no-cache");
			    headers.add("Expires", "0");
			    		    
			    return ResponseEntity
			            .ok()
			            .headers(headers)
			            .contentLength(fileResource.contentLength())
			            .contentType(MediaType.parseMediaType("application/zip"))
			            .body(new InputStreamResource(fileResource.getInputStream()));
			}
			
		} catch (HttpMessageNotReadableException ex) {
			throw ex;
		} catch (AccessDeniedException ex) {
			throw ex;
		} catch (Exception ex) {
			logger.error(String.format("File [%s] was not found.", data.getToken()), ex);
		}
		
		throw new ResourceNotFoundException();
	}	
}
