package eu.daiad.web.controller.action;

import java.util.List;

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
import eu.daiad.web.model.group.GroupQueryRequest;
import eu.daiad.web.model.group.GroupQueryResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;

/**
 * Provides actions for performing administration tasks.
 */
@RestController
public class AdminController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(AdminController.class);

    /**
     * Repository for accessing group data.
     */
    @Autowired
    private IGroupRepository groupRepository;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Returns information about all trial user activity.
     *
     * @param user the currently authenticated user.
     * @return the user activity.
     */
    @RequestMapping(value = "/action/admin/user/activity", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getUserActivity(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            AccountActivityResponse response = new AccountActivityResponse();

            List<AccountActivity> records = userRepository.getAccountActivity(user.getUtilityId());

            for (AccountActivity a : records) {
                response.getAccounts().add(a);
            }

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Returns all available groups including clusters, segments and user
     * defined user groups. Optionally filters data.
     *
     * @param request the query to filter data.
     * @return the selected groups.
     */
    @RequestMapping(value = "/action/admin/group/query", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroups(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody GroupQueryRequest request) {

        try {
            GroupQueryResponse response = new GroupQueryResponse();

            response.setGroups(this.groupRepository.getAll(user.getUtilityKey()));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Returns all available counter values
     *
     * @return the selected groups.
     */
    @RequestMapping(value = "/action/admin/counter", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getCounters(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            CounterCollectionResponse response = new CounterCollectionResponse();

            response.setCounters(this.utilityRepository.getCounters(user.getUtilityId()));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }
}
