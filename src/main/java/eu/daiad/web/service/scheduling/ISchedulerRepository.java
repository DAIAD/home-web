package eu.daiad.web.service.scheduling;

import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;

import eu.daiad.web.domain.admin.ScheduledJobEntity;
import eu.daiad.web.domain.admin.ScheduledJobExecutionEntity;
import eu.daiad.web.model.scheduling.ExecutionQuery;
import eu.daiad.web.model.scheduling.ExecutionQueryResult;

/**
 * * Scheduled job repository
 */
public interface ISchedulerRepository {

    /**
     * Returns a list of all registered {@link ScheduledJobEntity}.
     *
     * @return the scheduled jobs.
     */
    abstract List<ScheduledJobEntity> getJobs();

    /**
     * Returns a {@link ScheduledJobEntity} based on its id.
     *
     * @param jobId the job id.
     * @return the job with the given id.
     */
    abstract ScheduledJobEntity getJobById(long jobId);

    /**
     * Returns a {@link ScheduledJobEntity} based on its name.
     *
     * @param jobName the job name.
     * @return the job with the given name.
     */
    abstract ScheduledJobEntity getJobByName(String jobName);

    /**
     * Returns a list of {@link ScheduledJobExecutionEntity} based on a job id.
     *
     * @param jobId the job id.
     * @param startPosition the start index.
     * @param maxResult the number of returned objects.
     * @return the first {@code maxResult} jobs starting from {@code startPosition} index.
     */
    abstract List<ScheduledJobExecutionEntity> getExecutions(long jobId, int startPosition, int maxResult);

    /**
     * Returns a list of {@link ScheduledJobExecutionEntity} based on a job name.
     *
     * @param jobName
     *            the job name.
     * @param startPosition
     *            the start index.
     * @param maxResult
     *            the number of returned objects.
     * @return the first {@code maxResult} jobs starting from
     *         {@code startPosition} index.
     */
    abstract List<ScheduledJobExecutionEntity> getExecutions(String jobName, int startPosition, int maxResult);

    /**
     * Returns the message of an execution by its id.
     *
     * @param executionId the execution id.
     * @return the execution message.
     */
    abstract String getExecutionMessage(long executionId);

    /**
     * Returns a list of {@link ScheduledJobExecutionEntity} filtered by their exit code.
     *
     * @param exitStatus exit code for filtering the job executions.
     * @return the job executions.
     */
    abstract List<ScheduledJobExecutionEntity> getExecutionByExitStatus(ExitStatus exitStatus);

    /**
     * Returns a list of {@link ScheduledJobExecutionEntity}, optionally filtered by a
     * query.
     *
     * @param query
     *            the query to filter results
     * @return the job executions
     */
    abstract ExecutionQueryResult getExecutions(ExecutionQuery query);

    /**
     * Returns the last {@link ScheduledJobExecutionEntity} based on a job name.
     *
     * @param jobName
     *            the job name.
     * @return the last job execution.
     */
    abstract ScheduledJobExecutionEntity getLastExecution(String jobName);

    /**
     * Returns the last {@link ScheduledJobExecutionEntity} based on a job id.
     *
     * @param jobId
     *            the job id.
     * @return the last job execution.
     */
    abstract ScheduledJobExecutionEntity getLastExecution(long jobId);

    /**
     * Enables a job execution based on job id.
     *
     * @param jobId the job id.
     * @return the enabled job.
     */
    abstract ScheduledJobEntity enable(long jobId);

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
     * @param cronExpression
     *            the CRON expression.
     */
    abstract void scheduleCronJob(long jobId, String cronExpression);

    /**
     * Updates job execution status by its id.
     * @param jobExecutionId the job execution status.
     * @param status the status.
     * @return the number of rows affected.
     */
    abstract int updateJobExecutionStatus(long jobExecutionId, BatchStatus status);

}
