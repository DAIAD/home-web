package eu.daiad.web.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import eu.daiad.web.model.device.DefaultAmphiroProperties;

/**
 * Provides configuration options for Amphiro devices
 */
@Configuration
@EnableConfigurationProperties(DefaultAmphiroProperties.class)
public class AmphiroConfig {

}
