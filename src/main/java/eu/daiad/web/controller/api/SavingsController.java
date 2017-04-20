package eu.daiad.web.controller.api;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import eu.daiad.web.model.query.savings.CrateSavingScenarioResponse;
import eu.daiad.web.model.query.savings.CreateSavingScenarioRequest;
import eu.daiad.web.model.query.savings.SavingScenarioExploreResponse;
import eu.daiad.web.model.query.savings.SavingScenarioQueryRequest;
import eu.daiad.web.model.query.savings.SavingScenarioQueryResponse;
import eu.daiad.web.model.query.savings.SavingScenarioResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.service.savings.ISavingsPotentialService;

/**
 * Provides actions for managing savings potential scenarios.
 */
@RestController("ApiSavingsController")
public class SavingsController extends BaseRestController {

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
     * @param request the scenario options.
     * @return an instance of {@link CrateSavingScenarioResponse}.
     */
    @RequestMapping(value = "/api/v1/savings", method = RequestMethod.PUT, produces = "application/json")
    public RestResponse create(@RequestBody CreateSavingScenarioRequest request) {
        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            UUID key = savingsPotentialService.create(user, request.getTitle(), request.getParameters());

            return new CrateSavingScenarioResponse(key);
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Refreshes an existing savings scenario.
     *
     * @param scenarioKey the scenario Key.
     * @param request user credentials.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/api/v1/savings/refresh/{scenarioKey}", method = RequestMethod.POST, produces = "application/json")
    public RestResponse refresh(@PathVariable UUID scenarioKey, @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            savingsPotentialService.refresh(scenarioKey);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Loads a savings scenario.
     *
     * @param scenarioKey the scenario Key.
     * @param request user credentials.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/api/v1/savings/{scenarioKey}", method = RequestMethod.POST, produces = "application/json")
    public RestResponse find(@PathVariable UUID scenarioKey, @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            return new SavingScenarioResponse(savingsPotentialService.find(scenarioKey));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }


    /**
     * Query savings scenario consumer data.
     *
     * @param scenarioKey the scenario Key.
     * @param clusterKey the cluster key
     * @return an instance of {@link SavingScenarioExploreResponse}.
     */
    @RequestMapping(value = "/api/v1/savings/explore/{scenarioKey}/{clusterKey}", method = RequestMethod.POST, produces = "application/json")
    public RestResponse explore(@PathVariable UUID scenarioKey, @PathVariable UUID clusterKey, @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            return new SavingScenarioExploreResponse(savingsPotentialService.explore(scenarioKey, clusterKey));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }


    /**
     * Deletes a savings scenario.
     *
     * @param scenarioKey the scenario Key.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/api/v1/savings/{scenarioKey}", method = RequestMethod.DELETE, produces = "application/json")
    public RestResponse delete(@PathVariable UUID scenarioKey, @RequestBody AuthenticatedRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            savingsPotentialService.delete(scenarioKey);

            return new RestResponse();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Query savings scenarios.
     *
     * @param request request of type {@link SavingScenarioQueryRequest}.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/api/v1/savings/query", method = RequestMethod.POST, produces = "application/json")
    public RestResponse query(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody SavingScenarioQueryRequest request) {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_UTILITY_ADMIN);

            return new SavingScenarioQueryResponse(savingsPotentialService.find(request.getQuery()));
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

}
