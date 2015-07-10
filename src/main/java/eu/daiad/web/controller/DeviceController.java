package eu.daiad.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;

import eu.daiad.web.data.DeviceRepository;
import eu.daiad.web.model.Device;
import eu.daiad.web.model.DeviceRegistrationRequest;
import eu.daiad.web.model.DeviceRegistrationResponse;
import eu.daiad.web.security.Authenticator;
import eu.daiad.web.security.model.DaiadUser;

@RestController
public class DeviceController {

	private static final int ERROR_PARSING_FAILED = 1;
	private static final int ERROR_AUTH_FAILED = 2;
	private static final int ERROR_DEVICE_EXISTS = 3;
	
	private static final int ERROR_UNKNOWN = 100;

	private static final Log logger = LogFactory
			.getLog(DeviceController.class);

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private DeviceRepository repository;
    
	@RequestMapping(value = "/api/v1/device/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public DeviceRegistrationResponse register(@RequestBody DeviceRegistrationRequest data,
			BindingResult results) {

		try {
			if (results.hasErrors()) {
				return new DeviceRegistrationResponse(ERROR_PARSING_FAILED,
						"Invalid request.");
			}
			
			DaiadUser user = this.authenticator.authenticateAndGetUser(data.getCredentials());
			if(user == null) {
				return new DeviceRegistrationResponse(ERROR_AUTH_FAILED,
						"Authentication has failed.");
			} 
			
			Device device = repository.getUserDeviceById(data.getDeviceId(),  user.getKey());
			if(device != null) {
				return new DeviceRegistrationResponse(ERROR_DEVICE_EXISTS,
						String.format("Device [%s] already exists.", data.getDeviceId()));
			}
			
			device = repository.createAmphiroDevice(data.getDeviceId(),
										   		    user.getKey(),
										   		    data.getName(),
										   		    data.getProperties());
			
			DeviceRegistrationResponse response =  new DeviceRegistrationResponse();
	        response.setDeviceKey(device.getKey().toString());
		        
	        return response;
		} catch (Exception ex) {
			logger.error("Unhandled exception has occurred.", ex);
		}
		return new DeviceRegistrationResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}
}
