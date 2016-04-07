package eu.daiad.web.configuration;

import java.util.concurrent.Executor;

import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
@PropertySource("${scheduler.properties}")
public class SchedulerConfig implements SchedulingConfigurer {

	@Value("${pool-size}")
	private int poolSize;

	@Value("${thread-name-prefix}")
	private String threadNamePrefix;

	@Autowired
	private JobParametersIncrementer jobParametersIncrementer;

	@Bean(destroyMethod = "shutdown")
	public Executor executor() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(this.poolSize);
		scheduler.setThreadNamePrefix(this.threadNamePrefix);
		return scheduler;
	}

	@Bean
	public TaskExecutor taskExecutor() {
		return (TaskExecutor) executor();
	}

	@Bean
	public ThreadPoolTaskScheduler taskScheduler() {
		return (ThreadPoolTaskScheduler) executor();
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskScheduler());
	}
}
