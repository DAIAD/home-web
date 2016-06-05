package eu.daiad.web.service.scheduling;

import java.util.Date;
import java.util.List;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.scheduling.ExecutionQuery;
import eu.daiad.web.model.scheduling.ExecutionQueryResult;
import eu.daiad.web.model.scheduling.JobExecutionInfo;
import eu.daiad.web.model.scheduling.JobInfo;

public interface ISchedulerService {

	/**
	 * Returns all registered {@link JobInfo}
	 * 
	 * @return the registered jobs.
	 */
	abstract List<JobInfo> getJobs();

	/**
	 * Returns a {@link JobInfo} based on its id.
	 * 
	 * @param jobId
	 *            the job id.
	 * @return the job with the given id.
	 */
	abstract JobInfo getJob(long jobId);

	/**
     * Returns a list of {@link JobExecutionInfo}, optionally filtered by a query.
     * 
     * @param query the query to filter records.
     * @return the executions
     */
    abstract ExecutionQueryResult getJobExecutions(ExecutionQuery query);
    
	/**
	 * Returns a list of {@link JobExecutionInfo} for a job based on its id.
	 * 
	 * @param jobId the job id.
	 * @param startPosition the start index.
	 * @param maxResult the number of returned objects.
	 * @return the first {@code maxResult} executions starting from {@code startPosition} index.
	 */
	abstract List<JobExecutionInfo> getJobExecutions(long jobId, int startPosition, int maxResult);

	/**
	 * Enables a job based on its id.
	 * 
	 * @param jobId
	 *            the job id.
	 */
	abstract void enable(Long jobId);

	/**
	 * Disables a job based on its id.
	 * 
	 * @param jobId
	 *            the job id.
	 */
	abstract void disable(Long jobId);

    /**
     * Returns the message of an execution by its id.
     * 
     * @param executionId the execution id.
     * @return the execution message.
     */
	abstract String getExecutionMessage(long executionId);
	
	/**
	 * Schedules a job to run periodically.
	 * 
	 * @param jobId
	 *            the job id.
	 * @param period
	 *            the period length in seconds.
	 */
	abstract void schedulePeriodicJob(long jobId, long period);

	/**
	 * Schedules a job to execute using a CRON expression.
	 * 
	 * @param jobId
	 *            the job id.
	 * @param cronExpression
	 *            the CRON expression.
	 */
	abstract Date scheduleCronJob(long jobId, String cronExpression);

	/**
	 * Launches a job based on its id
	 * 
	 * @param jobId
	 *            the job id
	 * @throws ApplicationException
	 *             no job with this id exists.
	 */
	abstract void launch(long jobId) throws ApplicationException;

	/**
	 * Launches a job based on its name
	 * 
	 * @param jobName
	 *            the job name
	 * @throws ApplicationException
	 *             no job with this name exists.
	 */
	abstract void launch(String jobName) throws ApplicationException;

	/**
	 * Sends a message to stop a job execution based on its id. Still no
	 * guarantees are provided that the job will stop. The caller must poll the
	 * running jobs using {@link #getJobExecutions(long, int, int)} in order to
	 * verify that the job has been stopped successfully.
	 * 
	 * @param executionId
	 *            the execution id
	 * @return true if the message was successfully sent (does not guarantee
	 *         that the job has stopped)
	 */
	abstract boolean stop(Long executionId);

}
