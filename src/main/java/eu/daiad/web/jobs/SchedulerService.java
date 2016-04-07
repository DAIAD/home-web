package eu.daiad.web.jobs;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import eu.daiad.web.domain.admin.ScheduledJob;

@Service
public class SchedulerService {

	private static final Log logger = LogFactory.getLog(SchedulerService.class);

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private ISchedulerRepository schedulerRepository;

	@Autowired
	private JobParametersIncrementer jobParametersIncrementer;

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	private ArrayList<ScheduledFuture<?>> scheduledJobs = new ArrayList<ScheduledFuture<?>>();

	@PostConstruct
	private void initialize() throws Exception {
		try {
			// Fetch all registered jobs
			for (ScheduledJob scheduledJob : this.schedulerRepository.getScheduledJobs()) {
				// Get job builder from the context
				IJobBuilder jobBuilder = (IJobBuilder) ctx.getBean(scheduledJob.getJob().getBeanName());

				Job batchJob = jobBuilder.build(scheduledJob.getJob().getJobName(), jobParametersIncrementer);

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
									scheduledJob.getJob().getJobName(), scheduledJob.getPeriod(),
									scheduledJob.getCronExpression()));
				} else if (scheduledJob.getPeriod() != null) {
					scheduledJobs.add(taskScheduler.schedule(new RunnableJob(jobLauncher, batchJob, jobParameters),
									new PeriodicTrigger(scheduledJob.getPeriod() * 1000)));
				} else if (!StringUtils.isBlank(scheduledJob.getCronExpression())) {
					scheduledJobs.add(taskScheduler.schedule(new RunnableJob(jobLauncher, batchJob, jobParameters),
									new CronTrigger(scheduledJob.getCronExpression())));
				} else {
					logger.error(String.format("Failed to scheduler job [%s]. No trigger options is set.", scheduledJob
									.getJob().getJobName()));
				}
			}

		} catch (Exception ex) {
			logger.error(ex);
		}

	}

	@PreDestroy
	private void destroy() throws Exception {

	}

	public static class RunnableJob implements Runnable {

		private JobLauncher jobLauncher;

		private Job job;

		private JobParameters parameters;

		RunnableJob(JobLauncher jobLauncher, Job job, JobParameters parameters) {
			this.jobLauncher = jobLauncher;
			this.job = job;
			this.parameters = parameters;
		}

		@Override
		public void run() {
			try {
				System.out.println(new DateTime());
				jobLauncher.run(job, job.getJobParametersIncrementer().getNext(parameters));
			} catch (Exception ex) {
				logger.error(String.format("Failed to start job [%s].", job.getName()), ex);
			}
		}

	}
}
