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
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
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
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.service.savings.IBudgetService;

/**
 * Provides actions for managing budgets.
 */
@RestController("ActionBudgetController")
public class BudgetController extends BaseController {

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
     * @param user the authenticated user.
     * @param request the budget options.
     * @return an instance of {@link CreateBudgetRequest}.
     */
    @RequestMapping(value = "/action/budget", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse create(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody CreateBudgetRequest request) {
        try {
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
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/budget/compute/{budgetKey}/{year}/{month}", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse scheduleSnapshotCreation(@PathVariable UUID budgetKey,
                                                 @PathVariable int year,
                                                 @PathVariable int month) {
        try {
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
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/budget/{budgetKey}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse find(@PathVariable UUID budgetKey) {
        try {
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
     * @return an instance of {@link BudgetExploreClusterResponse}.
     */
    @RequestMapping(value = "/action/budget/explore/cluster/{budgetKey}/{clusterKey}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse exploreCluster(@PathVariable UUID budgetKey, @PathVariable UUID clusterKey) {
        try {
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
     * @return an instance of {@link BudgetExploreConsumerResponse}.
     */
    @RequestMapping(value = "/action/budget/explore/consumer/{budgetKey}/{consumerKey}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse exploreConsumer(@PathVariable UUID budgetKey, @PathVariable UUID consumerKey) {
        try {
            return new BudgetExploreConsumerResponse(budgetService.exploreConsumer(budgetKey, consumerKey));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Deletes a budget.
     *
     * @param budgetKey the budget Key.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/budget/{budgetKey}", method = RequestMethod.DELETE, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse delete(@PathVariable UUID budgetKey) {
        try {
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
    @RequestMapping(value = "/action/budget/{budgetKey}/activate", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse activate(@PathVariable UUID budgetKey) {
        try {
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
    @RequestMapping(value = "/action/budget/{budgetKey}/deactivate", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse deactivate(@PathVariable UUID budgetKey) {
        try {
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
    @RequestMapping(value = "/action/budget/query", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse query(@RequestBody BudgetQueryRequest request) {
        try {
            return new BudgetQueryResponse(budgetService.find(request.getQuery()));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

}
