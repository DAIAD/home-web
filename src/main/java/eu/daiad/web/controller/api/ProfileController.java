package eu.daiad.web.controller.api;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.profile.NotifyProfileRequest;
import eu.daiad.web.model.profile.ProfileResponse;
import eu.daiad.web.model.profile.UpdateHouseholdRequest;
import eu.daiad.web.model.profile.UpdateProfileRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IProfileRepository;
import eu.daiad.web.util.ValidationUtils;

/**
 * Provides actions for loading and updating user profile.
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
    public RestResponse getProfile(@RequestBody Credentials data) {
        RestResponse response = new RestResponse();

        try {
            AuthenticatedUser user = this.authenticate(data);

            if (user.hasRole(EnumRole.ROLE_USER)) {
                return new ProfileResponse(this.getRuntime(), this.profileRepository
                                .getProfileByUsername(EnumApplication.MOBILE));
            } else if (user.hasRole(EnumRole.ROLE_ADMIN)) {
                return new ProfileResponse(this.getRuntime(), this.profileRepository
                                .getProfileByUsername(EnumApplication.UTILITY));
            } else {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Updates user profile
     * 
     * @param request the profile data to store
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/profile/save", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse saveProfile(@RequestBody UpdateProfileRequest request) {
        RestResponse response = new RestResponse();

        try {
            AuthenticatedUser user = this.authenticate(request.getCredentials());

            if (user.hasRole(EnumRole.ROLE_USER)) {
                request.setApplication(EnumApplication.MOBILE);
            } else if (user.hasRole(EnumRole.ROLE_ADMIN)) {
                request.setApplication(EnumApplication.UTILITY);
            } else {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }

            // Check time zone
            String timezone = request.getTimezone();

            Set<String> zones = DateTimeZone.getAvailableIDs();

            if ((!StringUtils.isBlank(timezone)) && (!zones.contains(timezone))) {
                Map<String, Object> properties = ImmutableMap.<String, Object> builder().put("timezone", timezone)
                                .build();

                response.add(SharedErrorCode.TIMEZONE_NOT_FOUND, this.getMessage(SharedErrorCode.TIMEZONE_NOT_FOUND,
                                properties));
            }
            // Check locale
            String locale = request.getLocale();

            if ((!StringUtils.isBlank(locale)) && (!ValidationUtils.isLocaleValid(locale))) {
                Map<String, Object> properties = ImmutableMap.<String, Object> builder().put("locale", locale).build();

                response.add(SharedErrorCode.LOCALE_NOT_SUPPORTED, this.getMessage(
                                SharedErrorCode.LOCALE_NOT_SUPPORTED, properties));
            }

            if (response.getSuccess()) {
                this.profileRepository.saveProfile(request);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }


    /**
     * Updates user household information.
     * 
     * @param request the profile data to store
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/household", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse saveHousehold(@RequestBody UpdateHouseholdRequest request) {
        RestResponse response = new RestResponse();

        try {
            this.authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            this.profileRepository.saveHousehold(request);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Updates user profile that a specific application configuration version
     * has been applied to the mobile client.
     * 
     * @param request the notification request.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/profile/notify", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse notifyProfile(@RequestBody NotifyProfileRequest request) {
        RestResponse response = new RestResponse();

        try {
            this.authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            this.profileRepository.notifyProfile(EnumApplication.MOBILE, request.getVersion(), new DateTime(request
                            .getUpdatedOn()));

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }
}
