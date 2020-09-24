package eu.daiad.utility.controller.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.scheduling.ExecutionMessageResponse;
import eu.daiad.common.model.scheduling.ExecutionQuery;
import eu.daiad.common.model.scheduling.ExecutionQueryRequest;
import eu.daiad.common.model.scheduling.ExecutionQueryResponse;
import eu.daiad.common.model.scheduling.JobCollectionResponse;
import eu.daiad.common.model.scheduling.JobInfo;
import eu.daiad.common.model.scheduling.JobResponse;
import eu.daiad.common.model.security.RoleConstant;
import eu.daiad.utility.controller.BaseController;
import eu.daiad.utility.feign.client.SchedulerFeignClient;
import feign.FeignException;

/**
 * Provides actions for query, scheduling and launching jobs.
 */
@RestController
public class SchedulerController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(SchedulerController.class);

    /**
     * Scheduler client used for querying, scheduling and launching jobs.
     */
    @Autowired
    private ObjectProvider<SchedulerFeignClient> schedulerClient;

    /**
     * Gets all registered jobs.
     *
     * @return the jobs.
     */
    @RequestMapping(value = "/action/scheduler/jobs", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getJobs() {
        try {
        	final ResponseEntity<JobCollectionResponse> e = this.schedulerClient.getObject().getJobs();       	
            
            final JobCollectionResponse res = e.getBody();
            
            List<JobInfo> allJobs = res.getJobs();

            // Hide selected jobs from the UI
            List<JobInfo> visibleJobs = new ArrayList<JobInfo>();
            for(JobInfo job : allJobs) {
                if(job.isVisible()) {
                    visibleJobs.add(job);
                }
            }

            return new JobCollectionResponse(visibleJobs);
        } catch (final FeignException fex) {
        	logger.error(fex.getMessage(), fex);
        	
            final SharedErrorCode code = SharedErrorCode.fromStatusCode(fex.status());

            return RestResponse.error(code, "An error has occurred");
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
    @RequestMapping(value = "/action/scheduler/executions", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
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

        	final ResponseEntity<ExecutionQueryResponse> e = this.schedulerClient.getObject().getExecutions(request);       	
            
            final ExecutionQueryResponse res = e.getBody();
                       
            ExecutionQueryResponse response = new ExecutionQueryResponse();

            response.setTotal(res.getTotal());

            response.setIndex(request.getQuery().getIndex());
            response.setSize(request.getQuery().getSize());
            response.setExecutions(res.getExecutions());

            return response;
        } catch (final FeignException fex) {
        	logger.error(fex.getMessage(), fex);
        	
            final SharedErrorCode code = SharedErrorCode.fromStatusCode(fex.status());

            return RestResponse.error(code, "An error has occurred");
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
    @RequestMapping(value = "/action/scheduler/job/{jobId}/{startPosition}/{maxResult}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getJob(long jobId, int startPosition, int maxResult) {
        try {
        	final ResponseEntity<JobResponse> e = this.schedulerClient.getObject().getJob(jobId, startPosition, maxResult);       	
          
            return e.getBody();
        } catch (final FeignException fex) {
        	logger.error(fex.getMessage(), fex);
        	
            final SharedErrorCode code = SharedErrorCode.fromStatusCode(fex.status());

            return RestResponse.error(code, "An error has occurred");
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
    @RequestMapping(value = "/action/scheduler/job/enable/{jobId}", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse enableJob(@PathVariable long jobId) {
        try {
        	this.schedulerClient.getObject().enableJob(jobId);       	
        } catch (final FeignException fex) {
        	logger.error(fex.getMessage(), fex);
        	
            final SharedErrorCode code = SharedErrorCode.fromStatusCode(fex.status());

            return RestResponse.error(code, "An error has occurred");
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
    @RequestMapping(value = "/action/scheduler/job/disable/{jobId}", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse disableJob(@PathVariable long jobId) {
        try {
        	this.schedulerClient.getObject().disableJob(jobId);
        } catch (final FeignException fex) {
        	logger.error(fex.getMessage(), fex);
        	
            final SharedErrorCode code = SharedErrorCode.fromStatusCode(fex.status());

            return RestResponse.error(code, "An error has occurred");
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
    @RequestMapping(value = "/action/scheduler/job/launch/{jobId}", method = RequestMethod.PUT, produces = "application/json")
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse launchJob(@PathVariable long jobId) {
        try {
        	this.schedulerClient.getObject().launch(jobId);
        } catch (final FeignException fex) {
        	logger.error(fex.getMessage(), fex);
        	
            final SharedErrorCode code = SharedErrorCode.fromStatusCode(fex.status());

            return RestResponse.error(code, "An error has occurred");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Returns the message of an execution by its id.
     *
     * @param executionId the execution id.
     * @return the execution message.
     */
    @RequestMapping(value = "/action/scheduler/execution/{executionId}/message/'", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getExecutionMessage(@PathVariable long executionId) {
        try {
        	final ResponseEntity<ExecutionMessageResponse> e = this.schedulerClient.getObject().getExecutionMessage(executionId);       	
            
            return e.getBody();
        } catch (final FeignException fex) {
        	logger.error(fex.getMessage(), fex);
        	
            final SharedErrorCode code = SharedErrorCode.fromStatusCode(fex.status());

            return RestResponse.error(code, "An error has occurred");
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
    @RequestMapping(value = "/action/scheduler/execution/stop/{executionId}", method = RequestMethod.DELETE, produces = "application/json")
    @Secured({ RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse stopExecution(long executionId) {
        try {
        	this.schedulerClient.getObject().stopExecution(executionId);
        } catch (final FeignException fex) {
        	logger.error(fex.getMessage(), fex);
        	
            final SharedErrorCode code = SharedErrorCode.fromStatusCode(fex.status());

            return RestResponse.error(code, "An error has occurred");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(this.getError(ex));
        }

        return new RestResponse();
    }
}
