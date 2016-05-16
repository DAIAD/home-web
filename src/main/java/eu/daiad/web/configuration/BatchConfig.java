package eu.daiad.web.configuration;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.TaskExecutor;

@Configuration
@EnableBatchProcessing
@PropertySource("${batch.properties}")
public class BatchConfig {

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private TaskExecutor taskExecutor;

	@Bean
	public JobLauncher jobLauncher() {
		final SimpleJobLauncher jobLauncher = new SimpleJobLauncher();

		jobLauncher.setJobRepository(this.jobRepository);
		jobLauncher.setTaskExecutor(this.taskExecutor);

		return jobLauncher;
	}

}
