package eu.daiad.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Provides configuration for the DAIAD database
 */
@Configuration
@EnableJpaRepositories(
	basePackageClasses = { 
		eu.daiad.common.repository._Marker.class 
	}
)
@EnableTransactionManagement
public class DatabaseConfiguration {

}
