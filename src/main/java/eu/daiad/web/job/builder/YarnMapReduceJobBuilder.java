package eu.daiad.web.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.YarnMapReduceJobTask;

/**
 * Initializes and submits a MapReduce job to YARN.
 */
@Component
public class YarnMapReduceJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Job submission step name.
     */
    private static final String STEP_SUBMIT_JOB = "submit-job";

    /**
     * Task for submitting a MapReduce job to YARN.
     */
    @Autowired
    private YarnMapReduceJobTask yarnMapReduceJobTask;

    /**
     * Builds a step for executing a MapReduce job on a YARN cluster.
     *
     * @return the configured step.
     */
    private Step submitJob() {
        return stepBuilderFactory.get(STEP_SUBMIT_JOB)
                                 .tasklet(yarnMapReduceJobTask)
                                 .build();
    }

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name)
                                .incrementer(incrementer)
                                .start(submitJob()).build();
    }
}
