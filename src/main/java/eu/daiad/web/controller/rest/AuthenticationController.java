package eu.daiad.web.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.IProfileRepository;
import eu.daiad.web.model.AuthenticationResponse;
import eu.daiad.web.model.Credentials;
import eu.daiad.web.model.Error;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.security.AuthenticationService;

@RestController("RestAuthenticationController")
public class AuthenticationController {

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private IProfileRepository profileRepository;

	@RequestMapping(value = "/api/v1/auth/login", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse login(@RequestBody Credentials data) throws Exception {
		AuthenticatedUser user = this.authenticationService.authenticateAndGetUser(data);

		if (user != null) {
			Profile profile = profileRepository.getProfileByUsername(user.getUsername());

			return new AuthenticationResponse(profile);
		} else {
			return new RestResponse(Error.ERROR_AUTHENTICATION, "Authentication has failed");
		}
	}

}
