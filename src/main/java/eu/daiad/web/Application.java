package eu.daiad.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

/**
 * Web Application entry class.
 */
@SpringBootApplication
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
