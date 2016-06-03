package eu.daiad.web.service.scheduling;

import java.util.List;

import eu.daiad.web.domain.admin.ScheduledJob;
import eu.daiad.web.domain.admin.ScheduledJobExecution;

/**
 * 
 * Scheduled job repository
 *
 */
public interface ISchedulerRepository {

	/**
	 * Returns a list of all registered {@link ScheduledJob}.
	 * 
	 * @return the scheduled jobs.
	 */
	abstract List<ScheduledJob> getJobs();

	/**
	 * Returns a {@link ScheduledJob} based on its id.
	 * 
	 * @param jobId the job id.
	 * @return the job with the given id.
	 */
	abstract ScheduledJob getJobById(long jobId);

	/**
	 * Returns a {@link ScheduledJob} based on its name.
	 * 
	 * @param jobName the job name.
	 * @return the job with the given name.
	 */
	abstract ScheduledJob getJobByName(String jobName);

	/**
	 * Returns a list of {@link ScheduledJobExecution} based on a job name.
	 * 
	 * @param jobName the job name.
	 * @param startPosition the start index.
	 * @param maxResult the number of returned objects.
	 * @return the first {@code maxResult} jobs starting from {@code startPosition} index.
	 */
	abstract List<ScheduledJobExecution> getExecutions(String jobName, int startPosition, int maxResult);

	/**
	 * Returns a list of {@link ScheduledJobExecution} based on a job id.
	 * 
	 * @param jobId the job id.
	 * @param startPosition the start index.
	 * @param maxResult the number of returned objects.
	 * @return the first {@code maxResult} jobs starting from {@code startPosition} index.
	 */
	abstract List<ScheduledJobExecution> getExecutions(long jobId, int startPosition, int maxResult);

	/**
	 * Returns the last {@link ScheduledJobExecution} based on a job name.
	 * 
	 * @param jobName the job name.
	 * @return the last job execution.
	 */
	abstract ScheduledJobExecution getLastExecution(String jobName);

	/**
	 * Returns the last {@link ScheduledJobExecution} based on a job id.
	 * 
	 * @param jobId the job id.
	 * @return the last job execution.
	 */
	abstract ScheduledJobExecution getLastExecution(long jobId);

	/**
	 * Enables a job execution based on job id.
	 * 
	 * @param jobId the job id.
	 * @return the enabled job.
	 */
	abstract ScheduledJob enable(long jobId);

	/**
	 * Disables a job execution based on job id.
	 * 
	 * @param jobId the job id.
	 */
	abstract void disable(long jobId);

	/**
	 * Schedules a job to run periodically.
	 * 
	 * @param jobId the job id.
	 * @param period the period length in seconds.
	 */
	abstract void schedulePeriodicJob(long jobId, long period);

	/**
	 * Schedules a job to execute using a CRON expression.
	 * 
	 * @param jobId the job id.
	 * @param cronExpression the CRON expression.
	 */
	abstract void scheduleCronJob(long jobId, String cronExpression);

}
