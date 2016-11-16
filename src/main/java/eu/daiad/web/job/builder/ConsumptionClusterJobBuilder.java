package eu.daiad.web.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.ConsumptionClusterTask;

@Component
public class ConsumptionClusterJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Spring application context.
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Job builder factory.
     */
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    /**
     * Step builder factory.
     */
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ConsumptionClusterTask consumptionClusterStep;

    /**
     * Creates a step that compute cluster segments.
     *
     * @return the new step.
     */
    private Step createClusterSegments() {
        return stepBuilderFactory.get(consumptionClusterStep.getName()).tasklet(consumptionClusterStep).build();
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
