package eu.daiad.api.controller.api;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.daiad.api.controller.BaseRestController;
import eu.daiad.common.model.EnumApplication;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ProfileErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.profile.ComparisonRankingResponse;
import eu.daiad.common.model.profile.NotifyProfileRequest;
import eu.daiad.common.model.profile.Profile;
import eu.daiad.common.model.profile.ProfileResponse;
import eu.daiad.common.model.profile.UpdateHouseholdRequest;
import eu.daiad.common.model.profile.UpdateProfileRequest;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.Credentials;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.repository.application.IProfileRepository;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.repository.application.IWaterIqRepository;
import eu.daiad.common.util.ValidationUtils;

/**
 * Provides actions for loading and updating user profile.
 */
@RestController
public class ProfileController extends BaseRestController {

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
     * @param request user credentials.
     * @return the user profile.
     */
    @PostMapping(value = "/api/v1/profile/load")
    public RestResponse getProfile(@RequestBody Credentials request) {
        try {
            AuthenticatedUser user = authenticate(request);

            if (user.hasRole(EnumRole.ROLE_USER)) {
                profileRepository.updateMobileVersion(user.getKey(), request.getVersion());

                return new ProfileResponse(getRuntime(),
                                           profileRepository.getProfileByUserKey(user.getKey(), EnumApplication.MOBILE),
                                           user.roleToStringArray());
            } else if (user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
                return new ProfileResponse(getRuntime(),
                                           profileRepository.getProfileByUserKey(user.getKey(), EnumApplication.UTILITY),
                                           user.roleToStringArray());
            } else {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads user profile data for a specific user.
     *
     * @param application the request's application.
     * @param userKey the user key.
     * @param request user credentials.
     * @return the user profile.
     */
    @PostMapping(value = "/api/v1/profile/load/{application}/{userKey}")
    public RestResponse getProfileByUserKey(@PathVariable EnumApplication application,
                                            @PathVariable UUID userKey,
                                            @RequestBody Credentials request) {
        try {
            AuthenticatedUser user = authenticate(request,
                                                  EnumRole.ROLE_USER, EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

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

            switch(application) {
                case HOME: case MOBILE: case UTILITY:
                    Profile profile = profileRepository.getProfileByUserKey(userKey, application);

                    return new ProfileResponse(getRuntime(), profile, user.roleToStringArray());
                default:
                    throw createApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application", application.toString());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads comparison and ranking data for a user.
     *
     * @param data user credentials.
     * @param year reference year.
     * @param month reference month.
     * @return the user profile.
     */
    @PostMapping(value = "/api/v1/comparison/{year}/{month}")
    public RestResponse getComparisonRanking(@RequestBody Credentials data, @PathVariable int year, @PathVariable int month) {
        try {
            AuthenticatedUser user = authenticate(data);

            if (user.hasRole(EnumRole.ROLE_USER)) {
                ComparisonRankingResponse response = new ComparisonRankingResponse();

                response.setComparison(waterIqRepository.getWaterIqByUserKey(user.getKey(), year, month));

                return response;
            } else {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads comparison and ranking data for a user.
     *
     * @param data user credentials.
     * @param year reference year.
     * @param month reference month.
     * @return the user profile.
     */
    @PostMapping(value = "/api/v1/comparison/{year}/{month}/{userKey}")
    public RestResponse getComparisonRanking(@RequestBody Credentials data,
                                             @PathVariable int year, @PathVariable int month,
                                             @PathVariable UUID userKey) {
        try {
            AuthenticatedUser user = authenticate(data);

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

    /**
     * Updates user profile.
     *
     * @param request the profile data to store.
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v1/profile/save")
    public RestResponse saveProfile(@RequestBody UpdateProfileRequest request) {
        RestResponse response = new RestResponse();

        try {
            AuthenticatedUser user = authenticate(request.getCredentials());

            if (user.hasRole(EnumRole.ROLE_USER)) {
                request.setApplication(EnumApplication.MOBILE);
            } else if (user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
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
                profileRepository.saveProfile(request);
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
    @PostMapping(value = "/api/v1/household")
    public RestResponse saveHousehold(@RequestBody UpdateHouseholdRequest request) {
        RestResponse response = new RestResponse();

        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            profileRepository.saveHousehold(request);
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
    @PostMapping(value = "/api/v1/profile/notify")
    public RestResponse notifyProfile(@RequestBody NotifyProfileRequest request) {
        RestResponse response = new RestResponse();

        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_USER);

            profileRepository.notifyProfile(EnumApplication.MOBILE, request.getVersion(), new DateTime(request.getUpdatedOn()));

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }
}
