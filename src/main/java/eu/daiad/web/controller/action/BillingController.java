package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.billing.PriceBracket;
import eu.daiad.web.model.billing.PriceBracketCollectionResult;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.repository.application.IBillingRepository;

/**
 * Provides actions for providing billing data.
 */
@RestController
public class BillingController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(BillingController.class);

    /**
     * Repository for accessing billing data.
     */
    @Autowired
    private IBillingRepository billingRepository;

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
     * Returns the currently applicable price brackets.
     *
     * @param user the authenticated user.
     * @return a collection {@link PriceBracket} objects.
     */
    @RequestMapping(value = "/action/billing/price-bracket", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getPriceBrackets(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            return new PriceBracketCollectionResult(billingRepository.getPriceBracketByUtilityId(user.getUtilityId()));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Returns historical price bracket data.
     *
     * @param user the authenticated user.
     * @param referenceDate reference date used for selecting the price bracket time interval.
     * @return a collection {@link PriceBracket} objects.
     */
    @RequestMapping(value = "/action/billing/price-bracket/{referenceDate}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_USER, RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getPriceBrackets(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String referenceDate) {
        try {
            return new PriceBracketCollectionResult(billingRepository.getPriceBracketByUtilityId(user.getUtilityId(), referenceDate));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

}
