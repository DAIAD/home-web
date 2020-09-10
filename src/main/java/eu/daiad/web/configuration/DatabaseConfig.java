package eu.daiad.web.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * Provides configuration for the DAIAD database
 *
 */
@Configuration
@EnableJpaRepositories(
    basePackages = { "eu.daiad.web.repository.application" }
)
@EnableTransactionManagement
public class DatabaseConfig {

}
