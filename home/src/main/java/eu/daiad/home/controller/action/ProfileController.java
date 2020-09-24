package eu.daiad.home.controller.action;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;

import eu.daiad.common.model.EnumApplication;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.profile.ComparisonRankingResponse;
import eu.daiad.common.model.profile.ProfileDeactivateRequest;
import eu.daiad.common.model.profile.ProfileModesFilterOptionsResponse;
import eu.daiad.common.model.profile.ProfileModesRequest;
import eu.daiad.common.model.profile.ProfileModesResponse;
import eu.daiad.common.model.profile.ProfileModesSubmitChangesRequest;
import eu.daiad.common.model.profile.ProfileResponse;
import eu.daiad.common.model.profile.UpdateHouseholdRequest;
import eu.daiad.common.model.profile.UpdateProfileRequest;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.model.security.RoleConstant;
import eu.daiad.common.repository.application.IProfileRepository;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.repository.application.IWaterIqRepository;
import eu.daiad.common.util.ValidationUtils;
import eu.daiad.home.controller.BaseController;

/**
 * Provides methods for managing user profile.
 */
@RestController
public class ProfileController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(ProfileController.class);

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing user profile data.
     */
    @Autowired
    private IProfileRepository profileRepository;

    /**
     * Repository for accessing water IQ data.
     */
    @Autowired
    @Qualifier("jpaWaterIqRepository")
    private IWaterIqRepository waterIqRepository;

    /**
     * Loads user profile data.
     *
     * @param user the currently authenticated user.
     * @return the user profile.
     */
    @RequestMapping(value = "/action/profile/load", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getProfile(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            if (user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
                return new ProfileResponse(getRuntime(),
                                           profileRepository.getProfileByUserKey(user.getKey(), EnumApplication.UTILITY),
                                           user.roleToStringArray());
            }

            return new ProfileResponse(getRuntime(),
                                       profileRepository.getProfileByUserKey(user.getKey(), EnumApplication.HOME),
                                       user.roleToStringArray());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * It lists all modes (mobile, amphiro, social) for all users matching the
     * specified filter options (if any).
     *
     * @param user the authenticated user.
     * @param filters the filter options
     * @return the array of modes for the matching users
     */
    @RequestMapping(value = "/action/profile/modes/list", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getProfileModes(@AuthenticationPrincipal AuthenticatedUser user,
                                        @RequestBody ProfileModesRequest filters) {
        try {
            return new ProfileModesResponse(profileRepository.getProfileModes(filters));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
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
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getFilterOptions(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            return new ProfileModesFilterOptionsResponse(profileRepository.getFilterOptions());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Given an array of altered modes, this changes are applied.
     *
     * @param user the authenticated user.
     * @param modeChanges the array of mode changes.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/profile/modes/save", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse saveModeChanges(@AuthenticationPrincipal AuthenticatedUser user,
                                        @RequestBody ProfileModesSubmitChangesRequest modeChanges) {
        try {
            profileRepository.setProfileModes(modeChanges);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }

        return new RestResponse();
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
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse deactivateProfile(@AuthenticationPrincipal AuthenticatedUser user,
                                          @RequestBody ProfileDeactivateRequest userDeactId) {
        try {
            profileRepository.deactivateProfile(userDeactId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Updates user profile
     *
     * @param user the authenticated user
     * @param request the profile data to store
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/profile/save", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
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

                response.add(SharedErrorCode.TIMEZONE_NOT_FOUND, this.getMessage(SharedErrorCode.TIMEZONE_NOT_FOUND, properties));
            }
            // Check locale
            String locale = request.getLocale();

            if ((!StringUtils.isBlank(locale)) && (!ValidationUtils.isLocaleValid(locale))) {
                Map<String, Object> properties = ImmutableMap.<String, Object> builder().put("locale", locale).build();

                response.add(SharedErrorCode.LOCALE_NOT_SUPPORTED, this.getMessage(SharedErrorCode.LOCALE_NOT_SUPPORTED, properties));
            }

            if (response.getSuccess()) {
                if (user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
                    request.setApplication(EnumApplication.UTILITY);
                    profileRepository.saveProfile(request);
                } else {
                    request.setApplication(EnumApplication.HOME);
                    profileRepository.saveProfile(request);
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
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse saveHousehold(@RequestBody UpdateHouseholdRequest request) {
        RestResponse response = new RestResponse();

        try {
            profileRepository.saveHousehold(request);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Loads comparison and ranking data for a user.
     *
     * @param year reference year.
     * @param month reference month.
     * @return the user profile.
     */
    @RequestMapping(value = "/action/comparison/{year}/{month}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse getComparisonRanking(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable int year, @PathVariable int month) {
        try {
            ComparisonRankingResponse response = new ComparisonRankingResponse();

            response.setComparison(waterIqRepository.getWaterIqByUserKey(user.getKey(), year, month));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads comparison and ranking data for a user.
     *
     * @param year reference year.
     * @param month reference month.
     * @return the user profile.
     */
    @RequestMapping(value = "/action/comparison/{year}/{month}/{userKey}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getComparisonRankingForUser(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable int year, @PathVariable int month,
                                                    @PathVariable UUID userKey) {
        try {
            // If user has not administrative permissions and requests data for another user, throw an exception
            if ((!user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) && (!user.getKey().equals(userKey))) {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }

            // Check utility access
            if (!user.getKey().equals(userKey)) {
                AuthenticatedUser dataOwner = userRepository.getUserByKey(userKey);

                if (!user.getUtilities().contains(dataOwner.getUtilityId())) {
                    throw createApplicationException(SharedErrorCode.AUTHORIZATION_UTILITY_ACCESS_DENIED);
                }
            }

            ComparisonRankingResponse response = new ComparisonRankingResponse();

            response.setComparison(waterIqRepository.getWaterIqByUserKey(userKey, year, month));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

}
