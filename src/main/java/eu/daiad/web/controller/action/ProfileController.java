package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.daiad.web.model.*;
import eu.daiad.web.model.Error;
import eu.daiad.web.security.model.ApplicationUser;
import eu.daiad.web.data.*;

@RestController
public class ProfileController {

	private static final Log logger = LogFactory
			.getLog(ProfileController.class);

	@Autowired
	private ProfileRepository profileRepository;

	@RequestMapping(value = "/action/profile", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_USER")
	public ViewProfileResponse getProfile() {
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
		return new ViewProfileResponse(Error.ERROR_UNKNOWN,
				"An unhandled exception has occurred.");
	}

}
