package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.ProfileRepository;
import eu.daiad.web.model.Error;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.profile.ViewProfileResponse;
import eu.daiad.web.security.model.ApplicationUser;

@RestController
public class ProfileController {

	private static final Log logger = LogFactory
			.getLog(ProfileController.class);

	@Autowired
	private ProfileRepository profileRepository;

	@RequestMapping(value = "/action/profile", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getProfile() {
		try {
			ApplicationUser user = (ApplicationUser) SecurityContextHolder
					.getContext().getAuthentication().getPrincipal();

			ViewProfileResponse response = new ViewProfileResponse();

			response.setProfile(profileRepository.getProfileByUsername(user
					.getUsername()));

			return response;
		} catch (Exception ex) {
			logger.error("Failed to load profile.", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred.");
	}

}
