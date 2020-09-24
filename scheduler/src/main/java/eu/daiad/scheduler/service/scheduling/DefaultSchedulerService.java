package eu.daiad.scheduler.service.scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import eu.daiad.common.domain.admin.ScheduledJobEntity;
import eu.daiad.common.domain.admin.ScheduledJobExecutionEntity;
import eu.daiad.common.domain.admin.ScheduledJobParameterEntity;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.SchedulerErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.scheduling.ExecutionQuery;
import eu.daiad.common.model.scheduling.ExecutionQueryResult;
import eu.daiad.common.model.scheduling.JobExecutionInfo;
import eu.daiad.common.model.scheduling.JobInfo;
import eu.daiad.common.model.scheduling.JobInfoScheduleCron;
import eu.daiad.common.model.scheduling.JobInfoSchedulePeriodic;
import eu.daiad.common.service.BaseService;
import eu.daiad.scheduler.job.builder.IJobBuilder;

// https://github.com/spring-projects/spring-batch-admin/blob/master/spring-batch-admin-manager/src/main/java/org/springframework/batch/admin/service/SimpleJobService.java

/**
 * Default implementation of {@link ISchedulerService} that provides methods for querying, scheduling and launching jobs.
 */
@Service
public class DefaultSchedulerService extends BaseService implements ISchedulerService {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DefaultSchedulerService.class);

    /**
     * Default shutdown timeout to wait for jobs to stop before stopping the
     * scheduler.
     */
    private static final int DEFAULT_SHUTDOWN_TIMEOUT = 60 * 1000;

    /**
     * Application server time zone.
     */
    @Value("${daiad.batch.server-time-zone:Europe/Athens}")
    private String serverTimeZone;

    /**
     * Spring application context.
     */
    @Autowired
    private ApplicationContext ctx;

    /**
     * Provides a unique set of job parameters for starting a job.
     */
    @Autowired
    private JobParametersIncrementer jobParametersIncrementer;

    /**
     * Job registry.
     */
    @Autowired
    private JobRegistry jobRegistry;

    /**
     * Interface for launching jobs.
     */
    @Autowired
    private JobLauncher jobLauncher;

    /**
     * Interface for managing jobs.
     */
    @Autowired
    private JobOperator jobOperator;

    /**
     * Interface for scheduling job execution.
     */
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    /**
     * Repository for accessing job metadata and parameters.
     */
    @Autowired
    private ISchedulerRepository schedulerRepository;

    /**
     * A map for storing information for all scheduled jobs.
     */
    private Map<Long, JobSchedulingProperties> scheduledJobs = new HashMap<Long, JobSchedulingProperties>();

    /**
     * A collection of all running jobs. The service periodically checks all
     * active executions and removes completed jobs from this collection.
     */
    private List<JobExecution> activeExecutions = Collections.synchronizedList(new ArrayList<JobExecution>());

    /**
     * Updates job status for after a system restart
     * @throws Exception
     */
    @PostConstruct
    public void init() throws Exception {
        try {
            updateScheduler();
        } catch (Exception ex) {
            logger.error("Failed to reload job metadata.", ex);
        }
    }

    /**
     * Cancels all scheduled jobs and waits for {@code DEFAULT_SHUTDOWN_TIMEOUT}
     * milliseconds for any running jobs to stop.
     *
     * @throws Exception if job stop operation has failed.
     */
    @PreDestroy
    private void destroy() throws Exception {
        // Cancel scheduling
        for (Entry<Long, JobSchedulingProperties> entry : scheduledJobs.entrySet()) {
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
                    stop(jobExecution.getId());
                }
            } catch (Exception ex) {
                if (firstException == null) {
                    firstException = wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
                }
            }
        }

        int count = 0;
        int maxCount = (DEFAULT_SHUTDOWN_TIMEOUT + 1000) / 1000;
        while (!activeExecutions.isEmpty() && ++count < maxCount) {
            logger.error("Waiting for " + activeExecutions.size() + " active executions to complete");

            removeInactiveExecutions();
            Thread.sleep(1000L);
        }

        if (firstException != null) {
            throw firstException;
        }
    }

    /**
     * Converts an instance of {@link ScheduledJobEntity} to a new instance of
     * {@link JobInfo}.
     *
     * @param scheduledJob the entity to convert.
     * @return the new {@link JobInfo} object.
     */
    private JobInfo scheduledJobToJobInfo(ScheduledJobEntity scheduledJob) {
        JobInfo info = new JobInfo();

        DateTime startLocalDateTime;
        DateTime endLocalDateTime;

        info.setId(scheduledJob.getId());

        info.setCategory(scheduledJob.getCategory());
        info.setContainer(scheduledJob.getContainer());

        info.setName(scheduledJob.getName());
        info.setDescription(scheduledJob.getDescription());

        info.setEnabled(scheduledJob.isEnabled());
        info.setVisible(scheduledJob.isVisible());

        ScheduledJobExecutionEntity lastExecution = schedulerRepository.getLastExecution(scheduledJob.getName());
        if (lastExecution != null) {
            startLocalDateTime = lastExecution.getStartedOn().toDateTime(DateTimeZone.forID(serverTimeZone));

            info.setLastExecution(startLocalDateTime.getMillis());

            if (lastExecution.getCompletedOn() != null) {
                endLocalDateTime = lastExecution.getCompletedOn().toDateTime(DateTimeZone.forID(serverTimeZone));

                info.setLastExecutionDuration((endLocalDateTime.getMillis() - startLocalDateTime.getMillis()) / 1000);
            }
            info.setLastExecutionExitCode(lastExecution.getExitCode());
            info.setLastExecutionExitMessage(lastExecution.getExitMessage());

            for (JobExecution activeExecution : activeExecutions) {
                if ((activeExecution.getId() != null) && (lastExecution.getJobInstanceId() == activeExecution.getId())) {
                    info.setRunning(true);
                }
            }
        }

        JobSchedulingProperties entry = scheduledJobs.get(scheduledJob.getId());
        if (entry != null) {
            long delay = entry.getFuture().getDelay(TimeUnit.MILLISECONDS);
            DateTime nextExecution = DateTime.now();
            if (delay > 0) {
                nextExecution = nextExecution.plus(delay);
            }
            info.setNextExecution(nextExecution.getMillis());
        }

        if (scheduledJob.getPeriod() != null) {
            JobInfoSchedulePeriodic schedule = new JobInfoSchedulePeriodic();
            schedule.setPeriod(scheduledJob.getPeriod());

            info.setSchedule(schedule);
        } else if (!StringUtils.isBlank(scheduledJob.getCronExpression())) {
            JobInfoScheduleCron schedule = new JobInfoScheduleCron();
            schedule.setCronExpression(scheduledJob.getCronExpression());

            info.setSchedule(schedule);
        }

        // TODO : Set progress if available

        return info;
    }

    @Override
    public List<JobInfo> getJobs() {
        removeInactiveExecutions();

        List<JobInfo> jobs = new ArrayList<JobInfo>();

        for (ScheduledJobEntity scheduledJob : schedulerRepository.getJobs()) {
            jobs.add(scheduledJobToJobInfo(scheduledJob));
        }

        return jobs;
    }

    @Override
    public JobInfo getJob(long jobId) {
        JobInfo info = null;

        ScheduledJobEntity scheduledJob = schedulerRepository.getJobById(jobId);

        if (scheduledJob != null) {
            info = scheduledJobToJobInfo(scheduledJob);
        }

        return info;
    }
	/**
	 * Returns a {@link JobInfo} based on its name.
	 *
	 * @param jobName the job name.
	 * @return the job with the given name.
	 */
	public JobInfo getJob(String jobName) {
		ScheduledJobEntity job = schedulerRepository.getJobByName(jobName);

		if (job != null) {
			return scheduledJobToJobInfo(job);
		}

		return null;
	}
	
    /**
     * Returns the job next scheduled execution.
     *
     * @param jobName the job name.
     * @return a {@link DateTime} instance or null if the job is not scheduled.
     */
    @Override
    public DateTime getJobNextExecutionDateTime(String jobName) {
        ScheduledJobEntity job = schedulerRepository.getJobByName(jobName);
        if (job == null) {
            return null;
        }

        JobSchedulingProperties entry = scheduledJobs.get(job.getId());
        if (entry != null) {
            long delay = entry.getFuture().getDelay(TimeUnit.MILLISECONDS);
            DateTime nextExecution = DateTime.now();
            if (delay > 0) {
                nextExecution = nextExecution.plus(delay);
            }
            return nextExecution.toDateTime(DateTimeZone.forID(serverTimeZone));
        }

        return null;
    }

    @Override
    public ExecutionQueryResult getJobExecutions(ExecutionQuery query) {
        return schedulerRepository.getExecutions(query);
    }

    @Override
    public List<JobExecutionInfo> getJobExecutions(long jobId, int startPosition, int maxResult) {
        List<JobExecutionInfo> result = new ArrayList<JobExecutionInfo>();

        List<ScheduledJobExecutionEntity> executions = schedulerRepository
                        .getExecutions(jobId, startPosition, maxResult);

        DateTime utcDateTime;

        for (ScheduledJobExecutionEntity execution : executions) {
            JobExecutionInfo info = new JobExecutionInfo();

            if (execution.getCompletedOn() != null) {
                utcDateTime = execution.getCompletedOn().toDateTime(DateTimeZone.forID(serverTimeZone));

                info.setCompletedOn(utcDateTime.getMillis());
            }
            info.setExecutionId(execution.getJobExecutionId());
            info.setExitCode(execution.getExitCode());
            info.setInstanceId(execution.getJobInstanceId());
            info.setJobId(jobId);

            utcDateTime = execution.getStartedOn().toDateTime(DateTimeZone.forID(serverTimeZone));
            info.setStartedOn(utcDateTime.getMillis());

            info.setStatusCode(execution.getStatusCode());
            info.setJobName(execution.getJobName());

            // TODO: Add parameters

            result.add(info);
        }

        return result;
    }

    @Override
    public String getExecutionMessage(long executionId) {
        return schedulerRepository.getExecutionMessage(executionId);
    }

    @Override
    public void enable(Long jobId) {
        try {
            ScheduledJobEntity scheduledJob = schedulerRepository.enable(jobId);

            schedule(scheduledJob);
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void disable(Long jobId) {
        try {
            schedulerRepository.disable(jobId);

            if (scheduledJobs.containsKey(jobId)) {
                scheduledJobs.get(jobId).getFuture().cancel(false);
                scheduledJobs.remove(jobId);
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void schedulePeriodicJob(long scheduledJobId, long period) {
        schedulerRepository.schedulePeriodicJob(scheduledJobId, period);
    }

    @Override
    public Date scheduleCronJob(long scheduledJobId, String cronExpression) {
        try {
            CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(cronExpression);

            schedulerRepository.scheduleCronJob(scheduledJobId, cronExpression);

            return cronSequenceGenerator.next(new Date());
        } catch (IllegalArgumentException ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    /**
     * Sets the status to {@link BatchStatus#ABANDONED} for all pending jobs
     * with exist status {@link ExitStatus#UNKNOWN} and schedules all registered
     * jobs.
     *
     * @throws Exception if an exception occurs when updating job status.
     */
    private void updateScheduler() throws Exception {
        cleanupInterruptedJobs();

        for (ScheduledJobEntity scheduledJob : schedulerRepository.getJobs()) {
            schedule(scheduledJob);
        }
    }

    /**
     * Schedules a job.
     *
     * @param scheduledJob the job to schedule.
     * @throws Exception if job scheduling fails.
     */
    private void schedule(ScheduledJobEntity scheduledJob) throws Exception {
        // Remove existing job
        long jobId = scheduledJob.getId();

        if (scheduledJobs.containsKey(jobId)) {
            scheduledJobs.get(jobId).getFuture().cancel(false);
            scheduledJobs.remove(jobId);
        }

        // Get job builder from the context
        IJobBuilder jobBuilder = (IJobBuilder) ctx.getBean(scheduledJob.getBean());

        Job batchJob = jobBuilder.build(scheduledJob.getName(), jobParametersIncrementer);

        // Register job thus making it accessible to JobOperator
        if (jobRegistry.getJobNames().contains(scheduledJob.getName())) {
            jobRegistry.unregister(scheduledJob.getName());
        }
        jobRegistry.register(new ReferenceJobFactory(batchJob));

        if (scheduledJob.isEnabled()) {
            // Initialize job parameters
            JobParameters jobParameters = initializeJobParameters(scheduledJob, null);

            // Start job execution
            if ((scheduledJob.getPeriod() != null) && (!StringUtils.isBlank(scheduledJob.getCronExpression()))) {
                logger.error(String.format("Failed to scheduler job [%s]. Both trigger options are set to [%d] and [%s].",
                                           scheduledJob.getName(),
                                           scheduledJob.getPeriod(),
                                           scheduledJob.getCronExpression()));
            } else if (scheduledJob.getPeriod() != null) {
                logger.info(String.format("Initializing job [%s] with periodic trigger [%d].",
                                          scheduledJob.getName(),
                                          scheduledJob.getPeriod()));

                ScheduledFuture<?> future = taskScheduler.schedule(
                    new RunnableJob(activeExecutions, jobLauncher, batchJob, jobParameters),
                    new PeriodicTrigger(scheduledJob.getPeriod() * 1000)
                );

                scheduledJobs.put(scheduledJob.getId(), new JobSchedulingProperties(scheduledJob.getId(), future));
            } else if (!StringUtils.isBlank(scheduledJob.getCronExpression())) {
                logger.info(String.format("Initializing job [%s] with CRON trigger expression [%s].",
                                          scheduledJob.getName(),
                                          scheduledJob.getCronExpression()));

                ScheduledFuture<?> future = taskScheduler.schedule(
                    new RunnableJob(activeExecutions, jobLauncher, batchJob, jobParameters),
                    new CronTrigger(scheduledJob.getCronExpression())
                );

                scheduledJobs.put(scheduledJob.getId(), new JobSchedulingProperties(scheduledJob.getId(), future));
            } else {
                logger.error(String.format("Failed to scheduler job [%s]. No trigger options is set.", scheduledJob.getName()));
            }
        }
    }

    @Override
    public Long launch(String jobName) throws ApplicationException {
        ScheduledJobEntity job = schedulerRepository.getJobByName(jobName);

        return this.launch(job.getId(), null);
    }

    @Override
    public Long launch(String jobName, Map<String, String> parameters) throws ApplicationException {
        ScheduledJobEntity job = schedulerRepository.getJobByName(jobName);

        return this.launch(job.getId(), parameters);
    }

    @Override
    public Long launch(long jobId) throws ApplicationException {
        return this.launch(jobId, null);
    }

    @Override
    public Long launch(long jobId, Map<String, String> parameters) throws ApplicationException {
        try {
            refreshActiveExecutions();

            ScheduledJobEntity scheduledJob = schedulerRepository.getJobById(jobId);

            for (JobExecution activeExecution : activeExecutions) {
                if (activeExecution.getJobInstance().getJobName().equals(scheduledJob.getName())) {
                    logger.info(String.format("Launching job [%s] failed. Job is already running.", scheduledJob.getName()));
                    return null;
                }
            }

            // Get job builder from the context
            IJobBuilder jobBuilder = (IJobBuilder) ctx.getBean(scheduledJob.getBean());

            Job job = jobBuilder.build(scheduledJob.getName(), jobParametersIncrementer);

            // Register job thus making it accessible to JobOperator
            if (!jobRegistry.getJobNames().contains(scheduledJob.getName())) {
                jobRegistry.register(new ReferenceJobFactory(job));
            }

            // Initialize job parameters
            JobParameters jobParameters = initializeJobParameters(scheduledJob, parameters);

            JobExecution jobExecution = jobLauncher.run(job, job.getJobParametersIncrementer().getNext(jobParameters));

            if (jobExecution.isRunning()) {
                activeExecutions.add(jobExecution);
            }

            return jobExecution.getJobInstance().getInstanceId();
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SchedulerErrorCode.SCHEDULER_JOB_LAUNCH_FAILED).set("job", jobId);
        }
    }

    @Override
    public Long launch(IJobBuilder jobBuilder, String jobName, Map<String, String> parameters) throws ApplicationException {
        try {
            refreshActiveExecutions();

            for (JobExecution activeExecution : activeExecutions) {
                if (activeExecution.getJobInstance().getJobName().equals(jobName)) {
                    logger.info(String.format("Launching job [%s] failed. Job is already running.", jobName));
                    return null;
                }
            }

            Job job = jobBuilder.build(jobName, jobParametersIncrementer);

            // Register job thus making it accessible to JobOperator
            if (!jobRegistry.getJobNames().contains(jobName)) {
                jobRegistry.register(new ReferenceJobFactory(job));
            }

            // Initialize job parameters
            JobParameters jobParameters = initializeJobParameters(parameters);

            JobExecution jobExecution = jobLauncher.run(job, job.getJobParametersIncrementer().getNext(jobParameters));

            if (jobExecution.isRunning()) {
                activeExecutions.add(jobExecution);
            }

            return jobExecution.getJobInstance().getInstanceId();
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SchedulerErrorCode.SCHEDULER_JOB_LAUNCH_FAILED).set("job", jobName);
        }
    }


    @Override
    public boolean stop(Long executionId) {
        try {
        	// TODO: Check job execution id value
			JobExecution jobExecution = activeExecutions.stream()
				.filter(e -> Objects.equals(e.getId(), executionId))
				.findFirst()
				.orElse(null);
			
			if (jobExecution != null) {
				activeExecutions.remove(jobExecution);
				
	            return jobOperator.stop(executionId);
			}

			return false;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    /**
     * Implements logic for initializing the execution of a scheduled job.
     */
    private static class RunnableJob implements Runnable {

        /**
         * Interface for launching a job.
         */
        private final JobLauncher jobLauncher;

        /**
         * The job to launch.
         */
        private final Job job;

        /**
         * The job parameters.
         */
        private final JobParameters parameters;

        /**
         * A collection of all running jobs.
         */
        private final Collection<JobExecution> activeExecutions;

        /**
         * Creates a new instance of {@link RunnableJob}.
         *
         * @param activeExecutions a collection of all running jobs.
         * @param jobLauncher a simple interface for managing jobs.
         * @param job the job to launch.
         * @param parameters the job parameters.
         */
        public RunnableJob(Collection<JobExecution> activeExecutions, JobLauncher jobLauncher, Job job, JobParameters parameters) {
            this.activeExecutions = activeExecutions;
            this.jobLauncher = jobLauncher;
            this.job = job;
            this.parameters = parameters;
        }

        @Override
        public void run() {
            try {
                for (JobExecution activeExecution : activeExecutions) {
                    if (activeExecution.getJobInstance().getJobName().equals(job.getName())) {
                        logger.info(String.format("Launching job [%s] failed. Job is already running.", job.getName()));
                        return;
                    }
                }

                logger.info(String.format("Launching job [%s].", job.getName()));

                JobExecution jobExecution = jobLauncher.run(job, job.getJobParametersIncrementer().getNext(parameters));

                if (jobExecution.isRunning()) {
                    activeExecutions.add(jobExecution);
                }

            } catch (Exception ex) {
                logger.error(String.format("Failed to start job [%s].", job.getName()), ex);
            }
        }

    }

    /**
     * Removes completed job from {@link DefaultSchedulerService#activeExecutions}.
     */
    @Scheduled(fixedDelay = 60000)
    public void removeInactiveExecutions() {
        refreshActiveExecutions();
    }

    private void refreshActiveExecutions() {
        for (Iterator<JobExecution> iterator = activeExecutions.iterator(); iterator.hasNext();) {
            JobExecution jobExecution = iterator.next();
            try {
                Set<Long> runningExecutions = jobOperator.getRunningExecutions(jobExecution.getJobInstance().getJobName());
                if (!runningExecutions.contains(jobExecution.getId())) {
                    iterator.remove();
                }
            } catch (NoSuchJobException e) {
                logger.error("Unexpected exception loading running executions", e);
            }
        }
    }

    /**
     * Sets job status to {@link BatchStatus#ABANDONED} for all jobs with exit status equal to {@link ExitStatus#UNKNOWN}.
     */
    private void cleanupInterruptedJobs() {
        try {
            List<ScheduledJobExecutionEntity> executions = schedulerRepository.getExecutionByExitStatus(ExitStatus.UNKNOWN);

            for (ScheduledJobExecutionEntity execution : executions) {
                schedulerRepository.updateJobExecutionStatus(execution.getJobExecutionId(), BatchStatus.ABANDONED);
            }
        } catch (Exception e) {
            logger.error("Failed to update abandoned job executions.", e);
        }
    }

    /**
     * Initializes job parameters.
     *
     * @param job the job entity.
     * @param parameters any external parameters for overriding the values in the
     *                   database.
     * @return a valid {@link JobParameters} object.
     */
    private JobParameters initializeJobParameters(ScheduledJobEntity job, Map<String, String> parameters) {
        JobParametersBuilder parameterBuilder = new JobParametersBuilder();

        for (ScheduledJobParameterEntity parameter : job.getParameters()) {
            parameterBuilder.addString(parameter.getQualifiedName(), parameter.getValue());
        }

        // Override parameters
        if (parameters != null) {
            for (Entry<String, String> entry : parameters.entrySet()) {
                parameterBuilder.addString(entry.getKey(), entry.getValue());
            }
        }

        return parameterBuilder.toJobParameters();
    }

    /**
     * Initializes job parameters.
     *
     * @param parameters any external parameters for overriding the values in the
     *                   database.
     * @return a valid {@link JobParameters} object.
     */
    private JobParameters initializeJobParameters(Map<String, String> parameters) {
        JobParametersBuilder parameterBuilder = new JobParametersBuilder();

        if (parameters != null) {
            for (Entry<String, String> entry : parameters.entrySet()) {
                parameterBuilder.addString(entry.getKey(), entry.getValue());
            }
        }

        return parameterBuilder.toJobParameters();
    }

}
