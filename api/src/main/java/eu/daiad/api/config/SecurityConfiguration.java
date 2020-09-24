package eu.daiad.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;

import eu.daiad.api.security.CustomAccessDeniedHandler;
import eu.daiad.api.security.CustomAuthenticationProvider;
import eu.daiad.api.security.CustomLoginUrlAuthenticationEntryPoint;
import eu.daiad.api.security.RESTAuthenticationFailureHandler;
import eu.daiad.api.security.RESTAuthenticationSuccessHandler;
import eu.daiad.api.security.RESTLogoutSuccessHandler;
import eu.daiad.common.logging.filter.MappedDiagnosticContextFilter;

/**
 * Configures application security.
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String[] AUTHORIZED_PATHS = {
        "/",
        "/login",
        "/logout",
        "/error/**",
        "/password/reset/**",
        "/action/user/password/reset/token/create",
        "/action/user/password/reset/token/redeem",
        "/assets/**",
        "/api/**" };

    private static final String DOCUMENTATION_PATH = "/docs/**";

    @Bean
    protected ErrorProperties errorProperties() {
        return new ErrorProperties();
    }

    @Value("${server.login.force-https:true}")
    private boolean forceHttps;

    @Value("${daiad.docs.require-authentication:true}")
    private boolean documentationRequiresAuthentication;

    @Autowired
    private RESTAuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private RESTAuthenticationFailureHandler authenticationFailureHandler;

    @Autowired
    private RESTLogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    private CustomAuthenticationProvider authenticationProvider;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * Adds authentication based upon the custom {@link AuthenticationProvider}
     *
     * @param auth the authentication manager builder.
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }

    /**
     * Configures the {@link HttpSecurity}.
     *
     * @param http the configuration for modifying web based security.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Allow anonymous access to selected requests
        http.authorizeRequests().antMatchers(AUTHORIZED_PATHS).permitAll();
        if(documentationRequiresAuthentication) {
            http.authorizeRequests().antMatchers(DOCUMENTATION_PATH).access("hasRole('ROLE_ADMIN')");
        } else {
            http.authorizeRequests().antMatchers(DOCUMENTATION_PATH).permitAll();
        }

        // Disable CSRF for API requests
        http.csrf().disable();

        // Require authorization for all requests except for the aforementioned
        // exceptions
        http.authorizeRequests().anyRequest().fullyAuthenticated();

        // Configure form based authentication for the web application
        http.formLogin()
        	.loginPage("/login")
        	.usernameParameter("username")
        	.passwordParameter("password")
            .successHandler(authenticationSuccessHandler)
            .failureHandler(authenticationFailureHandler);

        // Configure logout page for the web application
        http.logout()
        	.logoutUrl("/logout")
        	.logoutSuccessHandler(logoutSuccessHandler);

        http.exceptionHandling()
        	.accessDeniedHandler(accessDeniedHandler)
        	.authenticationEntryPoint(new CustomLoginUrlAuthenticationEntryPoint("/login", forceHttps));

        // Set MDC before CSRF filter
        http.addFilterBefore(new MappedDiagnosticContextFilter(), CsrfFilter.class);
    }
}
