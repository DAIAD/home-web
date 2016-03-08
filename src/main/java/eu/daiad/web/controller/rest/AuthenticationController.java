package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.data.IProfileRepository;
import eu.daiad.web.model.AuthenticationResponse;
import eu.daiad.web.model.Credentials;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.security.AuthenticationService;

@RestController("RestAuthenticationController")
public class AuthenticationController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(AuthenticationController.class);

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private IProfileRepository profileRepository;

	@RequestMapping(value = "/api/v1/auth/login", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse login(@RequestBody Credentials credentials) throws Exception {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticate(credentials);

			if (user != null) {
				Profile profile = profileRepository.getProfileByUsername(EnumApplication.MOBILE);

				return new AuthenticationResponse(profile);
			} else {
				throw new ApplicationException(SharedErrorCode.AUTHENTICATION);
			}
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}
