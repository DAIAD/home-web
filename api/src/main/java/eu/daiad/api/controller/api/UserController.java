package eu.daiad.api.controller.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.api.controller.BaseRestController;
import eu.daiad.common.model.AuthenticatedRequest;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.amphiro.AmphiroSessionCollection;
import eu.daiad.common.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.common.model.amphiro.EnumIndexIntervalQuery;
import eu.daiad.common.model.device.AmphiroDevice;
import eu.daiad.common.model.device.Device;
import eu.daiad.common.model.device.DeviceAmphiroConfiguration;
import eu.daiad.common.model.device.DeviceRegistrationQuery;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.device.WaterMeterDevice;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.meter.WaterMeterStatus;
import eu.daiad.common.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.model.security.PasswordChangeRequest;
import eu.daiad.common.model.security.PasswordResetTokenCreateRequest;
import eu.daiad.common.model.security.PasswordResetTokenCreateResponse;
import eu.daiad.common.model.security.PasswordResetTokenRedeemRequest;
import eu.daiad.common.model.user.Account;
import eu.daiad.common.model.user.UserInfoResponse;
import eu.daiad.common.model.user.UserRegistrationRequest;
import eu.daiad.common.model.user.UserRegistrationResponse;
import eu.daiad.common.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.common.repository.application.IDeviceRepository;
import eu.daiad.common.repository.application.IFavouriteRepository;
import eu.daiad.common.repository.application.IGroupRepository;
import eu.daiad.common.repository.application.IMeterDataRepository;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.service.IUserService;

/**
 * Provides methods for user management
 */
