package eu.daiad.utility.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.scheduling.ExecutionMessageResponse;
import eu.daiad.common.model.scheduling.ExecutionQueryRequest;
import eu.daiad.common.model.scheduling.ExecutionQueryResponse;
import eu.daiad.common.model.scheduling.JobCollectionResponse;
import eu.daiad.common.model.scheduling.JobResponse;
import eu.daiad.common.model.scheduling.LaunchJobRequest;

@FeignClient(
    name = "${daiad.feign.scheduler-service.name}",
    url = "${daiad.feign.scheduler-service.url}"
)
public interface SchedulerFeignClient {

    /**
     * Gets all registered jobs.
     *
     * @return the jobs.
     */
	@GetMapping(value = "/api/v1/scheduler/jobs")
	ResponseEntity<JobCollectionResponse> getJobs();

    /**
     * Returns all job executions, optionally filtered by a query.
     *
     * @param request query for filtering job executions.
     * @return the executions.
     */
	@PostMapping(value = "/api/v1/scheduler/executions")
	ResponseEntity<ExecutionQueryResponse> getExecutions(@RequestBody ExecutionQueryRequest request);

    /**
     * Loads a job based on its id and a subset of its executions.
     *
     * @param jobId the job id.
     * @param startPosition the execution start index.
     * @param maxResult the maximum number of executions to return.
     * @return the job and its executions.
     */
	@GetMapping(value = "/api/v1/scheduler/job/{jobId}/{startPosition}/{maxResult}")
	ResponseEntity<JobResponse> getJob(@PathVariable long jobId, @PathVariable int startPosition, @PathVariable int maxResult);

    /**
     * Loads a job based on its name
     *
	 * @param jobName the job name.
	 * @return the job with the given name.
     */
	@GetMapping(value = "/api/v1/scheduler/job/{jobName}")
	ResponseEntity<JobResponse> getJob(@PathVariable String jobName);

	
    /**
     * Enables a job by its id.
     *
     * @param jobId the job id.
     * @return the controller's response.
     */
	@PutMapping(value = "/api/v1/scheduler/job/enable/{jobId}")
	ResponseEntity<RestResponse> enableJob(@PathVariable long jobId);

    /**
     * Disables a job by its id.
     *
     * @param jobId the job id.
     * @return the controller's response.
     */
	@PutMapping(value = "/api/v1/scheduler/job/disable/{jobId}")
	ResponseEntity<RestResponse> disableJob(@PathVariable long jobId);

    /**
     * Launches a job by its id.
     *
     * @param jobId the job id.
     * @return the controller's response.
     */
	@PutMapping(value = "/api/v1/scheduler/job/launch/{jobId}")
	ResponseEntity<RestResponse> launch(@PathVariable long jobId);

    /**
     * Launches a job by its name.
     *
     * @param request authentication information and custom job parameters.
     * @param jobName the name of the job.
     * @return the controller's response.
     */
    @PostMapping(value = "/api/v1/scheduler/job/launch-by-name/{jobName}")
    ResponseEntity<RestResponse> launch(@RequestBody LaunchJobRequest request, @PathVariable String jobName);


    /**
     * Returns the message of an execution by its id.
     *
     * @param executionId the execution id.
     * @return the execution message.
     */
	@GetMapping(value = "/api/v1/scheduler/execution/{executionId}/message")
	ResponseEntity<ExecutionMessageResponse> getExecutionMessage(@PathVariable long executionId);

    /**
     * Sends a message to stop a job execution. The scheduler does not
     * guarantees immediate job termination.
     *
     * @param executionId the execution id.
     * @return the controller's response.
     */
	@DeleteMapping(value = "/api/v1/scheduler/execution/stop/{executionId}")
	ResponseEntity<RestResponse> stopExecution(long executionId);

}
