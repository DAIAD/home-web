package eu.daiad.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ServletMvcConfiguration implements WebMvcConfigurer {


	/**
	 * Configure simple automated controllers.
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		// Add error pages
		registry.addViewController("/error/403").setViewName("error/403");
		registry.addViewController("/error/404").setViewName("error/404");
		registry.addViewController("/error/500").setViewName("error/500");
	}

}
