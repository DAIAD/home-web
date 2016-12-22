package eu.daiad.web.job.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.connector.SftpProperties;
import eu.daiad.web.model.loader.DataTransferConfiguration;
import eu.daiad.web.service.IWaterMeterDataLoaderService;

/**
 * Job for downloading smart water meter data from a remote SFTP server and
 * storing it to HBase.
 */
@Component
public class WaterMeterDataSecureFileTransferJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Logger instance for writing events using the configured logging API.
     */
	private static final Log logger = LogFactory.getLog(WaterMeterDataSecureFileTransferJobBuilder.class);

	/**
	 * Service for downloading, parsing and importing smart water meter data to HBase.
	 */
	@Autowired
	private IWaterMeterDataLoaderService loader;

	private Step transferData() {
		return stepBuilderFactory.get("transferData").tasklet(new StoppableTasklet() {
			@Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
				try {
					// Initialize configuration
					DataTransferConfiguration config = new DataTransferConfiguration();

					// File properties
					config.setLocalFolder((String) chunkContext.getStepContext().getJobParameters().get("folder.local"));
					config.setRemoteFolder((String) chunkContext.getStepContext().getJobParameters()
									.get("folder.remote"));
					config.setTimezone((String) chunkContext.getStepContext().getJobParameters().get("timezone"));

					// Filter properties
					config.setFilterRegEx((String) chunkContext.getStepContext().getJobParameters().get("filter.regex"));

					// SFTP properties
					String host = (String) chunkContext.getStepContext().getJobParameters().get("sftp.host");
					int port = Integer.parseInt((String) chunkContext.getStepContext().getJobParameters()
									.get("sftp.port"));
					String username = (String) chunkContext.getStepContext().getJobParameters().get("sftp.username");
					String password = (String) chunkContext.getStepContext().getJobParameters().get("sftp.password");

					config.setSftpProperties(new SftpProperties(host, port, username, password));

					// Execute data file transfer and import
					loader.load(config);
				} catch (Exception ex) {
					logger.fatal("Failed to load meter data from SFTP server.", ex);

					throw ex;
				}
				return RepeatStatus.FINISHED;
			}

			@Override
			public void stop() {

			}
		}).build();
	}

	@Override
	public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
		return jobBuilderFactory.get(name).incrementer(incrementer).start(transferData()).build();
	}
}
