package eu.daiad.api.controller.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.api.controller.BaseRestController;
import eu.daiad.common.model.EnumApplication;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.profile.Profile;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.AuthenticationResponse;
import eu.daiad.common.model.security.Credentials;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.repository.application.IProfileRepository;


/**
 * Provides actions for authenticating a user.
 */
@RestController
public class AuthenticationController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(AuthenticationController.class);

    /**
     * Repository for accessing user profile data.
     */
    @Autowired
    private IProfileRepository profileRepository;

    /**
     * Authenticates a user.
     *
     * @param credentials the user credentials
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v1/auth/login")
    public RestResponse login(@RequestBody Credentials credentials) {
        try {
            AuthenticatedUser user = authenticate(credentials, EnumRole.ROLE_USER);

            Profile profile = profileRepository.getProfileByUserKey(user.getKey(), EnumApplication.MOBILE);

            profileRepository.updateMobileVersion(user.getKey(), credentials.getVersion());

            return new AuthenticationResponse(getRuntime(), profile, user.roleToStringArray());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

}
