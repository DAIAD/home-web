package eu.daiad.utility.controller.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.amphiro.HistoricalToRealTimeRequest;
import eu.daiad.common.model.amphiro.IgnoreShowerRequest;
import eu.daiad.common.model.amphiro.MemberAssignmentRequest;
import eu.daiad.common.model.device.AmphiroDevice;
import eu.daiad.common.model.error.DeviceErrorCode;
import eu.daiad.common.model.error.QueryErrorCode;
import eu.daiad.common.model.query.DataQuery;
import eu.daiad.common.model.query.DataQueryCollectionResponse;
import eu.daiad.common.model.query.DataQueryRequest;
import eu.daiad.common.model.query.ForecastQuery;
import eu.daiad.common.model.query.ForecastQueryRequest;
import eu.daiad.common.model.query.StoreDataQueryRequest;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.RoleConstant;
import eu.daiad.common.repository.application.IAmphiroIndexOrderedRepository;
import eu.daiad.common.repository.application.IDeviceRepository;
import eu.daiad.common.service.IDataService;
import eu.daiad.utility.controller.BaseController;

/**
 * Provides methods for managing, querying and exporting data.
 */
@RestController
public class DataController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DataController.class);

    /**
     * Repository for accessing device data.
     */
    @Autowired
    private IDeviceRepository deviceRepository;

    /**
     * Repository for accessing amphiro b1 data indexed by shower id.
     */
    @Autowired
    private IAmphiroIndexOrderedRepository amphiroIndexOrderedRepository;

    /**
     * Service for querying smart water and amphiro b1 data in HBASE.
     */
    @Autowired
    private IDataService dataService;

    /**
     * Query amphiro b1 session data and smart water meter readings using one or more filtering
     * criteria. Depending on the search criteria, one or more data series may be returned.
     *
     * @param user the currently authenticated user.
     * @param data the data query.
     * @return the data series.
     */
    @RequestMapping(value = "/action/query", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse query(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody DataQueryRequest data) {
        try {
            DataQuery query = data.getQuery();
            if (query == null) {
                return createResponse(QueryErrorCode.EMPTY_QUERY);
            }

            if(StringUtils.isBlank(query.getTimezone())) {
                query.setTimezone(user.getTimezone());
            }

            return dataService.execute(query);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Saves a data query.
     *
     * @param user the user
     * @param request the data.
     * @return the result of the save operation.
     */
    @RequestMapping(value = "/action/data/query/store", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse storeQuery(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody StoreDataQueryRequest request) {
        try {

            List<DataQuery> queries = request.getNamedQuery().getQueries();

            if (queries == null || queries.isEmpty()) {
                return createResponse(QueryErrorCode.EMPTY_QUERY);
            }

            if (StringUtils.isBlank(queries.get(0).getTimezone())) {
                queries.get(0).setTimezone(user.getTimezone());
            }

            dataService.storeQuery(request.getNamedQuery(), user.getKey());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Saves a data query.
     *
     * @param user the user
     * @param request the data.
     * @return the result of the save operation.
     */
    @RequestMapping(value = "/action/data/query/update", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse updateStoredQuery(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody StoreDataQueryRequest request) {
        RestResponse response = new RestResponse();

        try {

            // Set defaults if needed
            List<DataQuery> queries = request.getNamedQuery().getQueries();
            if (queries != null && !queries.isEmpty()) {
                // Initialize time zone
                if (StringUtils.isBlank(queries.get(0).getTimezone())) {
                    queries.get(0).setTimezone(user.getTimezone());
                }
            }

            dataService.updateStoredQuery(request.getNamedQuery(), user.getKey());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Deletes a data query.
     *
     * @param user the user
     * @param request the data.
     * @return the result of the delete operation.
     */
    @RequestMapping(value = "/action/data/query/delete", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse deleteStoredQuery(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody StoreDataQueryRequest request) {
        RestResponse response = new RestResponse();

        try {

            dataService.deleteStoredQuery(request.getNamedQuery(), user.getKey());

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Pin query to dashboard.
     *
     * @param user the user
     * @param request the requested query to pin.
     * @return the result of the operation.
     */
    @RequestMapping(value = "/action/data/query/pin", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse pinQuery(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody StoreDataQueryRequest request) {
        RestResponse response = new RestResponse();

        try {

            dataService.pinStoredQuery(request.getNamedQuery().getId(), user.getKey());

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Unpin query from dashboard.
     *
     * @param user the user
     * @param request the requested query to unpin.
     * @return the result of the operation.
     */
    @RequestMapping(value = "/action/data/query/unpin", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse unpinQuery(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody StoreDataQueryRequest request) {

        RestResponse response = new RestResponse();

        try {

            dataService.unpinStoredQuery(request.getNamedQuery().getId(), user.getKey());

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Loads all saved data queries.
     *
     * @param user the user
     * @return the saved data queries
     */
    @RequestMapping(value = "/action/data/query/load", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getAllQueries(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            DataQueryCollectionResponse response = new DataQueryCollectionResponse();

            response.setQueries(dataService.getQueriesForOwner(user.getId()));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Returns forecasting results for a smart water meter
     *
     * @param data the query.
     * @return the data series.
     */
    @RequestMapping(value = "/action/data/meter/forecast", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse forecast(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody ForecastQueryRequest data) {
        try {
            ForecastQuery query = data.getQuery();
            if (query == null) {
                return createResponse(QueryErrorCode.EMPTY_QUERY);
            }

            if (StringUtils.isBlank(query.getTimezone())) {
                query.setTimezone(user.getTimezone());
            }

            return dataService.execute(query);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Assigns household members to amphiro b1 sessions.
     *
     * @param user the currently authenticated user.
     * @param request member assignment data
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/data/session/member", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse assignMemberToSession(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody MemberAssignmentRequest request) {
        RestResponse response = new RestResponse();

        try {
            amphiroIndexOrderedRepository.assignMember(user, request.getAssignments());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Marks an amphiro b1 message as not being a shower.
     *
     * @param user the currently authenticated user.
     * @param request shower data.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/data/session/ignore", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse invalidateShower(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody IgnoreShowerRequest request) {
        RestResponse response = new RestResponse();

        try {
            amphiroIndexOrderedRepository.ignore(user, request.getSessions());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Updates the date time of a historical shower and converts it to a real-time one.
     *
     * @param user the currently authenticated user.
     * @param request the shower data including its unique id and timestamp.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/data/session/date", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER })
    public RestResponse convertHistoricalToRealTimeShower(@AuthenticationPrincipal AuthenticatedUser user,
                                                          @RequestBody HistoricalToRealTimeRequest request) {
        RestResponse response = new RestResponse();

        try {
            AmphiroDevice device = deviceRepository.getUserAmphiroByKey(user.getKey(), request.getDeviceKey());
            if(device == null) {
                throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", request.getDeviceKey());
            }
            amphiroIndexOrderedRepository.toRealTime(user, device, request.getSessionId(), request.getTimestamp());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }
}