@RestController
public class UserController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(UserController.class);

    /**
     * Instance of @{link IUserService} that implements user management operations.
     */
    @Autowired
    private IUserService userService;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Repository for accessing favourite data.
     */
    @Autowired
    private IFavouriteRepository favouriteRepository;

    /**
     * Repository for accessing amphiro b1 data.
     */
    @Autowired
    private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    /**
     * Repository for accessing smart water meter data.
     */
    @Autowired
    private IMeterDataRepository waterMeterMeasurementRepository;

    /**
     * Instance of @{link org.springframework.validation.Validator} for performing user input validation manually.
     */
    @Autowired
    private org.springframework.validation.Validator validator;

    /**
     * True if white list checks must be applied; Otherwise False.
     */
    @Value("${security.white-list:true}")
    private boolean enforceWhiteListCheck;

    /**
     * Creates a new user.
     *
     * @param request the request.
     * @param results the binding results.
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v1/user/register")
    public RestResponse register(@RequestBody UserRegistrationRequest request, BindingResult results) {
        RestResponse response = new RestResponse();

        try {
            if (enforceWhiteListCheck) {
                ((OptionalValidatorFactoryBean) validator).validate(request, results,
                                Account.AccountSimpleValidation.class);
            } else {
                ((OptionalValidatorFactoryBean) validator).validate(request.getAccount(), results,
                                Account.AccountDefaultValidation.class);
            }

            if (results.hasErrors()) {
                for (FieldError e : results.getFieldErrors()) {
                    response.add(this.getError(e));
                }

                return response;
            }

            UUID userKey = userService.createUser(request);

            UserRegistrationResponse registerResponse = new UserRegistrationResponse();
            registerResponse.setUserKey(userKey.toString());

            return registerResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Changes a user's password.
     *
     * @param data the request.
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v1/user/password/change")
    public RestResponse changePassword(@RequestBody PasswordChangeRequest data) {
        RestResponse response = new RestResponse();

        try {
            AuthenticatedUser user = authenticate(data.getCredentials());

            if (user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
                if(StringUtils.isBlank(data.getUsername())) {
                    userService.changePassword(user.getUsername(), data.getPassword());
                } else {
                    userService.changePassword(data.getUsername(), data.getPassword());
                }
            } else if(user.hasRole(EnumRole.ROLE_USER)){
                if(StringUtils.isBlank(data.getUsername())) {
                    userService.changePassword(user.getUsername(), data.getPassword());
                } else {
                    throw createApplicationException(SharedErrorCode.AUTHORIZATION);
                }
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
     * Requests a token for resetting a user's password.
     *
     * @param request the name of the user.
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v1/user/password/reset/token/create")
    public RestResponse resetPasswordCreateToken(@RequestBody PasswordResetTokenCreateRequest request) {
        try {
            UUID token = userService.resetPasswordCreateToken(request.getUsername(), request.getApplication());

            return new PasswordResetTokenCreateResponse(token);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            // Do not send detailed error information
            return new RestResponse(getErrorUnknown());
        }
    }

    /**
     * Resets a user's password given a valid token and password.
     *
     * @param request the token and new password values.
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v1/user/password/reset/token/redeem")
    public RestResponse resetPasswordRedeemToken(@RequestBody PasswordResetTokenRedeemRequest request) {
        RestResponse response = new RestResponse();

        try {
            userService.resetPasswordRedeemToken(request.getToken(), request.getPin(), request.getPassword());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Returns basic info for a user given his key.
     *
     * @param userKey the key of the user for which data is requested.
     * @param request instance of {@link AuthenticatedRequest}.
     * @return information about the requested user.
     */
    @PostMapping(value = "/api/v1/user/{userKey}")
    public @ResponseBody RestResponse getUserInfoByKey(@PathVariable UUID userKey, @RequestBody AuthenticatedRequest request) {
        try {
            AuthenticatedUser user = authenticate(request.getCredentials(),
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

            UserInfoResponse response = new UserInfoResponse();

            response.setUser(userRepository.getUserInfoByKey(userKey));
            response.setGroups(groupRepository.getMemberGroups(userKey));
            response.setFavorite(favouriteRepository.isUserFavorite(userKey, response.getUser().getId()));

            List<Device> amphiroDevices = getAmphiroDevices(userKey);

            response.setConfigurations(new ArrayList<DeviceAmphiroConfiguration>());
            for (Device d : amphiroDevices) {
                response.getConfigurations().add(((AmphiroDevice) d).getConfiguration());
            }

            response.setMeters(getMeters(userKey));
            response.setDevices(getDevices(userKey, amphiroDevices, user.getTimezone()));

            return response;
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Get the amphiro b1 devices registered to the user with the given key.
     *
     * @param userkey the user key.
     * @return a list of amphiro b1 device objects.
     */
    private List<Device> getAmphiroDevices(UUID userkey) {
        DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
        deviceQuery.setType(EnumDeviceType.AMPHIRO);

        return deviceRepository.getUserDevices(userkey, deviceQuery);
    }

    /**
     * Get the status of the smart water meters for the user with the given key.
     *
     * @param userkey the user key.
     * @return a list of smart water meter status objects.
     */
    private List<WaterMeterStatus> getMeters(UUID userkey) {
        DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
        deviceQuery.setType(EnumDeviceType.METER);

        List<String> serials = new ArrayList<String>();
        List<UUID> deviceKeys = new ArrayList<UUID>();

        for (Device d : deviceRepository.getUserDevices(userkey, deviceQuery)) {
            serials.add(((WaterMeterDevice) d).getSerial());
            deviceKeys.add(((WaterMeterDevice) d).getKey());
        }

        WaterMeterStatusQueryResult result = waterMeterMeasurementRepository
                        .getStatus(serials.toArray(new String[] {}));

        for (WaterMeterStatus status : result.getDevices()) {
            for (int i = 0, count = serials.size(); i < count; i++) {
                if (serials.get(i).equals(status.getSerial())) {
                    status.setDeviceKey(deviceKeys.get(i));
                    break;
                }
            }
        }

        return result.getDevices();
    }

    /**
     * Get the amphiro b1 devices registered to the user with the given key
     * alongside with the most recent session.
     *
     * @param userkey the user key.
     * @param devices a list of devices to search for.
     * @param timezone the time zone used for reference for expressing timestamps.
     * @return a list of amphiro b1 device objects with sessions.
     */
    private List<AmphiroSessionCollection> getDevices(UUID userkey, List<Device> devices, String timezone) {
        List<String> names = new ArrayList<String>();
        List<UUID> deviceKeys = new ArrayList<UUID>();

        for (Device d : devices) {
            names.add(((AmphiroDevice) d).getName());
            deviceKeys.add(((AmphiroDevice) d).getKey());
        }

        AmphiroSessionCollectionIndexIntervalQuery query = new AmphiroSessionCollectionIndexIntervalQuery();
        query.setDeviceKey(deviceKeys.toArray(new UUID[] {}));
        query.setLength(1);
        query.setType(EnumIndexIntervalQuery.SLIDING);
        query.setUserKey(userkey);

        return amphiroIndexOrderedRepository.getSessions(names.toArray(new String[] {}),
                        DateTimeZone.forID(timezone), query).getDevices();
    }
}
