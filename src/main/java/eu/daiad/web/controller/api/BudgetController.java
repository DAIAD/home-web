package eu.daiad.web.controller.api;

import java.util.UUID;

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
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.query.savings.BudgetExploreClusterResponse;
import eu.daiad.web.model.query.savings.BudgetExploreConsumerResponse;
import eu.daiad.web.model.query.savings.BudgetQueryRequest;
import eu.daiad.web.model.query.savings.BudgetQueryResponse;
import eu.daiad.web.model.query.savings.BudgetResponse;
import eu.daiad.web.model.query.savings.CreateBudgetRequest;
import eu.daiad.web.model.query.savings.CreateBudgetResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.service.savings.IBudgetService;

/**
 * Provides actions for managing budgets.
 */
@RestController("ApiBudgetController")
public class BudgetController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(BudgetController.class);

    /**
     * Service for computing and querying budgets.
     */
    @Autowired
    private IBudgetService budgetService;

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
     * Creates a new budget.
     *
     * @param request the budget options.
     * @return an instance of {@link CreateBudgetRequest}.
     */
    @RequestMapping(value = "/api/v1/budget", method = RequestMethod.PUT, produces = "application/json")
    public RestResponse create(@RequestBody CreateBudgetRequest request) {
        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            UUID key = budgetService.createBudget(user, request.getTitle(), request.getParameters());

            return new CreateBudgetResponse(key);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Refreshes an existing budget.
     *
     * @param budgetKey the budget Key.
     * @param year the reference date year.
     * @param month the reference date month.
     * @param request the user credentials.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/api/v1/budget/compute/{budgetKey}/{year}/{month}", method = RequestMethod.POST, produces = "application/json")
    public RestResponse scheduleSnapshotCreation(@PathVariable UUID budgetKey,
                                                 @PathVariable int year,
                                                 @PathVariable int month,
                                                 @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            budgetService.scheduleSnapshotCreation(budgetKey, year, month);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Loads a budget.
     *
     * @param budgetKey the budget Key.
     * @param request the user credentials.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/api/v1/budget/{budgetKey}", method = RequestMethod.POST, produces = "application/json")
    public RestResponse find(@PathVariable UUID budgetKey, @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            return new BudgetResponse(budgetService.find(budgetKey));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Query budget data.
     *
     * @param budgetKey the budget Key.
     * @param clusterKey the cluster key
     * @param request the user credentials.
     * @return an instance of {@link BudgetExploreClusterResponse}.
     */
    @RequestMapping(value = "/api/v1/budget/explore/cluster/{budgetKey}/{clusterKey}", method = RequestMethod.POST, produces = "application/json")
    public RestResponse exploreCluster(@PathVariable UUID budgetKey, @PathVariable UUID clusterKey, @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            return new BudgetExploreClusterResponse(budgetService.exploreCluster(budgetKey, clusterKey));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Query budget data.
     *
     * @param budgetKey the budget Key.
     * @param consumerKey the consumer key.
     * @param request the user credentials.
     * @return an instance of {@link BudgetExploreConsumerResponse}.
     */
    @RequestMapping(value = "/api/v1/budget/explore/consumer/{budgetKey}/{consumerKey}", method = RequestMethod.POST, produces = "application/json")
    public RestResponse exploreConsumer(@PathVariable UUID budgetKey, @PathVariable UUID consumerKey, @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            return new BudgetExploreConsumerResponse(budgetService.exploreConsumer(budgetKey, consumerKey));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Deletes a budget.
     *
     * @param budgetKey the budget Key.
     * @param request the user credentials.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/api/v1/budget/{budgetKey}", method = RequestMethod.DELETE, produces = "application/json")
    public RestResponse delete(@PathVariable UUID budgetKey, @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            budgetService.delete(budgetKey);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Activates a budget.
     *
     * @param budgetKey the budget Key.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/api/v1/budget/{budgetKey}/activate", method = RequestMethod.PUT, produces = "application/json")
    public RestResponse activate(@PathVariable UUID budgetKey, @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            budgetService.setActive(budgetKey, true);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Deactivates a budget.
     *
     * @param budgetKey the budget Key.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/api/v1/budget/{budgetKey}/deactivate", method = RequestMethod.PUT, produces = "application/json")
    public RestResponse deactivate(@PathVariable UUID budgetKey, @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            budgetService.setActive(budgetKey, false);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Query a budgets.
     *
     * @param request request of type {@link BudgetQueryRequest}.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/api/v1/budget/query", method = RequestMethod.POST, produces = "application/json")
    public RestResponse query(@RequestBody BudgetQueryRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            return new BudgetQueryResponse(budgetService.find(request.getQuery()));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

}
