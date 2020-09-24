package eu.daiad.utility;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@SpringBootApplication(
    scanBasePackageClasses = {
        eu.daiad.common.hbase._Marker.class,
        eu.daiad.common.repository._Marker.class,
        eu.daiad.common.service._Marker.class,
        eu.daiad.utility.config._Marker.class,
        eu.daiad.utility.controller._Marker.class,
        eu.daiad.utility.feign.client._Marker.class,
        eu.daiad.utility.security._Marker.class,
        eu.daiad.utility.service._Marker.class,
    }
)
@EntityScan(
    basePackageClasses = {
        eu.daiad.common.domain._Marker.class,
    }
)
@EnableGlobalMethodSecurity(securedEnabled = true)
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
