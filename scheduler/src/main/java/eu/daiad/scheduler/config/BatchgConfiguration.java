package eu.daiad.scheduler.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.TaskExecutor;

/**
 *
 * Configures Spring Batch.
 *
 */
@Configuration
@EnableBatchProcessing
@DependsOn("dataSource")
@PropertySource("${batch.properties}")
public class BatchgConfiguration {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private TaskExecutor taskExecutor;

	@Bean
	@Primary
	public JobLauncher gobLauncher() {
		final SimpleJobLauncher jobLauncher = new SimpleJobLauncher();

		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.setTaskExecutor(taskExecutor);

		return jobLauncher;
	}

}
