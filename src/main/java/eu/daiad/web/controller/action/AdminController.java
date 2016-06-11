package eu.daiad.web.controller.action;

import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.admin.AccountActivity;
import eu.daiad.web.model.admin.AccountActivityResponse;
import eu.daiad.web.model.admin.CounterCollectionResponse;
import eu.daiad.web.model.group.GroupQuery;
import eu.daiad.web.model.group.GroupQueryRequest;
import eu.daiad.web.model.group.GroupQueryResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;

/**
 * Provides actions for performing administration tasks.
 */
@RestController
public class AdminController extends BaseController {

    private static final Log logger = LogFactory.getLog(AdminController.class);

    @Autowired
    private IGroupRepository groupRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Returns information about all trial user activity.
     * 
     * @param user the currently authenticated user.
     * @return the user activity.
     */
    @RequestMapping(value = "/action/admin/trial/activity", method = RequestMethod.GET, produces = "application/json")
    @Secured("ROLE_ADMIN")
    public RestResponse getTrialUserActivity(@AuthenticationPrincipal AuthenticatedUser user) {
        RestResponse response = null;

        try {
            AccountActivityResponse controllerResponse = new AccountActivityResponse();

            List<AccountActivity> records = userRepository.getAccountActivity(user.getUtilityId());

            for (AccountActivity a : records) {
                controllerResponse.getAccounts().add(a);
            }

            response = controllerResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response = new RestResponse();
            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Returns all available groups including clusters, segments and user
     * defined user groups. Optionally filters data.
     * 
     * @param request the query to filter data.
     * @return the selected groups.
     */
    @RequestMapping(value = "/action/admin/group/query", method = RequestMethod.POST, produces = "application/json")
    @Secured("ROLE_ADMIN")
    public RestResponse getGroups(@AuthenticationPrincipal AuthenticatedUser user,
                    @RequestBody GroupQueryRequest request) {
        RestResponse response = null;

        try {
            if (request == null) {
                request = new GroupQueryRequest();
            }
            if (request.getQuery() == null) {
                request.setQuery(new GroupQuery());
            }
            if (request.getQuery().getUtility() == null) {
                UUID utilityKey = utilityRepository.getUtilityById(user.getUtilityId()).getKey();

                request.getQuery().setUtility(utilityKey);
            }

            return new GroupQueryResponse(this.groupRepository.getAll(request.getQuery()));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response = new RestResponse();
            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Returns all available counter values
     * 
     * @return the selected groups.
     */
    @RequestMapping(value = "/action/admin/counter", method = RequestMethod.GET, produces = "application/json")
    @Secured("ROLE_ADMIN")
    public RestResponse getCounters(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            CounterCollectionResponse response = new CounterCollectionResponse();

            response.setCounters(this.utilityRepository.getCounters(user.getUtilityId()));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            RestResponse response = new RestResponse();
            response.add(this.getError(ex));

            return response;
        }
    }
}
