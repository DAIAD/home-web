package eu.daiad.web.controller.action;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.profile.ProfileDeactivateRequest;
import eu.daiad.web.model.profile.ProfileModesFilterOptionsResponse;
import eu.daiad.web.model.profile.ProfileModesRequest;
import eu.daiad.web.model.profile.ProfileModesResponse;
import eu.daiad.web.model.profile.ProfileModesSubmitChangesRequest;
import eu.daiad.web.model.profile.ProfileResponse;
import eu.daiad.web.model.profile.UpdateHouseholdRequest;
import eu.daiad.web.model.profile.UpdateProfileRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IProfileRepository;
import eu.daiad.web.util.ValidationUtils;

/**
 * Provides methods for managing user profile.
 */
@RestController
public class ProfileController extends BaseController {

    private static final Log logger = LogFactory.getLog(ProfileController.class);

    @Autowired
    private IProfileRepository profileRepository;

    @RequestMapping(value = "/action/profile/load", method = RequestMethod.GET, produces = "application/json")
    @Secured({ "ROLE_USER", "ROLE_ADMIN" })
    public RestResponse getProfile(@AuthenticationPrincipal AuthenticatedUser user) {
        RestResponse response = new RestResponse();

        try {
            if (user.hasRole("ROLE_ADMIN")) {
                return new ProfileResponse(this.getRuntime(), profileRepository
                                .getProfileByUsername(EnumApplication.UTILITY));
            }

            return new ProfileResponse(this.getRuntime(), profileRepository.getProfileByUsername(EnumApplication.HOME));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * It lists all modes (mobile, amphiro, social) for all users matching the
     * specified filter options (if any).
     * 
     * @param user the authenticated user.
     * @param filters the filter options
     * @return the array of modes for the matching users
     * @throws JsonProcessingException
     */
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

    /**
     * It returns all available filter options for the user modes (including
     * possible mode values and utility names)
     * 
     * @param user the authenticated user.
     * @return the available filter options.
     * @throws JsonProcessingException
     */
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

    /**
     * Given an array of altered modes, this changes are applied.
     * 
     * @param user the authenticated user.
     * @param modeChanges the array of mode changes.
     * @return the controller's response.
     * @throws JsonProcessingException
     */
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
     * @param user the authenticated user.
     * @param userDeactId the user to deactivate
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

    /**
     * Updates user profile
     * 
     * @param user the authenticated user 
     * @param request the profile data to store 
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/profile/save", method = RequestMethod.POST, produces = "application/json")
    @Secured({ "ROLE_USER", "ROLE_SUPERUSER", "ROLE_ADMIN" })
    public RestResponse saveProfile(@AuthenticationPrincipal AuthenticatedUser user,
                    @RequestBody UpdateProfileRequest request) {
        RestResponse response = new RestResponse();

        try {
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
                if (user.hasRole("ROLE_ADMIN")) {
                    request.setApplication(EnumApplication.UTILITY);
                    this.profileRepository.saveProfile(request);
                } else {
                    request.setApplication(EnumApplication.HOME);
                    this.profileRepository.saveProfile(request);
                }
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
    @RequestMapping(value = "/action/household", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ "ROLE_USER" })
    public RestResponse saveHousehold(@RequestBody UpdateHouseholdRequest request) {
        RestResponse response = new RestResponse();

        try {
            this.profileRepository.saveHousehold(request);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

}
