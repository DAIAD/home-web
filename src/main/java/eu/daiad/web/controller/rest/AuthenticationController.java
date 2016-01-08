package eu.daiad.web.controller.rest;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;

import eu.daiad.web.model.Credentials;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.security.AuthenticationService;
import eu.daiad.web.security.model.ApplicationUser;
import eu.daiad.web.security.model.AuthenticationResponse;
import eu.daiad.web.model.Error;

@RestController("RestAuthenticationController")
public class AuthenticationController {

	@Autowired
	private AuthenticationService authenticationService;

	@RequestMapping(value = "/api/v1/auth/login", 
					method = RequestMethod.POST, 
					consumes = "application/json", 
					produces = "application/json")
	public RestResponse login(@RequestBody Credentials data) {

		ApplicationUser user = this.authenticationService.authenticateAndGetUser(data);
		
		if (user != null) {
			return new AuthenticationResponse(user.getKey().toString());
		} else {
			return new RestResponse(Error.ERROR_AUTH_FAILED, "Authentication has failed");
		}
	}

}
