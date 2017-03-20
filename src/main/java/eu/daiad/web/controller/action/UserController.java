package eu.daiad.web.controller.action;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.DeviceMeterEntity;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.admin.AccountWhiteListInfo;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.EnumIndexIntervalQuery;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceAmphiroConfiguration;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.meter.WaterMeterStatus;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.PasswordChangeRequest;
import eu.daiad.web.model.security.PasswordResetTokenCreateRequest;
import eu.daiad.web.model.security.PasswordResetTokenRedeemRequest;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.model.user.DeviceMeterInfo;
import eu.daiad.web.model.user.UserInfo;
import eu.daiad.web.model.user.UserInfoCollectionResponse;
import eu.daiad.web.model.user.UserInfoResponse;
import eu.daiad.web.model.user.UserQuery;
import eu.daiad.web.model.user.UserQueryRequest;
import eu.daiad.web.model.user.UserQueryResponse;
import eu.daiad.web.model.user.UserQueryResult;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IFavouriteRepository;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IMeterDataRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.service.IUserService;

/**
 * Provides methods for user management.
 */
@RestController
public class UserController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(UserController.class);

    /**
     * True if the white list is enabled.
     */
    @Value("${security.white-list}")
    private boolean enforceWhiteListCheck;

    /**
     * Service for creating new user accounts.
     */
    @Autowired
    private IUserService userService;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

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
     * Repository for accessing favourite data.
     */
    @Autowired
    private IFavouriteRepository favouriteRepository;

    /**
     * Searches user by name prefix.
     *
     * @param prefix the prefix to search for.
     * @return a list of users.
     */
    @RequestMapping(value = "/action/user/search/prefix/{prefix}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse filterUserByPrefix(@PathVariable String prefix) {
        try {
            UserInfoCollectionResponse response = new UserInfoCollectionResponse();

            response.setUsers(userRepository.filterUserByPrefix(prefix));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Searches users.
     *
     * @param user the currently authenticated user.
     * @param request the query for filtering users.
     * @return a list of users.
     */
    @RequestMapping(value = "/action/user/search", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse search(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody UserQueryRequest request) {
        try {
            // Set default values
            if (request.getQuery() == null) {
                request.setQuery(new UserQuery());
            }
            if ((request.getQuery().getIndex() == null) || (request.getQuery().getIndex() < 0)) {
                request.getQuery().setIndex(0);
            }
            if (request.getQuery().getSize() == null) {
                request.getQuery().setSize(10);
            }

            UserQueryResult result = userRepository.search(request.getQuery());

            UserQueryResponse response = new UserQueryResponse();

            response.setTotal(result.getTotal());

            response.setIndex(request.getQuery().getIndex());
            response.setSize(request.getQuery().getSize());

            List<UserInfo> accounts = new ArrayList<UserInfo>();

            for (AccountEntity entity : result.getAccounts()) {
                UserInfo account = new UserInfo(entity);

                account.setLocation(entity.getLocation());

                for (eu.daiad.web.domain.application.DeviceEntity d : entity.getDevices()) {
                    if (d.getType() == EnumDeviceType.METER) {
                        account.setMeter(new DeviceMeterInfo((DeviceMeterEntity) d));
                        break;
                    }
                }

                account.setFavorite(favouriteRepository.isUserFavorite(user.getKey(), entity.getKey()));

                accounts.add(account);
            }
            response.setAccounts(accounts);

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Adds a user to the white list.
     *
     * @param user the currently authenticated user.
     * @param userInfo the user to add.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/user/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse addUserToWhiteList(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @RequestBody AccountWhiteListInfo userInfo) {
        try {
            userRepository.insertAccountWhiteListEntry(userInfo);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(this.getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Returns basic info for a user given his key. Notice that it does not
     * provide info, regarding the groups it participates or the devices he
     * owns.
     *
     * @param user the authenticated user.
     * @param key the key of the user for which data is requested.
     * @return information about the requested user.
     */
    @RequestMapping(value = "/action/user/{key}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse getUserInfoByKey(@AuthenticationPrincipal AuthenticatedUser user,
                                                       @PathVariable UUID key) {
        try {
            UserInfoResponse response = new UserInfoResponse();

            response.setUser(userRepository.getUserInfoByKey(key));
            response.setGroups(groupRepository.getMemberGroups(key));
            response.setFavorite(favouriteRepository.isUserFavorite(user.getKey(), response.getUser().getId()));

            List<Device> amphiroDevices = getAmphiroDevices(key);

            response.setConfigurations(new ArrayList<DeviceAmphiroConfiguration>());
            for (Device d : amphiroDevices) {
                response.getConfigurations().add(((AmphiroDevice) d).getConfiguration());
            }

            response.setMeters(getMeters(key));
            response.setDevices(getDevices(key, amphiroDevices, user.getTimezone()));

            return response;
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
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

    /**
     * Adds a user to the favorite list.
     *
     * @param userKey the key of the user to add.
     * @return the result of the operation.
     */
    @RequestMapping(value = "/action/user/favorite/{userKey}", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse addFavorite(@AuthenticationPrincipal AuthenticatedUser user,
                                                  @PathVariable UUID userKey) {
        try {
            favouriteRepository.addUserFavorite(user.getKey(), userKey);

            return new RestResponse();
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Removes a user from the favorite list
     *
     * @param userKey the key of the user to remove
     * @return the result of the operation
     */
    @RequestMapping(value = "/action/user/favorite/{userKey}", method = RequestMethod.DELETE, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse removeFavorite(@AuthenticationPrincipal AuthenticatedUser user,
                    @PathVariable UUID userKey) {
        try {
            favouriteRepository.deleteUserFavorite(user.getKey(), userKey);

            return new RestResponse();
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Changes a user's password.
     *
     * @param user the currently authenticated user.
     * @param data the new password and optionally a user name for changing the password of a different user.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/user/password/change", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse changePassword(HttpServletRequest httpRequest,
                                       @AuthenticationPrincipal AuthenticatedUser user,
                                       @RequestBody PasswordChangeRequest request) {
        try {
            String remoteAddress = getRemoteAddress(httpRequest);

            if (user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
                if(StringUtils.isBlank(request.getUsername())) {
                    userService.changePassword(remoteAddress, request.getCaptcha(), user.getUsername(), request.getPassword());
                } else {
                    userService.changePassword(remoteAddress, request.getCaptcha(), request.getUsername(), request.getPassword());
                }
            } else if(user.hasRole(EnumRole.ROLE_USER)){
                if(StringUtils.isBlank(request.getUsername())) {
                    userService.changePassword(remoteAddress, request.getCaptcha(), user.getUsername(), request.getPassword());
                } else {
                    throw createApplicationException(SharedErrorCode.AUTHORIZATION);
                }
            } else {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Requests a token for resetting a user's password.
     *
     * @param request the name of the user.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/user/password/reset/token/create", method = RequestMethod.POST, produces = "application/json")
    public RestResponse resetPasswordCreateToken(@RequestBody PasswordResetTokenCreateRequest request) {
        try {
            userService.resetPasswordCreateToken(request.getUsername(), request.getApplication());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            // Do not send detailed error information
            return new RestResponse(getErrorUnknown());
        }

        return new RestResponse();
    }

    /**
     * Resets a user's password given a valid token and password.
     *
     * @param request the token and new password values.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/user/password/reset/token/redeem", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse resetPasswordRedeemToken(HttpServletRequest httpRequest, @RequestBody PasswordResetTokenRedeemRequest request) {
        try {
            String remoteAddress = getRemoteAddress(httpRequest);

            userService.resetPasswordRedeemToken(remoteAddress, request.getCaptcha(), request.getToken(), request.getPin(), request.getPassword());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Returns the remote address for the given {@link HttpServletRequest} request.
     * @param httpRequest the request.
     * @return the remote address.
     */
    private String getRemoteAddress(HttpServletRequest httpRequest) {
        String remoteAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (StringUtils.isBlank(remoteAddress)) {
            remoteAddress = httpRequest.getRemoteAddr();
        }

        return remoteAddress;
    }

}
