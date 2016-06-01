package eu.daiad.web.controller.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.profile.NotifyProfileRequest;
import eu.daiad.web.model.profile.ProfileResponse;
import eu.daiad.web.model.profile.UpdateProfileRequest;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IProfileRepository;

/**
 * 
 * Provides actions for loading and updating user profile.
 *
 */
@RestController("RestProfileController")
public class ProfileController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(ProfileController.class);

	@Autowired
	private IProfileRepository profileRepository;

	/**
	 * Loads user profile data.
	 * 
	 * @param data user credentials.
	 * @return the user profile.
	 */
	@RequestMapping(value = "/api/v1/profile/load", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse loadProfile(@RequestBody Credentials data) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data, EnumRole.ROLE_USER);

			return new ProfileResponse(this.getRuntime(),
							this.profileRepository.getProfileByUsername(EnumApplication.MOBILE));
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Saves client application specific information e.g. the web application layout to the server.
	 * 
	 * @param request the profile data to store
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/api/v1/profile/save", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse saveProfile(@RequestBody UpdateProfileRequest request) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(request.getCredentials(), EnumRole.ROLE_USER);

			this.profileRepository.setProfileConfiguration(EnumApplication.MOBILE, request.getConfiguration());

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Updates user profile that a specific application configuration version has been applied to 
	 * the mobile client.
	 * 
	 * @param request the notification request.
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/api/v1/profile/notify", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse notifyProfile(@RequestBody NotifyProfileRequest request) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(request.getCredentials(), EnumRole.ROLE_USER);

			this.profileRepository.notifyProfile(EnumApplication.MOBILE, request.getVersion(),
							new DateTime(request.getUpdatedOn()));

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}
