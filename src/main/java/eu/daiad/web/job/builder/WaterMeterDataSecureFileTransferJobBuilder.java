package eu.daiad.web.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.ImportMeterDataTask;

/**
 * Job for downloading smart water meter data from a remote SFTP server and
 * storing it to HBase.
 */
@Component
public class WaterMeterDataSecureFileTransferJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Data import step name.
     */
    private static final String STEP_IMPORT_DATA = "import-data";

    /**
     * Task for importing meter data.
     */
    @Autowired
    private ImportMeterDataTask importMeterDataTask;

	private Step importData() {
        return stepBuilderFactory.get(STEP_IMPORT_DATA)
                        .tasklet(importMeterDataTask)
                        .build();
	}

	@Override
	public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
		return jobBuilderFactory.get(name)
		                        .incrementer(incrementer)
		                        .start(importData()).build();
	}
}
