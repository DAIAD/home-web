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
import eu.daiad.web.model.query.savings.CrateSavingScenarioResponse;
import eu.daiad.web.model.query.savings.CreateSavingScenarioRequest;
import eu.daiad.web.model.query.savings.SavingScenarioExploreResponse;
import eu.daiad.web.model.query.savings.SavingScenarioQueryRequest;
import eu.daiad.web.model.query.savings.SavingScenarioQueryResponse;
import eu.daiad.web.model.query.savings.SavingScenarioResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.service.savings.ISavingsPotentialService;

/**
 * Provides actions for managing savings potential scenarios.
 */
@RestController("ActionSavingsController")
public class SavingsController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(SavingsController.class);

    /**
     * Service for computing and querying savings potential scenarios.
     */
    @Autowired
    private ISavingsPotentialService savingsPotentialService;

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
     * Creates a new savings scenario.
     *
     * @param user the authenticated user.
     * @param request the scenario options.
     * @return an instance of {@link CrateSavingScenarioResponse}.
     */
    @RequestMapping(value = "/action/savings", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse create(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody CreateSavingScenarioRequest request) {
        try {
            UUID key = savingsPotentialService.create(user, request.getTitle(), request.getParameters());

            return new CrateSavingScenarioResponse(key);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Refreshes an existing new savings scenario.
     *
     * @param user the authenticated user.
     * @param scenarioKey the scenario Key.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/savings/refresh/{scenarioKey}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse refresh(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID scenarioKey) {
        try {
            savingsPotentialService.refresh(scenarioKey);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Loads a savings scenario.
     *
     * @param user the authenticated user.
     * @param scenarioKey the scenario Key.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/savings/{scenarioKey}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse find(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID scenarioKey) {
        try {
            return new SavingScenarioResponse(savingsPotentialService.find(scenarioKey));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Refreshes an existing new savings scenario.
     *
     * @param user the authenticated user.
     * @param scenarioKey the scenario Key.
     * @param clusterKey the cluster key
     * @return an instance of {@link SavingScenarioExploreResponse}.
     */
    @RequestMapping(value = "/action/savings/explore/{scenarioKey}/{clusterKey}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse explore(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID scenarioKey, @PathVariable UUID clusterKey) {
        try {
            return new SavingScenarioExploreResponse(savingsPotentialService.explore(scenarioKey, clusterKey));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Deletes a savings scenario.
     *
     * @param user the authenticated user.
     * @param scenarioKey the scenario Key.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/savings/{scenarioKey}", method = RequestMethod.DELETE, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID scenarioKey) {
        try {
            savingsPotentialService.delete(scenarioKey);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Deletes a savings scenario.
     *
     * @param user the authenticated user.
     * @param request request of type {@link SavingScenarioQueryRequest}.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/savings/query", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN })
    public RestResponse query(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody SavingScenarioQueryRequest request) {
        try {
            return new SavingScenarioQueryResponse(savingsPotentialService.find(request.getQuery()));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }


}
