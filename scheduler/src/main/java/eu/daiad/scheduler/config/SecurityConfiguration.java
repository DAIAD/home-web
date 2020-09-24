package eu.daiad.scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Configures application security.
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    /**
     * Configures the {@link HttpSecurity}.
     *
     * @param http the configuration for modifying web based security.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http.authorizeRequests().anyRequest().permitAll();
    	
    	http.csrf().disable();
    	
    	http.httpBasic().disable();
    }
}
