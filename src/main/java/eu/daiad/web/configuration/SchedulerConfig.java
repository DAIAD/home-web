package eu.daiad.web.configuration;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import eu.daiad.web.jobs.HBaseStatusMonitorJob;

@Configuration
@EnableScheduling
@PropertySource("${scheduler.properties}")
public class SchedulerConfig implements SchedulingConfigurer {

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private HBaseStatusMonitorJob hbaseHealthCheckTask;

	@Bean(destroyMethod = "shutdown")
	public Executor taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(10);

		return scheduler;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskScheduler());


	}
}
