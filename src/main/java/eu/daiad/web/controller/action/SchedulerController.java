package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.scheduling.JobCollectionResponse;
import eu.daiad.web.model.scheduling.JobResponse;
import eu.daiad.web.scheduling.ISchedulerService;

@RestController
public class SchedulerController extends BaseController {

	private static final Log logger = LogFactory.getLog(AdminController.class);

	@Autowired
	private ISchedulerService jobService;

	@RequestMapping(value = "/action/scheduler/jobs", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse getJobs() {
		RestResponse response = null;

		try {
			return new JobCollectionResponse(this.jobService.getJobs());
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response = new RestResponse();
			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/scheduler/job/{jobId}/{startPosition}/{maxResult}", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse getJob(long jobId, int startPosition, int maxResult) {
		RestResponse response = null;

		try {
			JobResponse controllerResponse = new JobResponse();

			controllerResponse.setJob(this.jobService.getJob(jobId));
			controllerResponse.setExecutions(this.jobService.getJobExecutions(jobId, startPosition, maxResult));

			return controllerResponse;
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response = new RestResponse();
			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/scheduler/job/enable/{jobId}", method = RequestMethod.PUT, produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse enableJob(long jobId) {
		RestResponse response = new RestResponse();

		try {
			this.jobService.enable(jobId);
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/scheduler/job/disable/{jobId}", method = RequestMethod.PUT, produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse disableJob(long jobId) {
		RestResponse response = new RestResponse();

		try {
			this.jobService.disable(jobId);
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/action/scheduler/execution/stop/{executionId}", method = RequestMethod.DELETE, produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse stopExecution(long executionId) {
		RestResponse response = new RestResponse();

		try {
			this.jobService.stop(executionId);
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}
}
