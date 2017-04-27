package eu.daiad.web.service.scheduling;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.job.builder.IJobBuilder;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.scheduling.ExecutionQuery;
import eu.daiad.web.model.scheduling.ExecutionQueryResult;
import eu.daiad.web.model.scheduling.JobExecutionInfo;
import eu.daiad.web.model.scheduling.JobInfo;

/**
 * Provides methods for querying, scheduling and launching jobs.
 */
public interface ISchedulerService {

	/**
	 * Returns all registered {@link JobInfo}
	 *
	 * @return the registered jobs.
	 */
	List<JobInfo> getJobs();

	/**
	 * Returns a {@link JobInfo} based on its id.
	 *
	 * @param jobId the job id.
	 * @return the job with the given id.
	 */
	JobInfo getJob(long jobId);

	/**
	 * Returns the job next scheduled execution.
	 *
	 * @param jobName the job name.
	 * @return a {@link DateTime} instance or null if the job is not scheduled.
	 */
	DateTime getJobNextExecutionDateTime(String jobName);

	/**
     * Returns a list of {@link JobExecutionInfo}, optionally filtered by a query.
     *
     * @param query the query to filter records.
     * @return the executions
     */
    ExecutionQueryResult getJobExecutions(ExecutionQuery query);

	/**
	 * Returns a list of {@link JobExecutionInfo} for a job based on its id.
	 *
	 * @param jobId the job id.
	 * @param startPosition the start index.
	 * @param maxResult the number of returned objects.
	 * @return the first {@code maxResult} executions starting from {@code startPosition} index.
	 */
	List<JobExecutionInfo> getJobExecutions(long jobId, int startPosition, int maxResult);

	/**
	 * Enables a job based on its id.
	 *
	 * @param jobId the job id.
	 */
	void enable(Long jobId);

	/**
	 * Disables a job based on its id.
	 *
	 * @param jobId the job id.
	 */
	void disable(Long jobId);

    /**
     * Returns the message of an execution by its id.
     *
     * @param executionId the execution id.
     * @return the execution message.
     */
	String getExecutionMessage(long executionId);

	/**
	 * Schedules a job to run periodically.
	 *
	 * @param jobId the job id.
	 * @param period the period length in seconds.
	 */
	void schedulePeriodicJob(long jobId, long period);

	/**
	 * Schedules a job to execute using a CRON expression.
	 *
	 * @param jobId the job id.
	 * @param cronExpression the CRON expression.
	 */
	Date scheduleCronJob(long jobId, String cronExpression);

	/**
	 * Launches a job based on its id
	 *
	 * @param jobId the job id
	 * @param parameters job parameters
	 * @return the job instance id.
	 * @throws ApplicationException no job with this id exists.
	 */
	Long launch(long jobId, Map<String, String> parameters) throws ApplicationException;

	/**
     * Launches a job given an instance of {@link IJobBuilder}.
     *
     * @param jobBuilder the job builder for constructing the job
     * @param jobName the job unique name
     * @param parameters job parameters
     * @return the job instance id.
     * @throws ApplicationException no job with this id exists.
     */
	Long launch(IJobBuilder jobBuilder, String jobName, Map<String, String> parameters) throws ApplicationException;


    /**
     * Launches a job based on its id
     *
     * @param jobId the job id
     * @return the job instance id
     * @throws ApplicationException no job with this id exists.
     */
	Long launch(long jobId) throws ApplicationException;

    /**
     * Launches a job based on its name
     *
     * @param jobName the job name
     * @return the job instance id
     * @throws ApplicationException no job with this name exists.
     */
	Long launch(String jobName) throws ApplicationException;

    /**
     * Launches a job based on its name
     *
     * @param jobName the job name
     * @param parameters job parameters
     * @return the job instance id
     * @throws ApplicationException no job with this name exists.
     */
	Long launch(String jobName, Map<String, String> parameters) throws ApplicationException;

	/**
	 * Sends a message to stop a job execution based on its id. Still no
	 * guarantees are provided that the job will stop. The caller must poll the
	 * running jobs using {@link #getJobExecutions(long, int, int)} in order to
	 * verify that the job has been stopped successfully.
	 *
	 * @param executionId the execution id
	 * @return true if the message was successfully sent (does not guarantee
	 *         that the job has stopped)
	 */
	boolean stop(Long executionId);

}
