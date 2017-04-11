package eu.daiad.web.controller.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.billing.PriceBracket;
import eu.daiad.web.model.billing.PriceBracketCollectionResult;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IBillingRepository;

/**
 * Provides actions for providing billing data.
 */
@RestController("ApiBillingController")
public class BillingController extends BaseRestController {

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
     * @param request instance of {@link AuthenticatedRequest}.
     * @return a collection {@link PriceBracket} objects.
     */
    @RequestMapping(value = "/api/v1/billing/price-bracket", method = RequestMethod.POST, produces = "application/json")
    public RestResponse getPriceBrackets(@RequestBody AuthenticatedRequest request) {
        try {
            AuthenticatedUser user = authenticate(request.getCredentials(),
                                                  EnumRole.ROLE_USER, EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

            return new PriceBracketCollectionResult(billingRepository.getPriceBracketByUtilityId(user.getUtilityId()));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Returns historical price bracket data.
     *
     * @param referenceDate reference date used for selecting the price bracket time interval.
     * @param request instance of {@link AuthenticatedRequest}.
     * @return a collection {@link PriceBracket} objects.
     */
    @RequestMapping(value = "/api/v1/billing/price-bracket/{referenceDate}", method = RequestMethod.POST, produces = "application/json")
    public RestResponse getPriceBrackets(@PathVariable String referenceDate, @RequestBody AuthenticatedRequest request) {
        try {
            AuthenticatedUser user = authenticate(request.getCredentials(),
                                                  EnumRole.ROLE_USER, EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

            return new PriceBracketCollectionResult(billingRepository.getPriceBracketByUtilityId(user.getUtilityId(), referenceDate));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

}
