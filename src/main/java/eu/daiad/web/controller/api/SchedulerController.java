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
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.scheduling.LaunchJobRequest;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.service.scheduling.ISchedulerService;

/**
 * Provides actions for managing jobs.
 */
@RestController("ApiSchedulerController")
public class SchedulerController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(SchedulerController.class);

    /**
     * Service for querying, scheduling and launching jobs.
     */
    @Autowired
    private ISchedulerService schedulerService;

    /**
     * Launches a job by its name.
     *
     * @param request authentication information and custom job parameters.
     * @param jobName the name of the job.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/admin/scheduler/launch/{jobName}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse launch(@RequestBody LaunchJobRequest request, @PathVariable String jobName) {
        RestResponse response = new RestResponse();

        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN);

            schedulerService.launch(jobName, request.getParameters());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

}
