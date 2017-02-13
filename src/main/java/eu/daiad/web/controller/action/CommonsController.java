package eu.daiad.web.controller.action;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.group.CommonsCollectionResponse;
import eu.daiad.web.model.group.CommonsCreateRequest;
import eu.daiad.web.model.group.CommonsCreateRestResponse;
import eu.daiad.web.model.group.CommonsQueryRequest;
import eu.daiad.web.model.group.CommonsQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.repository.application.ICommonsRepository;

/**
 * Provides actions for commons creation and management.
 */
@RestController
public class CommonsController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(CommonsController.class);

    /**
     * Repository for accessing commons data.
     */
    @Autowired
    private ICommonsRepository commonsRepository;

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

    /**
     * Returns the commons with the given key.
     *
     * @param user the authenticated user.
     * @param commonsKey the commons key.
     * @return a collection {@link CommonsCollectionResponse} with a single element.
     */
    @RequestMapping(value = "/action/commons/{commonsKey}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse getCommons(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID commonsKey) {
        try {
            return new CommonsCollectionResponse(commonsRepository.getCommonsByKey(user.getKey(), commonsKey));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Creates a new commons.
     *
     * @param user the authenticated user.
     * @param request the new commons properties.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/commons", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse createCommons(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody CommonsCreateRequest request) {
        try {
            UUID key = commonsRepository.create(user.getKey(), request);

            return new CommonsCreateRestResponse(key);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Updates an existing commons.
     *
     * @param user the authenticated user.
     * @param commonsKey the key of the commons to update.
     * @param request the commons updated properties.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/commons/{commonsKey}", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse updateCommons(@AuthenticationPrincipal AuthenticatedUser user,
                                      @PathVariable UUID commonsKey,
                                      @RequestBody CommonsCreateRequest request) {
        try {
            commonsRepository.update(user.getKey(), commonsKey, request);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Deletes an existing commons.
     *
     * @param user the authenticated user.
     * @param commonsKey the key of the commons to update.
     * @param request the commons updated properties.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/commons/{commonsKey}", method = RequestMethod.DELETE, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse deleteCommons(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID commonsKey) {
        try {
            commonsRepository.remove(user.getKey(), commonsKey);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Adds a user to a commons.
     *
     * @param user the authenticated user.
     * @param commonsKey the key of the commons.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/commons/{commonsKey}/join", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse join(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID commonsKey) {
        try {
            commonsRepository.join(user.getKey(), commonsKey);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Removes a user from a commons.
     *
     * @param user the authenticated user.
     * @param commonsKey the key of the commons.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/commons/{commonsKey}/leave", method = RequestMethod.DELETE, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse leave(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID commonsKey) {
        try {
            commonsRepository.leave(user.getKey(), commonsKey);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Searches for commons.
     *
     * @param user the user who submits the query.
     * @param request the request.
     * @return a {@link CommonsCollectionResponse} collection.
     */
    @RequestMapping(value = "/action/commons", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public @ResponseBody RestResponse search(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody CommonsQueryRequest request) {
        try {
            if ((request.getQuery().getPageIndex() == null) || (request.getQuery().getPageIndex() < 0)) {
                request.getQuery().setPageIndex(0);
            }
            if ((request.getQuery().getPageSize() == null) || (request.getQuery().getPageSize() < 1)) {
                request.getQuery().setPageSize(10);
            }
            CommonsQueryResult result = commonsRepository.search(user.getKey(), request.getQuery());

            return new CommonsCollectionResponse(result.getGroups(), result.getPageIndex(), result.getPageSize(), result.getCount());
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Gets authenticated user all commons.
     *
     * @param user the user who submits the query.
     * @return a {@link CommonsCollectionResponse} collection.
     */
    @RequestMapping(value = "/action/commons/membership", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public @ResponseBody RestResponse getCommonsMembership(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            return new CommonsCollectionResponse(commonsRepository.getCommonsByUserKey(user.getKey()));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

}
