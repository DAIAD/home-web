package eu.daiad.web.controller.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.group.GroupQueryRequest;
import eu.daiad.web.model.group.GroupQueryResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IGroupRepository;

/**
 * Provides actions for performing administration tasks.
 */
@RestController("ApiAdminController")
public class AdminController extends BaseRestController {

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
     * Returns all available groups including clusters, segments and user
     * defined user groups. Optionally filters data.
     *
     * @param request the query to filter data.
     * @return the selected groups.
     */
    @RequestMapping(value = "/api/v1/admin/group/query", method = RequestMethod.POST, produces = "application/json")
    public RestResponse getGroups(@RequestBody GroupQueryRequest request) {
        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

            GroupQueryResponse response = new GroupQueryResponse();

            response.setGroups(groupRepository.getAll(user.getUtilityKey()));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

}
