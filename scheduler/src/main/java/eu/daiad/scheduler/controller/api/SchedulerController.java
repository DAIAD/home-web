package eu.daiad.scheduler.controller.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.common.domain.admin.ScheduledJobExecutionEntity;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.scheduling.ExecutionMessageResponse;
import eu.daiad.common.model.scheduling.ExecutionQuery;
import eu.daiad.common.model.scheduling.ExecutionQueryRequest;
import eu.daiad.common.model.scheduling.ExecutionQueryResponse;
import eu.daiad.common.model.scheduling.ExecutionQueryResult;
import eu.daiad.common.model.scheduling.JobCollectionResponse;
import eu.daiad.common.model.scheduling.JobExecutionInfo;
import eu.daiad.common.model.scheduling.JobInfo;
import eu.daiad.common.model.scheduling.JobResponse;
import eu.daiad.common.model.scheduling.LaunchJobRequest;
import eu.daiad.scheduler.controller.BaseController;
import eu.daiad.scheduler.service.scheduling.ISchedulerService;

/**
 * Provides actions for managing jobs.
 */
@RestController
public class SchedulerController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(SchedulerController.class);

    /**
     * Server time zone.
     */
    @Value("${daiad.batch.server-time-zone:Europe/Athens}")
    private String serverTimeZone;

    /**
     * Scheduler service used for querying, scheduling and launching jobs.
     */
    @Autowired
    private ISchedulerService schedulerService;

    /**
     * Gets all registered jobs.
     *
     * @return the jobs.
     */
	@GetMapping(value = "/api/v1/scheduler/jobs")
    public RestResponse getJobs() {
        try {
            List<JobInfo> allJobs = schedulerService.getJobs();

            // Hide selected jobs from the UI
            List<JobInfo> visibleJobs = new ArrayList<JobInfo>();
            for(JobInfo job : allJobs) {
                if(job.isVisible()) {
                    visibleJobs.add(job);
                }
            }

            return new JobCollectionResponse(visibleJobs);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Returns all job executions, optionally filtered by a query.
     *
     * @param request query for filtering job executions.
     * @return the executions.
     */
	@PostMapping(value = "/api/v1/scheduler/executions")
    public RestResponse getExecutions(@RequestBody ExecutionQueryRequest request) {
        try {
            // Set default values
            if (request.getQuery() == null) {
                request.setQuery(new ExecutionQuery());
            }
            if ((request.getQuery().getIndex() == null) || (request.getQuery().getIndex() < 0)) {
                request.getQuery().setIndex(0);
            }
            if (request.getQuery().getSize() == null) {
                request.getQuery().setSize(10);
            }

            ExecutionQueryResult result = schedulerService.getJobExecutions(request.getQuery());

            ExecutionQueryResponse response = new ExecutionQueryResponse();

            response.setTotal(result.getTotal());

            response.setIndex(request.getQuery().getIndex());
            response.setSize(request.getQuery().getSize());

            List<JobExecutionInfo> executions = new ArrayList<JobExecutionInfo>();

            DateTime utcDateTime;

            for (ScheduledJobExecutionEntity entity : result.getExecutions()) {
                JobExecutionInfo e = new JobExecutionInfo();

                if (entity.getStartedOn() != null) {
                    utcDateTime = entity.getStartedOn().toDateTime(DateTimeZone.forID(serverTimeZone));
                    e.setStartedOn(utcDateTime.getMillis());
                }
                if (entity.getCompletedOn() != null) {
                    utcDateTime = entity.getCompletedOn().toDateTime(DateTimeZone.forID(serverTimeZone));
                    e.setCompletedOn(utcDateTime.getMillis());
                }
                e.setExecutionId(entity.getJobExecutionId());
                e.setExitCode(entity.getExitCode());
                e.setInstanceId(entity.getJobInstanceId());
                e.setJobId(entity.getJobId());
                e.setStatusCode(entity.getStatusCode());
                e.setJobName(entity.getJobName());

                executions.add(e);
            }
            response.setExecutions(executions);

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Loads a job by name
     *
     * @param jobName the job name
     * @return the job
     */
    @GetMapping(value = "/api/v1/scheduler/job/{jobName}")
    public RestResponse getJob(@PathVariable String jobName) {
        try {
            JobResponse controllerResponse = new JobResponse();

            controllerResponse.setJob(schedulerService.getJob(jobName));

            return controllerResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }
    
    /**
     * Loads a job based on its id and a subset of its executions.
     *
     * @param jobId the job id.
     * @param startPosition the execution start index.
     * @param maxResult the maximum number of executions to return.
     * @return the job and its executions.
     */
    @GetMapping(value = "/api/v1/scheduler/job/{jobId}/{startPosition}/{maxResult}")
    public RestResponse getJob(@PathVariable long jobId, @PathVariable int startPosition, @PathVariable int maxResult) {
        try {
            JobResponse controllerResponse = new JobResponse();

            controllerResponse.setJob(schedulerService.getJob(jobId));
            controllerResponse.setExecutions(schedulerService.getJobExecutions(jobId, startPosition, maxResult));

            return controllerResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Enables a job by its id.
     *
     * @param jobId the job id.
     * @return the controller's response.
     */
    @PutMapping(value = "/api/v1/scheduler/job/enable/{jobId}")
    public RestResponse enableJob(@PathVariable long jobId) {
        try {
            schedulerService.enable(jobId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Disables a job by its id.
     *
     * @param jobId the job id.
     * @return the controller's response.
     */
    @PutMapping(value = "/api/v1/scheduler/job/disable/{jobId}")
    public RestResponse disableJob(@PathVariable long jobId) {
        try {
            schedulerService.disable(jobId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Launches a job by its id.
     *
     * @param jobId the job id.
     * @return the controller's response.
     */
    @PutMapping(value = "/api/v1/scheduler/job/launch/{jobId}")
    public RestResponse launch(@PathVariable long jobId) {
        try {
            schedulerService.launch(jobId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Launches a job by its name.
     *
     * @param request authentication information and custom job parameters.
     * @param jobName the name of the job.
     * @return the controller's response.
     */
    @PutMapping(value = "/api/v1/scheduler/job/launch-by-name/{jobName}")
    public RestResponse launch(@RequestBody LaunchJobRequest request, @PathVariable String jobName) {
        RestResponse response = new RestResponse();

        try {
            schedulerService.launch(jobName, request.getParameters());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }
    
    /**
     * Returns the message of an execution by its id.
     *
     * @param executionId the execution id.
     * @return the execution message.
     */
    @GetMapping(value = "/api/v1/scheduler/execution/{executionId}/message")
    public RestResponse getExecutionMessage(@PathVariable long executionId) {
        try {
            ExecutionMessageResponse response = new ExecutionMessageResponse();

            response.setMessage(schedulerService.getExecutionMessage(executionId));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Sends a message to stop a job execution. The scheduler does not
     * guarantees immediate job termination.
     *
     * @param executionId the execution id.
     * @return the controller's response.
     */
	@DeleteMapping(value = "/api/v1/scheduler/execution/stop/{executionId}")
	public RestResponse stopExecution(@PathVariable long executionId) {
		try {
			schedulerService.stop(executionId);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			return new RestResponse(this.getError(ex));
		}

		return new RestResponse();
	}

}
