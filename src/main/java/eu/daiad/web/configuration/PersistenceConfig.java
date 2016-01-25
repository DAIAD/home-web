package eu.daiad.web.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories("eu.daiad.data")
@EnableTransactionManagement
public class PersistenceConfig {

}
