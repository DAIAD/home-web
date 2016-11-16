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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import eu.daiad.web.domain.admin.ScheduledJob;
import eu.daiad.web.domain.admin.ScheduledJobExecution;
import eu.daiad.web.job.builder.IJobBuilder;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.scheduling.ExecutionQuery;
import eu.daiad.web.model.scheduling.ExecutionQueryResult;
import eu.daiad.web.model.scheduling.JobExecutionInfo;
import eu.daiad.web.model.scheduling.JobInfo;
import eu.daiad.web.model.scheduling.JobInfoScheduleCron;
import eu.daiad.web.model.scheduling.JobInfoSchedulePeriodic;
import eu.daiad.web.service.BaseService;

// https://github.com/spring-projects/spring-batch-admin/blob/master/spring-batch-admin-manager/src/main/java/org/springframework/batch/admin/service/SimpleJobService.java;

@Service
public class DefaultSchedulerService extends BaseService implements ISchedulerService, InitializingBean {

    private static final Log logger = LogFactory.getLog(DefaultSchedulerService.class);

    private static final int DEFAULT_SHUTDOWN_TIMEOUT = 60 * 1000;

    @Value("${daiad.batch.server-time-zone:Europe/Athens}")
    private String serverTimeZone;

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
            updateScheduler();
        } catch (Exception ex) {
            logger.error("Failed to reload job metadata.", ex);
        }
    }

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

        DateTime startLocalDateTime;
        DateTime endLocalDateTime;

        info.setId(scheduledJob.getId());

        info.setCategory(scheduledJob.getCategory());
        info.setContainer(scheduledJob.getContainer());

        info.setName(scheduledJob.getName());
        info.setDescription(scheduledJob.getDescription());

        info.setEnabled(scheduledJob.isEnabled());

        ScheduledJobExecution lastExecution = schedulerRepository.getLastExecution(scheduledJob.getName());
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

        for (ScheduledJob scheduledJob : schedulerRepository.getJobs()) {
            jobs.add(scheduledJobToJobInfo(scheduledJob));
        }

        return jobs;
    }

    @Override
    public JobInfo getJob(long jobId) {
        JobInfo info = null;

        ScheduledJob scheduledJob = schedulerRepository.getJobById(jobId);

        if (scheduledJob != null) {
            info = scheduledJobToJobInfo(scheduledJob);
        }

        return info;
    }

    @Override
    public ExecutionQueryResult getJobExecutions(ExecutionQuery query) {
        return schedulerRepository.getExecutions(query);
    }

    @Override
    public List<JobExecutionInfo> getJobExecutions(long jobId, int startPosition, int maxResult) {
        List<JobExecutionInfo> result = new ArrayList<JobExecutionInfo>();

        List<ScheduledJobExecution> executions = schedulerRepository
                        .getExecutions(jobId, startPosition, maxResult);

        DateTime utcDateTime;

        for (ScheduledJobExecution execution : executions) {
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
            ScheduledJob scheduledJob = schedulerRepository.enable(jobId);

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

    private void updateScheduler() throws Exception {
        cleanupInterruptedJobs();

        for (ScheduledJob scheduledJob : schedulerRepository.getJobs()) {
            schedule(scheduledJob);
        }
    }

    private void schedule(ScheduledJob scheduledJob) throws Exception {
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
        if (!jobRegistry.getJobNames().contains(scheduledJob.getName())) {
            jobRegistry.register(new ReferenceJobFactory(batchJob));
        }

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
                logger.info(String.format("Initializing job [%s] with periodic trigger [%d].", scheduledJob.getName(),
                                scheduledJob.getPeriod()));

                ScheduledFuture<?> future = taskScheduler.schedule(new RunnableJob(activeExecutions, jobLauncher,
                                batchJob, jobParameters), new PeriodicTrigger(scheduledJob.getPeriod() * 1000));

                scheduledJobs.put(scheduledJob.getId(), new JobSchedulingProperties(scheduledJob.getId(), future));
            } else if (!StringUtils.isBlank(scheduledJob.getCronExpression())) {
                logger.info(String.format("Initializing job [%s] with CRON trigger expression [%s].", scheduledJob
                                .getName(), scheduledJob.getCronExpression()));

                ScheduledFuture<?> future = taskScheduler.schedule(new RunnableJob(activeExecutions, jobLauncher,
                                batchJob, jobParameters), new CronTrigger(scheduledJob.getCronExpression()));

                scheduledJobs.put(scheduledJob.getId(), new JobSchedulingProperties(scheduledJob.getId(), future));
            } else {
                logger.error(String.format("Failed to scheduler job [%s]. No trigger options is set.", scheduledJob
                                .getName()));
            }
        }
    }

    @Override
    public void launch(String jobName) throws ApplicationException {
        ScheduledJob job = schedulerRepository.getJobByName(jobName);

        this.launch(job.getId(), null);
    }


    @Override
    public void launch(String jobName, Map<String, String> parameters) throws ApplicationException {
        ScheduledJob job = schedulerRepository.getJobByName(jobName);

        this.launch(job.getId(), parameters);
    }

    @Override
    public void launch(long jobId) throws ApplicationException {
        this.launch(jobId, null);
    }

    @Override
    public void launch(long jobId, Map<String, String> parameters) throws ApplicationException {
        try {
            ScheduledJob scheduledJob = schedulerRepository.getJobById(jobId);

            for (JobExecution activeExecution : activeExecutions) {
                if (activeExecution.getJobInstance().getJobName().equals(scheduledJob.getName())) {
                    logger.info(String.format("Launching job [%s] failed. Job is already running.", scheduledJob
                                    .getName()));
                    return;
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
            JobParametersBuilder parameterBuilder = new JobParametersBuilder();
            for (eu.daiad.web.domain.admin.ScheduledJobParameter parameter : scheduledJob.getParameters()) {
                parameterBuilder.addString(parameter.getName(), parameter.getValue());
            }
            // Override parameters
            if (parameters != null) {
                for (Entry<String, String> entry : parameters.entrySet()) {
                    parameterBuilder.addString(entry.getKey(), entry.getValue());
                }
            }

            JobParameters jobParameters = parameterBuilder.toJobParameters();

            JobExecution jobExecution = jobLauncher.run(job, job.getJobParametersIncrementer().getNext(jobParameters));

            if (jobExecution.isRunning()) {
                activeExecutions.add(jobExecution);
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SchedulerErrorCode.SCHEDULER_JOB_LAUNCH_FAIL).set("job", jobId);
        }
    }

    @Override
    public boolean stop(Long executionId) {
        try {
            if (activeExecutions.contains(executionId)) {
                activeExecutions.remove(executionId);
            }
            return jobOperator.stop(executionId);
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

    @Scheduled(fixedDelay = 60000)
    public void removeInactiveExecutions() {
        for (Iterator<JobExecution> iterator = activeExecutions.iterator(); iterator.hasNext();) {
            JobExecution jobExecution = iterator.next();
            try {
                Set<Long> runningExecutions = jobOperator.getRunningExecutions(jobExecution.getJobInstance()
                                .getJobName());
                if (!runningExecutions.contains(jobExecution.getId())) {
                    iterator.remove();
                }
            } catch (NoSuchJobException e) {
                logger.error("Unexpected exception loading running executions", e);
            }
        }

    }

    private void cleanupInterruptedJobs() {
        try {
            List<ScheduledJobExecution> executions = schedulerRepository.getExecutionByExitStatus(ExitStatus.UNKNOWN);

            for (ScheduledJobExecution execution : executions) {
                schedulerRepository.updateJobExecutionStatus(execution.getJobExecutionId(), BatchStatus.ABANDONED);
            }
        } catch (Exception e) {
            logger.error("Failed to update abandoned job executions.", e);
        }
    }

}
