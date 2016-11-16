package eu.daiad.web.controller.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.model.utility.UtilityInfoResponse;
import eu.daiad.web.repository.application.IUtilityRepository;

/**
 * Provides actions for querying utility data.
 */
@RestController
public class UtilityController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(UtilityController.class);

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Get all utilities accessible to the authenticated user.
     *
     * @param user the currently authenticated user.
     * @return the utilities.
     */
    @RequestMapping(value = "/action/utility/all", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getUtilityInfo(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            List<UtilityInfo> utilities = utilityRepository.getUtilities();

            for (int index = utilities.size() - 1; index >= 0; index--) {
                if (!user.canAccessUtility(utilities.get(index).getId())) {
                    utilities.remove(index);
                }
            }

            return new UtilityInfoResponse(utilities);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Get the utility of the authenticated user.
     *
     * @param user the currently authenticated user.
     * @return a list of utilities with a single member.
     */
    @RequestMapping(value = "/action/utility/current", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getCorrespondingUtilities(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            UtilityInfo utility = utilityRepository.getUtilityById(user.getUtilityId());

            return new UtilityInfoResponse(utility);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

}