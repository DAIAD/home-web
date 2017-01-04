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

/**
 * Configures scheduler.
 */
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

	/**
	 * Registers {@link Executor} bean.
	 *
	 * @return the bean.
	 */
	@Bean(destroyMethod = "shutdown")
	public Executor executor() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(poolSize);
		scheduler.setThreadNamePrefix(threadNamePrefix);
		scheduler.setRemoveOnCancelPolicy(true);
		return scheduler;
	}

	/**
	 * Registers {@link TaskExecutor} bean.
	 *
	 * @return the bean.
	 */
	@Bean
	public TaskExecutor taskExecutor() {
		return (TaskExecutor) executor();
	}

	/**
	 * Registers {@link ThreadPoolTaskScheduler} bean.
	 *
	 * @return the bean.
	 */
	@Bean
	public ThreadPoolTaskScheduler taskScheduler() {
		return (ThreadPoolTaskScheduler) executor();
	}

	/**
	 * Configures tasks.
	 */
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskScheduler());
	}
}
