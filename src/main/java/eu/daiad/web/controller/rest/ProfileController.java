package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.ProfileRepository;
import eu.daiad.web.model.Credentials;
import eu.daiad.web.model.Error;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.UpdateProfileRequest;
import eu.daiad.web.model.ViewProfileResponse;
import eu.daiad.web.security.AuthenticationService;
import eu.daiad.web.security.model.ApplicationUser;

@RestController("RestProfileController")
public class ProfileController {

	private static final Log logger = LogFactory
			.getLog(ProfileController.class);

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ProfileRepository profileRepository;

	@RequestMapping(value = "/api/v1/profile/load", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse loadProfile(@RequestBody Credentials data) {
		try {
			ApplicationUser user = this.authenticationService
					.authenticateAndGetUser(data);
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed");
			}

			return new RestResponse();
		} catch (Exception ex) {
			logger.error("Failed to load profile", ex);
		}
		return new ViewProfileResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");
	}

	@RequestMapping(value = "/api/v1/profile/save", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse saveProfile(@RequestBody UpdateProfileRequest data) {
		try {
			ApplicationUser user = this.authenticationService
					.authenticateAndGetUser(data.getCredentials());
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTH_FAILED,
						"Authentication has failed");
			}

			return new RestResponse();
		} catch (Exception ex) {
			logger.error("Failed to save profile", ex);
		}
		return new ViewProfileResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred");
	}

}
