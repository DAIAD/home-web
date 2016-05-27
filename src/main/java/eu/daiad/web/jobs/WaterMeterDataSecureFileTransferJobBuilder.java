package eu.daiad.web.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.connector.SftpProperties;
import eu.daiad.web.model.loader.DataTransferConfiguration;
import eu.daiad.web.service.IWaterMeterDataLoaderService;

@Component
public class WaterMeterDataSecureFileTransferJobBuilder implements IJobBuilder {

	private static final Log logger = LogFactory.getLog(WaterMeterDataSecureFileTransferJobBuilder.class);

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private IWaterMeterDataLoaderService loader;

	public WaterMeterDataSecureFileTransferJobBuilder() {

	}

	private Step transferData() {
		return stepBuilderFactory.get("transferData").tasklet(new StoppableTasklet() {
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
				loader.cancel();
			}
		}).build();
	}

	@Override
	public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
		return jobBuilderFactory.get(name).incrementer(incrementer).start(transferData()).build();
	}
}
