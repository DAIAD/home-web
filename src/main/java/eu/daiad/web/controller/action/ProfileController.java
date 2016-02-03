package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.data.IProfileRepository;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.ProfileResponse;
import eu.daiad.web.model.security.AuthenticatedUser;

@RestController
public class ProfileController extends BaseController {

	private static final Log logger = LogFactory.getLog(ProfileController.class);

	@Autowired
	private IProfileRepository profileRepository;

	@RequestMapping(value = "/action/profile", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getProfile(@AuthenticationPrincipal AuthenticatedUser user) {
		ProfileResponse response = new ProfileResponse();

		try {
			response.setProfile(profileRepository.getProfileByUsername(user.getUsername()));
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

}
