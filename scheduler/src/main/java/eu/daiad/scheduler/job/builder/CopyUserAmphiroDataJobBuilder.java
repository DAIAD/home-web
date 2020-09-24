package eu.daiad.scheduler.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.scheduler.job.task.CopyUserAmphiroDataTask;

/**
 * Job for copying amphiro b1 data between two users.
 */
@Component
public class CopyUserAmphiroDataJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Name of the step that copies data.
     */
    public static final String TASK_COPY_DATA = "copy-data";

    /**
     * Task that copies amphiro b1 data.
     */
    @Autowired
    private CopyUserAmphiroDataTask copyUserAmphiroDataTask;

    /**
     * Creates a step that copies amphiro b1 data.
     *
     * @return the new step.
     */
    private Step copyData() {
        return stepBuilderFactory.get(TASK_COPY_DATA).tasklet(copyUserAmphiroDataTask).build();
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
        return jobBuilderFactory.get(name).incrementer(incrementer).start(copyData()).build();
    }
}
