package eu.daiad.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(
	basePackageClasses = { 
		eu.daiad.common.repository._Marker.class 
	}
)
@EnableTransactionManagement
public class DatabaseConfiguration {

}
