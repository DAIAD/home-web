package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.NotifyProfileRequest;
import eu.daiad.web.model.profile.ProfileResponse;
import eu.daiad.web.model.profile.UpdateProfileRequest;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IProfileRepository;

@RestController("RestProfileController")
public class ProfileController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(ProfileController.class);

	@Autowired
	private IProfileRepository profileRepository;

	@RequestMapping(value = "/api/v1/profile/load", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse loadProfile(@RequestBody Credentials data) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data, EnumRole.ROLE_USER);

			return new ProfileResponse(this.profileRepository.getProfileByUsername(EnumApplication.MOBILE));
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/profile/save", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse saveProfile(@RequestBody UpdateProfileRequest request) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(request.getCredentials(), EnumRole.ROLE_USER);

			this.profileRepository.setProfileConfiguration(EnumApplication.MOBILE, request.getConfiguration());

		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/profile/notify", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse saveProfile(@RequestBody NotifyProfileRequest request) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(request.getCredentials(), EnumRole.ROLE_USER);

			this.profileRepository.notifyProfile(EnumApplication.MOBILE, request.getVersion(), request.getUpdatedOn());

		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}
