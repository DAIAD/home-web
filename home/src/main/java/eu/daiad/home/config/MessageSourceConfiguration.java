package eu.daiad.home.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class MessageSourceConfiguration {

	@Bean
	MessageSource messageSource() {

		ResourceBundleMessageSource source = new ResourceBundleMessageSource();

		source.setBasenames(
			"messages-common",
			"messages-mail",
			"messages"
		);

		source.setUseCodeAsDefaultMessage(true);
		source.setDefaultEncoding("utf-8");

		return source;
	}

}
