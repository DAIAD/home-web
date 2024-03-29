package eu.daiad.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configures HBASE by adding a {@link PropertySource} with HBASE configuration options
 * to the application context. 
 */
@Configuration
@PropertySource("${hbase.properties}")
public class HBasePersistenceConfiguration {

}
