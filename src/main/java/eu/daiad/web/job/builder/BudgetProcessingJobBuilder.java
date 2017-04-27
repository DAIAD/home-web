package eu.daiad.web.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.BudgetProcessingTask;

/**
 * Computes snapshots for active budgets.
 */
@Component
public class BudgetProcessingJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Job name.
     */
    public static final String JOB_NAME = "BUDGET";

    /**
     * Name of the step that computes budget snapshots.
     */
    public static final String STEP_BUDGET_PROCESSING = "compute-snapshot";

    /**
     * Task for computing budget snapshots.
     */
    @Autowired
    private BudgetProcessingTask budgetProcessingTask;

    /**
     * Builds a step for computing budget snapshots.
     *
     * @return the configured step.
     */
    private Step createBudgetProcessingTask() {
        return stepBuilderFactory.get(STEP_BUDGET_PROCESSING)
                                 .tasklet(budgetProcessingTask)
                                 .build();
    }

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name)
                                .incrementer(incrementer)
                                .start(createBudgetProcessingTask())
                                .build();
    }

}
