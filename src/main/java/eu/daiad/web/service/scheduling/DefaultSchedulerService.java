package eu.daiad.web.service.scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import eu.daiad.web.domain.admin.ScheduledJob;
import eu.daiad.web.domain.admin.ScheduledJobExecution;
import eu.daiad.web.jobs.IJobBuilder;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.scheduling.JobExecutionInfo;
import eu.daiad.web.model.scheduling.JobInfo;
import eu.daiad.web.service.BaseService;

// https://github.com/spring-projects/spring-batch-admin/blob/master/spring-batch-admin-manager/src/main/java/org/springframework/batch/admin/service/SimpleJobService.java

@Service
public class DefaultSchedulerService extends BaseService implements ISchedulerService, InitializingBean {

	private static final Log logger = LogFactory.getLog(DefaultSchedulerService.class);

	private static final int DEFAULT_SHUTDOWN_TIMEOUT = 60 * 1000;

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private JobParametersIncrementer jobParametersIncrementer;

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobOperator jobOperator;

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	@Autowired
	private ISchedulerRepository schedulerRepository;

	private Map<Long, JobSchedulingProperties> scheduledJobs = new HashMap<Long, JobSchedulingProperties>();

	private Collection<JobExecution> activeExecutions = Collections.synchronizedList(new ArrayList<JobExecution>());

