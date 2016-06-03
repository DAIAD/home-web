package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.profile.ProfileDeactivateRequest;
import eu.daiad.web.model.profile.ProfileModesFilterOptionsResponse;
import eu.daiad.web.model.profile.ProfileModesRequest;
import eu.daiad.web.model.profile.ProfileModesResponse;
import eu.daiad.web.model.profile.ProfileModesSubmitChangesRequest;
import eu.daiad.web.model.profile.ProfileResponse;
import eu.daiad.web.model.profile.UpdateProfileRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IProfileRepository;

/**
 * Provides methods for managing user profile.
 *
 */
@RestController
public class ProfileController extends BaseController {

	private static final Log logger = LogFactory.getLog(ProfileController.class);

	@Autowired
	private IProfileRepository profileRepository;

	@RequestMapping(value = "/action/profile/load", method = RequestMethod.GET, produces = "application/json")
	@Secured({ "ROLE_USER", "ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getProfile(@AuthenticationPrincipal AuthenticatedUser user) {
		RestResponse response = new RestResponse();

		try {
			if (user.hasRole("ROLE_ADMIN")) {
				return new ProfileResponse(this.getRuntime(),
								profileRepository.getProfileByUsername(EnumApplication.UTILITY));
			}

			return new ProfileResponse(this.getRuntime(), profileRepository.getProfileByUsername(EnumApplication.HOME));
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/profile/modes/list", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getProfileModes(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody ProfileModesRequest filters) throws JsonProcessingException {
		RestResponse response = new RestResponse();

		try {
			return new ProfileModesResponse(profileRepository.getProfileModes(filters));
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/profile/modes/filter/options", method = RequestMethod.GET, produces = "application/json")
	@Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getFilterOptions(@AuthenticationPrincipal AuthenticatedUser user)
					throws JsonProcessingException {
		RestResponse response = new RestResponse();

		try {
			return new ProfileModesFilterOptionsResponse(profileRepository.getFilterOptions());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/profile/modes/save", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse saveModeChanges(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody ProfileModesSubmitChangesRequest modeChanges) throws JsonProcessingException {
		RestResponse response = new RestResponse();

		try {
			profileRepository.setProfileModes(modeChanges);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Deactivates a user.
	 * 
	 * @param user
	 *            the authenticated user.
	 * @param userDeactId
	 *            the user to deactivate
	 * @return the controller's response.
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = "/action/profile/deactivate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse deactivateProfile(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody ProfileDeactivateRequest userDeactId) {
		RestResponse response = new RestResponse();

		try {
			profileRepository.deactivateProfile(userDeactId);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/profile/save", method = RequestMethod.POST, produces = "application/json")
	@Secured({ "ROLE_USER", "ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse saveProfile(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody UpdateProfileRequest request) {
		RestResponse response = new RestResponse();

		try {
			if (user.hasRole("ROLE_ADMIN")) {
				request.setApplication(EnumApplication.UTILITY);
				this.profileRepository.saveProfile(request);
			} else {
				request.setApplication(EnumApplication.HOME);
				this.profileRepository.saveProfile(request);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}
