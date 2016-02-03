package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.data.IProfileRepository;
import eu.daiad.web.model.Credentials;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.profile.UpdateProfileRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.security.AuthenticationService;

@RestController("RestProfileController")
public class ProfileController extends BaseController {

	private static final Log logger = LogFactory.getLog(ProfileController.class);

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private IProfileRepository profileRepository;

	@RequestMapping(value = "/api/v1/profile/load", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse loadProfile(@RequestBody Credentials data) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticationService.authenticateAndGetUser(data);
			if (user == null) {
				throw new ApplicationException(SharedErrorCode.AUTHENTICATION);
			}

			return new RestResponse();
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/profile/save", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse saveProfile(@RequestBody UpdateProfileRequest data) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticationService.authenticateAndGetUser(data.getCredentials());
			if (user == null) {
				throw new ApplicationException(SharedErrorCode.AUTHENTICATION);
			}

			return new RestResponse();
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

}
