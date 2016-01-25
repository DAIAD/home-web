package eu.daiad.web.configuration;

import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.daiad.web.controller.ErrorController;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	@Bean
	public ErrorController errorController(ErrorAttributes errorAttributes,
			ErrorProperties errorProperties) {
		// Override default implementation for ErrorController
		return new ErrorController(errorAttributes, errorProperties);
	}

	@Bean
	public Jackson2ObjectMapperBuilder objectMapperBuilder() {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

		// Add additional modules to JSON parser
		builder.modules(new JodaModule());

		return builder;
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		// Add error pages
		registry.addViewController("/error/403").setViewName("error/403");
		registry.addViewController("/error/404").setViewName("error/404");
		registry.addViewController("/error/500").setViewName("error/500");
	}
}
