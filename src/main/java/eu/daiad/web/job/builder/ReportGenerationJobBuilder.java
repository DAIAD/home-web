package eu.daiad.web.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.ReportCreationTask;

/**
 * Builder for creating a report generation job.
 */
@Component
public class ReportGenerationJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Report creation step name.
     */
    private static final String STEP_CREATE_REPORT = "create-report";

    /**
     * Task for creating reports.
     */
    @Autowired
    private ReportCreationTask reportCreationTask;

    /**
     * Creates a step for creating reports.
     *
     * @return the step.
     */
    private Step createReports() {
        return stepBuilderFactory.get(STEP_CREATE_REPORT)
                        .tasklet(reportCreationTask)
                        .build();
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
        return jobBuilderFactory.get(name).incrementer(incrementer).start(createReports()).build();
    }

}
