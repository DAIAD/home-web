package eu.daiad.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import eu.daiad.common.model.device.DefaultAmphiroProperties;

/**
 * Provides configuration options for Amphiro devices
 */
@Configuration
@EnableConfigurationProperties(DefaultAmphiroProperties.class)
public class AmphiroConfiguration {

}
