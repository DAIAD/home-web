package eu.daiad.web.scheduling;

import java.util.List;

import eu.daiad.web.domain.admin.ScheduledJob;
import eu.daiad.web.domain.admin.ScheduledJobExecution;

public interface ISchedulerRepository {

	abstract List<ScheduledJob> getJobs();

	abstract ScheduledJob getJobById(long jobId);

	abstract List<ScheduledJobExecution> getExecutions(String jobName, int startPosition, int maxResult);

	abstract List<ScheduledJobExecution> getExecutions(long jobId, int startPosition, int maxResult);

	abstract ScheduledJobExecution getLastExecution(String jobName);

	abstract ScheduledJobExecution getLastExecution(long jobId);

	abstract ScheduledJob enable(long jobId);

	abstract void disable(long jobId);

	abstract void schedulePeriodicJob(long jobId, long period);

	abstract void scheduleCronJob(long jobId, String cronExpression);

}
