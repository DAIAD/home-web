package eu.daiad.web.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.SendMailTask;

/**
 * Helper builder class for initializing a job that sends mails to a list of recipients.
 */
@Component
public class SendMailJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Task for sending mails.
     */
    @Autowired
    private SendMailTask sendMailTask;

    /**
     * Creates a step that sends an email based on a template to multiple users..
     *
     * @return the new step.
     */
    private Step createClusterSegments() {
        return stepBuilderFactory.get(sendMailTask.getName()).tasklet(sendMailTask).build();
    }

    /**
     * Builds a job with the given name and {@link JobParametersIncrementer} instance.
     *
     * @param name the job name.
     * @param incrementer the {@link JobParametersIncrementer} instance to be used during job execution.
     *
     * @return a fully configured job.
     * @throws Exception in case the job can not be instantiated.
     */
    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name).incrementer(incrementer).start(createClusterSegments()).build();
    }
}
