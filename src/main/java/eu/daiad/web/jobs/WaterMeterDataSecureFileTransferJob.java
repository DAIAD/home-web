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
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.connector.SftpProperties;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.loader.DataTransferConfiguration;
import eu.daiad.web.service.IWaterMeterDataLoader;

@Component
public class WaterMeterDataSecureFileTransferJob implements IScheduledJob {

	private static final Log logger = LogFactory.getLog(WaterMeterDataSecureFileTransferJob.class);

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private IWaterMeterDataLoader loader;

	public WaterMeterDataSecureFileTransferJob() {

	}

	private Step transferData() {
		return stepBuilderFactory.get("transferData").tasklet(new Tasklet() {
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
				try {
					DataTransferConfiguration loaderConfig = new DataTransferConfiguration();

					loaderConfig.setLocalFolder((String) chunkContext.getStepContext().getJobParameters()
									.get("folder.local"));
					loaderConfig.setRemoteFolder((String) chunkContext.getStepContext().getJobParameters()
									.get("folder.remote"));
					loaderConfig.setTimezone((String) chunkContext.getStepContext().getJobParameters().get("timezone"));

					String host = (String) chunkContext.getStepContext().getJobParameters().get("sftp.host");
					int port = Integer.parseInt((String) chunkContext.getStepContext().getJobParameters()
									.get("sftp.port"));
					String username = (String) chunkContext.getStepContext().getJobParameters().get("sftp.username");
					String password = (String) chunkContext.getStepContext().getJobParameters().get("sftp.password");

					loaderConfig.setSftpProperties(new SftpProperties(host, port, username, password));

					loader.load(loaderConfig);
				} catch (Exception ex) {
					logger.fatal("Failed to load meter data from SFTP server.", ex);

					throw ApplicationException.wrap(ex);
				}
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Override
	public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
		return jobBuilderFactory.get(name).incrementer(incrementer).start(transferData()).build();
	}
}
