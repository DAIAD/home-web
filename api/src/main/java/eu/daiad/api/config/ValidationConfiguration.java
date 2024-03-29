package eu.daiad.api.config;

import javax.validation.Validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidationConfiguration
{
    @Bean(name = "defaultBeanValidator")
    Validator validator()
    {
		try (LocalValidatorFactoryBean b = new LocalValidatorFactoryBean()) {
			b.afterPropertiesSet();
			return b.getValidator();
		}
    }
}
