package eu.daiad.web.service.scheduling;

import java.util.Date;
import java.util.List;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.scheduling.JobExecutionInfo;
import eu.daiad.web.model.scheduling.JobInfo;

public interface ISchedulerService {

	abstract List<JobInfo> getJobs();

	abstract JobInfo getJob(long jobId);

	abstract List<JobExecutionInfo> getJobExecutions(long jobId, int startPosition, int maxResult);

	abstract void enable(Long jobId);

	abstract void disable(Long jobId);

	abstract void schedulePeriodicJob(long jobId, long period);

	abstract Date scheduleCronJob(long jobId, String cronExpression);

	abstract void launch(long jobId) throws ApplicationException;

	abstract void launch(String jobName) throws ApplicationException;

	abstract boolean stop(Long executionId);

}
