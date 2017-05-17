package eu.daiad.web.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.CopyUserComparisonDataTask;

/**
 * Job for copying comparison and ranking data between two users.
 */
@Component
public class CopyUserComparisonDataJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Name of the step that copies data.
     */
    public static final String TASK_COPY_DATA = "copy-data";

    /**
     * Task that copies comparison and ranking data.
     */
    @Autowired
    private CopyUserComparisonDataTask copyUserComparisonDataTask;

    /**
     * Creates a step that copies comparison and ranking data.
     *
     * @return the new step.
     */
    private Step copyData() {
        return stepBuilderFactory.get(TASK_COPY_DATA).tasklet(copyUserComparisonDataTask).build();
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