	private int shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			this.updateScheduler();
		} catch (Exception ex) {
			logger.error("Failed to reload job metadata.", ex);
		}
	}

	@PreDestroy
	private void destroy() throws Exception {
		// Cancel scheduling
		for (Entry<Long, JobSchedulingProperties> entry : this.scheduledJobs.entrySet()) {
			ScheduledFuture<?> future = entry.getValue().getFuture();
			if ((!future.isCancelled()) && (!future.isDone())) {
				entry.getValue().getFuture().cancel(false);
			}
		}

		// Stop jobs
		Exception firstException = null;

		for (JobExecution jobExecution : activeExecutions) {
			try {
				if (jobExecution.isRunning()) {
					this.stop(jobExecution.getId());
				}
			} catch (Exception ex) {
				if (firstException == null) {
					firstException = wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
				}
			}
		}

		int count = 0;
		int maxCount = (shutdownTimeout + 1000) / 1000;
		while (!activeExecutions.isEmpty() && ++count < maxCount) {
			logger.error("Waiting for " + activeExecutions.size() + " active executions to complete");
			removeInactiveExecutions();
			Thread.sleep(1000L);
		}

		if (firstException != null) {
			throw firstException;
		}
	}

	private JobInfo scheduledJobToJobInfo(ScheduledJob scheduledJob) {
		JobInfo info = new JobInfo();

		info.setId(scheduledJob.getId());

		info.setCategory(scheduledJob.getCategory());
		info.setContainer(scheduledJob.getContainer());

		info.setName(scheduledJob.getName());
		info.setDescription(scheduledJob.getDescription());

		info.setEnabled(scheduledJob.isEnabled());

		ScheduledJobExecution lastExecution = this.schedulerRepository.getLastExecution(scheduledJob.getName());
		if (lastExecution != null) {
			info.setLastExecution(lastExecution.getStartedOn().getMillis());
			info.setLastExecutionDuration((lastExecution.getCompletedOn().getMillis() - lastExecution.getStartedOn()
							.getMillis()) / 1000);
			info.setLastExecutionExitCode(lastExecution.getExitCode());
			info.setLastExecutionExitMessage(lastExecution.getExitMessage());
		}

		JobSchedulingProperties entry = this.scheduledJobs.get(scheduledJob.getId());
		if (entry != null) {
			long delay = entry.getFuture().getDelay(TimeUnit.MILLISECONDS);
			DateTime nextExecution = DateTime.now();
			if (delay > 0) {
				nextExecution = nextExecution.plus(delay);
			}
			info.setNextExecution(nextExecution.getMillis());
		}

		// TODO : Set progress if available

		return info;
	}

	@Override
	public List<JobInfo> getJobs() {
		List<JobInfo> jobs = new ArrayList<JobInfo>();

		for (ScheduledJob scheduledJob : this.schedulerRepository.getJobs()) {
			jobs.add(this.scheduledJobToJobInfo(scheduledJob));
		}

		return jobs;
	}

	@Override
	public JobInfo getJob(long jobId) {
		JobInfo info = null;

		ScheduledJob scheduledJob = this.schedulerRepository.getJobById(jobId);

		if (scheduledJob != null) {
			info = this.scheduledJobToJobInfo(scheduledJob);
		}

		return info;
	}

	@Override
	public List<JobExecutionInfo> getJobExecutions(long jobId, int startPosition, int maxResult) {
		List<JobExecutionInfo> result = new ArrayList<JobExecutionInfo>();

		List<ScheduledJobExecution> executions = this.schedulerRepository
						.getExecutions(jobId, startPosition, maxResult);

		for (ScheduledJobExecution execution : executions) {
			JobExecutionInfo info = new JobExecutionInfo();

			if (execution.getCompletedOn() != null) {
				info.setCompletedOn(execution.getCompletedOn().getMillis());
			}
			info.setExecutionId(execution.getJobExecutionId());
			info.setExitCode(execution.getExitCode());
			info.setInstanceId(execution.getJobInstanceId());
			info.setJobId(jobId);
			info.setStartedOn(execution.getStartedOn().getMillis());
			info.setStatusCode(execution.getStatusCode());

			// TODO: Add parameters

			result.add(info);
		}

		return result;
	}

	@Override
	public void enable(Long jobId) {
		try {
			ScheduledJob scheduledJob = this.schedulerRepository.enable(jobId);

			if (scheduledJobs.containsKey(jobId)) {
				this.scheduledJobs.get(jobId).getFuture().cancel(false);
				this.scheduledJobs.remove(jobId);
			}

			this.schedule(scheduledJob);
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public void disable(Long jobId) {
		try {
			this.schedulerRepository.disable(jobId);

			if (scheduledJobs.containsKey(jobId)) {
				this.scheduledJobs.get(jobId).getFuture().cancel(false);
				this.scheduledJobs.remove(jobId);
			}
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public void schedulePeriodicJob(long scheduledJobId, long period) {
		this.schedulerRepository.schedulePeriodicJob(scheduledJobId, period);
	}

	@Override
	public Date scheduleCronJob(long scheduledJobId, String cronExpression) {
		try {
			CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(cronExpression);

			this.schedulerRepository.scheduleCronJob(scheduledJobId, cronExpression);

			return cronSequenceGenerator.next(new Date());
		} catch (IllegalArgumentException ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	private void updateScheduler() throws Exception {
		for (ScheduledJob scheduledJob : this.schedulerRepository.getJobs()) {
			this.schedule(scheduledJob);
		}
	}

	private void schedule(ScheduledJob scheduledJob) throws Exception {
		// Get job builder from the context
		IJobBuilder jobBuilder = (IJobBuilder) ctx.getBean(scheduledJob.getBean());

		Job batchJob = jobBuilder.build(scheduledJob.getName(), jobParametersIncrementer);

		// Register job thus making it accessible to JobOperator
		jobRegistry.register(new ReferenceJobFactory(batchJob));

		if (scheduledJob.isEnabled()) {
			// Initialize job parameters
			JobParametersBuilder parameterBuilder = new JobParametersBuilder();
			for (eu.daiad.web.domain.admin.ScheduledJobParameter parameter : scheduledJob.getParameters()) {
				parameterBuilder.addString(parameter.getName(), parameter.getValue());
			}
			JobParameters jobParameters = parameterBuilder.toJobParameters();

			// Start job execution
			if ((scheduledJob.getPeriod() != null) && (!StringUtils.isBlank(scheduledJob.getCronExpression()))) {
				logger.error(String.format(
								"Failed to scheduler job [%s]. Both trigger options are set to [%d] and [%s].",
								scheduledJob.getName(), scheduledJob.getPeriod(), scheduledJob.getCronExpression()));
			} else if (scheduledJob.getPeriod() != null) {
				logger.warn(String.format("Initializing job [%s] with periodic trigger [%d].", scheduledJob.getName(),
								scheduledJob.getPeriod()));

				ScheduledFuture<?> future = taskScheduler.schedule(new RunnableJob(this.activeExecutions, jobLauncher,
								batchJob, jobParameters), new PeriodicTrigger(scheduledJob.getPeriod() * 1000));

				this.scheduledJobs.put(scheduledJob.getId(), new JobSchedulingProperties(scheduledJob.getId(), future));
			} else if (!StringUtils.isBlank(scheduledJob.getCronExpression())) {
				logger.warn(String.format("Initializing job [%s] with CRON trigger expression [%s].",
								scheduledJob.getName(), scheduledJob.getCronExpression()));

				ScheduledFuture<?> future = taskScheduler.schedule(new RunnableJob(this.activeExecutions, jobLauncher,
								batchJob, jobParameters), new CronTrigger(scheduledJob.getCronExpression()));

				this.scheduledJobs.put(scheduledJob.getId(), new JobSchedulingProperties(scheduledJob.getId(), future));
			} else {
				logger.error(String.format("Failed to scheduler job [%s]. No trigger options is set.",
								scheduledJob.getName()));
			}
		}
	}

	@Override
	public void launch(String jobName) throws ApplicationException {
		ScheduledJob job = this.schedulerRepository.getJobByName(jobName);

		this.launch(job.getId());
	}

	@Override
	public void launch(long jobId) throws ApplicationException {
		try {
			ScheduledJob scheduledJob = this.schedulerRepository.getJobById(jobId);

			// Get job builder from the context
			IJobBuilder jobBuilder = (IJobBuilder) ctx.getBean(scheduledJob.getBean());

			Job job = jobBuilder.build(scheduledJob.getName(), jobParametersIncrementer);

			// Register job thus making it accessible to JobOperator
			if (!jobRegistry.getJobNames().contains(scheduledJob.getName())) {
				jobRegistry.register(new ReferenceJobFactory(job));
			}

			// Initialize job parameters
			JobParametersBuilder parameterBuilder = new JobParametersBuilder();
			for (eu.daiad.web.domain.admin.ScheduledJobParameter parameter : scheduledJob.getParameters()) {
				parameterBuilder.addString(parameter.getName(), parameter.getValue());
			}
			JobParameters parameters = parameterBuilder.toJobParameters();

			jobLauncher.run(job, job.getJobParametersIncrementer().getNext(parameters));
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SchedulerErrorCode.SCHEDULER_JOB_LAUNCH_FAIL).set("job", jobId);
		}
	}

	@Override
	public boolean stop(Long executionId) {
		try {
			if (this.activeExecutions.contains(executionId)) {
				this.activeExecutions.remove(executionId);
			}
			return this.jobOperator.stop(executionId);
		} catch (Exception ex) {
			throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
		}
	}

	private static class RunnableJob implements Runnable {

		private final JobLauncher jobLauncher;

		private final Job job;

		private final JobParameters parameters;

		private final Collection<JobExecution> activeExecutions;

		RunnableJob(Collection<JobExecution> activeExecutions, JobLauncher jobLauncher, Job job,
						JobParameters parameters) {
			this.activeExecutions = activeExecutions;
			this.jobLauncher = jobLauncher;
			this.job = job;
			this.parameters = parameters;
		}

		@Override
		public void run() {
			try {
				logger.warn(String.format("Launching job [%s].", job.getName()));

				JobExecution jobExecution = jobLauncher.run(job, job.getJobParametersIncrementer().getNext(parameters));

				if (jobExecution.isRunning()) {
					activeExecutions.add(jobExecution);
				}

			} catch (Exception ex) {
				logger.error(String.format("Failed to start job [%s].", job.getName()), ex);
			}
		}

	}

	@Scheduled(fixedDelay = 60000)
	public void removeInactiveExecutions() {
		for (Iterator<JobExecution> iterator = activeExecutions.iterator(); iterator.hasNext();) {
			JobExecution jobExecution = iterator.next();
			try {
				Set<Long> runningExecutions = this.jobOperator.getRunningExecutions(jobExecution.getJobInstance()
								.getJobName());
				if (!runningExecutions.contains(jobExecution.getId())) {
					iterator.remove();
				}
			} catch (NoSuchJobException e) {
				logger.error("Unexpected exception loading running executions", e);
			}
		}

	}
}
