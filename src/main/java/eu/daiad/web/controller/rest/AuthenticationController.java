package eu.daiad.web.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.security.AuthenticationResponse;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.repository.application.IProfileRepository;

@RestController("RestAuthenticationController")
public class AuthenticationController extends BaseRestController {

	@Autowired
	private IProfileRepository profileRepository;

	@RequestMapping(value = "/api/v1/auth/login", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse login(@RequestBody Credentials credentials) throws Exception {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(credentials);

			Profile profile = profileRepository.getProfileByUsername(EnumApplication.MOBILE);

			return new AuthenticationResponse(profile);
		} catch (ApplicationException ex) {
			response.add(this.getError(ex));
		}

		return response;
	}
}
