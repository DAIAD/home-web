package eu.daiad.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Web Application entry class.
 */
@SpringBootApplication(
    scanBasePackageClasses = {
        eu.daiad.common.hbase._Marker.class,
        eu.daiad.common.repository._Marker.class,
        eu.daiad.common.service._Marker.class,
        eu.daiad.scheduler.config._Marker.class,
        eu.daiad.scheduler.connector._Marker.class,
        eu.daiad.scheduler.controller._Marker.class,
        eu.daiad.scheduler.job.builder._Marker.class,
        eu.daiad.scheduler.job.task._Marker.class,
        eu.daiad.scheduler.service._Marker.class,
    }
)
@EntityScan(
    basePackageClasses = {
        eu.daiad.common.domain._Marker.class,
    }
)
public class Application extends SpringBootServletInitializer {

	/**
	 * Configure the application.
	 *
	 * @param builder a builder for the application context.
	 * @return the application builder.
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(Application.class);
	}

	/**
	 * Initializes and starts the application.
	 *
	 * @param args application arguments.
	 * @throws Exception if the initialization of the Spring Application Context fails.
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}
}
