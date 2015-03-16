package eu.daiad.web;

import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import eu.daiad.web.controller.ErrorController;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	@Bean
	public ErrorController errorController(ErrorAttributes errorAttributes) {
		return new ErrorController(errorAttributes);
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/login").setViewName("login");
	}
}
