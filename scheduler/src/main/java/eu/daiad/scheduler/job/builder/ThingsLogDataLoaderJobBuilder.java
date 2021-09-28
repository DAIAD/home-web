package eu.daiad.scheduler.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.scheduler.job.task.ImportThingsLogMeterDataTask;

/**
 * Job for downloading meter data from ThingsLog service
 */
@Component
public class ThingsLogDataLoaderJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Data import step name.
     */
    private static final String STEP_IMPORT_DATA = "import-data";

    /**

    /**
     * Task for importing meter data.
     */
    @Autowired
    private ImportThingsLogMeterDataTask importThingsLogMeterDataTask;

    /**
     * Builds a step for loading data files and importing rows to HBASE.
     *
     * @return the configured step.
     */
	private Step importData() {
        return stepBuilderFactory.get(STEP_IMPORT_DATA)
                        .tasklet(importThingsLogMeterDataTask)
                        .build();
	}

	@Override
	public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
		return jobBuilderFactory.get(name)
		                        .incrementer(incrementer)
		                        .start(importData())
		                        .build();
	}
}
