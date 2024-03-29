package eu.daiad.home.controller.action;

import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.home.controller.BaseController;
import eu.daiad.common.domain.application.AreaGroupEntity;
import eu.daiad.common.domain.application.AreaGroupMemberEntity;
import eu.daiad.common.domain.application.DeviceMeterEntity;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.device.WaterMeterDevice;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.RoleConstant;
import eu.daiad.common.model.spatial.Area;
import eu.daiad.common.model.spatial.AreaCollectionResponse;
import eu.daiad.common.model.spatial.AreaGroup;
import eu.daiad.common.model.spatial.AreaGroupCollectionResponse;
import eu.daiad.common.model.spatial.AreaResponse;
import eu.daiad.common.model.spatial.MeterCollectionResponse;
import eu.daiad.common.repository.application.ISpatialRepository;

/**
 * Provides actions for querying spatial data.
 */
@RestController
public class SpatialController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(SpatialController.class);

    /**
     * Repository for accessing spatial data.
     */
    @Autowired
    private ISpatialRepository spatialRepository;

    /**
     * Returns all available SWM
     *
     * @param authenticatedUser the currently authenticated user.
     * @return a collection of {@link WaterMeterDevice}.
     */
    @RequestMapping(value = "/action/spatial/meters", method = RequestMethod.GET)
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getMeters(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            List<DeviceMeterEntity> meters = spatialRepository.getAllMetersByUtilityId(authenticatedUser.getUtilityKey());

            return new MeterCollectionResponse(meters);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Returns all available area groups.
     *
     * @param authenticatedUser the currently authenticated user.
     * @return a collection of {@link AreaGroup}.
     */
    @RequestMapping(value = "/action/spatial/group", method = RequestMethod.GET)
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroups(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            List<AreaGroupEntity> groups = spatialRepository.getAreaGroupsByUtilityId(authenticatedUser.getUtilityKey());

            return new AreaGroupCollectionResponse(groups);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Returns all the areas of an area group.
     *
     * @param authenticatedUser the currently authenticated user.
     * @param groupKey the area group key.
     * @return a collection of {@link Area}.
     */
    @RequestMapping(value = "/action/spatial/group/{groupKey}/area", method = RequestMethod.GET)
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroupAreas(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable UUID groupKey) {
        try {
            List<AreaGroupMemberEntity> areas = spatialRepository.getAreasByAreaGroupKey(groupKey);

            return new AreaCollectionResponse(areas);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Returns the default area for a user.
     *
     * @param authenticatedUser the currently authenticated user.
     * @return a an instance of {@link AreaResponse}.
     */
    @RequestMapping(value = "/action/spatial/user/area", method = RequestMethod.GET)
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getUserDefaultArea(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            AreaGroupMemberEntity area = spatialRepository.getUserDefaultAreaByUserKey(authenticatedUser.getUtilityKey(),
                                                                                       authenticatedUser.getKey());

            return new AreaResponse(area);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Logs an exception and creates a response.
     *
     * @param ex the exception.
     * @return an instance of {@link RestResponse}.
     */
    private RestResponse handleException(Exception ex) {
        logger.error(ex.getMessage(), ex);

        if (ex instanceof ApplicationException) {
            return new RestResponse(getError(ex));
        }

        return new RestResponse(getError(SharedErrorCode.UNKNOWN));
    }

}
