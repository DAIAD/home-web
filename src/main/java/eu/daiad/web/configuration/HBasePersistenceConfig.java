package eu.daiad.web.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("${hbase.properties}")
public class HBasePersistenceConfig {

}
