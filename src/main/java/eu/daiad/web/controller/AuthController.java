package eu.daiad.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.Credentials;
import eu.daiad.web.security.Authenticator;

@RestController
public class AuthController {

	private static final int ERROR_PARSING_FAILED = 1;
	
	private static final int ERROR_AUTH_FAILED = 5;
	
	private static final int ERROR_UNKNOWN = 100;

	private static final Log logger = LogFactory
			.getLog(AuthController.class);

    @Autowired
    private Authenticator authenticator;

	@RequestMapping(value = "/api/v1/auth/login", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse login(@RequestBody Credentials data,
			BindingResult results) {

		try {
			if (results.hasErrors()) {
				return new RestResponse(ERROR_PARSING_FAILED,
						"Invalid credentials.");
			} else if(this.authenticator.authenticate(data)) {
		        return new RestResponse();
			} else {
				return new RestResponse(ERROR_AUTH_FAILED,
						"Authentication has failed.");
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
		return new RestResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}

}
