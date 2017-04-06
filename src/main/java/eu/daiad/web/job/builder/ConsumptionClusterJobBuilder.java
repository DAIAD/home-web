package eu.daiad.web.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.ConsumptionClusterTask;

/**
 * Helper builder class for initializing a job that clusters users based on their
 * monthly consumption per household member and computes water IQ rankings.
 */
@Component
public class ConsumptionClusterJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Name of the step that computes the clusters
     */
    public static final String TASK_CLUSTER_CREATION = "create-cluster";

    /**
     * Task that clusters users based on their consumption and computes water IQ status.
     */
    @Autowired
    private ConsumptionClusterTask consumptionClusterStep;

    /**
     * Creates a step that compute cluster segments.
     *
     * @return the new step.
     */
    private Step createClusterSegments() {
        return stepBuilderFactory.get(TASK_CLUSTER_CREATION).tasklet(consumptionClusterStep).build();
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
