package eu.daiad.web.controller.api;

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
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.security.AuthenticationResponse;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.repository.application.IProfileRepository;

/**
 * Provides actions for authenticating a user.
 */
@RestController("RestAuthenticationController")
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
    @RequestMapping(value = "/api/v1/auth/login", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse login(@RequestBody Credentials credentials) {
        try {
            this.authenticate(credentials);

            Profile profile = profileRepository.getProfileByUsername(EnumApplication.MOBILE);

            return new AuthenticationResponse(this.getRuntime(), profile);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

}
