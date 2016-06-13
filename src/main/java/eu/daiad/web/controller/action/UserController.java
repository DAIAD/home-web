package eu.daiad.web.controller.action;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.admin.AccountWhiteListInfo;
import eu.daiad.web.model.amphiro.AmphiroSessionCollection;
import eu.daiad.web.model.amphiro.AmphiroSessionCollectionIndexIntervalQuery;
import eu.daiad.web.model.amphiro.EnumIndexIntervalQuery;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.meter.WaterMeterStatus;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.user.UserInfoCollectionResponse;
import eu.daiad.web.model.user.UserInfoResponse;
import eu.daiad.web.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IFavouriteRepository;
import eu.daiad.web.repository.application.IUserGroupRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

/**
 * Provides actions for managing users.
 *
 */
@RestController
public class UserController extends BaseController {

    private static final Log logger = LogFactory.getLog(UserController.class);

    @Value("${security.white-list}")
    private boolean enforceWhiteListCheck;

    @Autowired
    private IUserRepository repository;

    @Autowired
    private IDeviceRepository deviceRepository;

    @Autowired
    private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    @Autowired
    private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

    @Autowired
    private IUserGroupRepository userGroupRepository;

    @Autowired
    private IFavouriteRepository favouriteRepository;

    @RequestMapping(value = "/action/user/search/prefix/{prefix}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
    public @ResponseBody RestResponse filterUserByPrefix(@PathVariable String prefix) {
        try {
            UserInfoCollectionResponse response = new UserInfoCollectionResponse();

            response.setUsers(repository.filterUserByPrefix(prefix));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            RestResponse response = new RestResponse();
            response.add(this.getError(ex));

            return response;
        }
    }

    /**
     * Adds a user to the white list.
     * 
     * @param user
     *            the currently authenticated user.
     * @param userInfo
     *            the user to add.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/user/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
    public @ResponseBody RestResponse addUserToWhiteList(@AuthenticationPrincipal AuthenticatedUser user,
                    @RequestBody AccountWhiteListInfo userInfo) {
        RestResponse response = new RestResponse();

        try {
            repository.insertAccountWhiteListEntry(userInfo);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Returns basic info for a user given his key. Notice that it does not
     * provide info, regarding the groups it participates or the devices he
     * owns.
     * 
     * @param user_id
     *            the user id
     * @return the user
     */
    @RequestMapping(value = "/action/user/{key}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
    public @ResponseBody RestResponse getUserInfoByKey(@AuthenticationPrincipal AuthenticatedUser user,
                    @PathVariable UUID key) {
        try {
            UserInfoResponse response = new UserInfoResponse();

            response.setUser(repository.getUserInfoByKey(key));
            response.setMeters(getMeters(key));
            response.setDevices(getDevices(key, user.getTimezone()));
            response.setGroups(userGroupRepository.getGroupsByMember(key));

            response.setFavorite(favouriteRepository.isFavorite(user.getKey(), response.getUser().getId()));
            
            return response;
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            RestResponse response = new RestResponse();
            response.add(this.getError(ex));

            return response;
        }
    }

    private List<WaterMeterStatus> getMeters(UUID userkey) {
        DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
        deviceQuery.setType(EnumDeviceType.METER);

        List<String> serials = new ArrayList<String>();
        List<UUID> deviceKeys = new ArrayList<UUID>();

        for (Device d : this.deviceRepository.getUserDevices(userkey, deviceQuery)) {
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

    private ArrayList<AmphiroSessionCollection> getDevices(UUID userkey, String timezone) {
        DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery();
        deviceQuery.setType(EnumDeviceType.AMPHIRO);

        List<String> names = new ArrayList<String>();
        List<UUID> deviceKeys = new ArrayList<UUID>();

        for (Device d : this.deviceRepository.getUserDevices(userkey, deviceQuery)) {
            names.add(((AmphiroDevice) d).getName());
            deviceKeys.add(((AmphiroDevice) d).getKey());
        }

        AmphiroSessionCollectionIndexIntervalQuery query = new AmphiroSessionCollectionIndexIntervalQuery();
        query.setDeviceKey(deviceKeys.toArray(new UUID[] {}));
        query.setLength(1);
        query.setType(EnumIndexIntervalQuery.SLIDING);
        query.setUserKey(userkey);

        return amphiroIndexOrderedRepository.searchSessions(names.toArray(new String[] {}),
                        DateTimeZone.forID(timezone), query).getDevices();
    }

    /**
     * Adds a user to the favorite list
     * 
     * @param userKey the key of the user to add
     * @return the result of the operation
     */
    @RequestMapping(value = "/action/favorite/{userKey}", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ "ROLE_ADMIN" })
    public @ResponseBody RestResponse addUserFavorite(@AuthenticationPrincipal AuthenticatedUser user,
                    @PathVariable UUID userKey) {
        try {
            favouriteRepository.addFavorite(user.getKey(), userKey);
            
            return new RestResponse();
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            RestResponse response = new RestResponse();
            response.add(this.getError(ex));

            return response;
        }
    }

    /**
     * Removes a user from the favorite list
     * 
     * @param userKey the key of the user to remove
     * @return the result of the operation
     */
    @RequestMapping(value = "/action/favorite/{userKey}", method = RequestMethod.DELETE, produces = "application/json")
    @Secured({ "ROLE_ADMIN" })
    public @ResponseBody RestResponse removeUserFavorite(@AuthenticationPrincipal AuthenticatedUser user,
                    @PathVariable UUID userKey) {
        try {
            favouriteRepository.deleteFavorite(user.getKey(), userKey);
            
            return new RestResponse();
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            RestResponse response = new RestResponse();
            response.add(this.getError(ex));

            return response;
        }
    }

}
