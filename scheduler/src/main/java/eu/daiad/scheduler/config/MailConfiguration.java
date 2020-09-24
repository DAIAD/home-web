package eu.daiad.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("${mail.properties}")
public class MailConfiguration {

}
