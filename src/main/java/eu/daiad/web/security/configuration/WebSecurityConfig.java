package eu.daiad.web.security.configuration;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import eu.daiad.web.security.*;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private RESTAuthenticationSuccessHandler authenticationSuccessHandler;

	@Autowired
	private RESTAuthenticationFailureHandler authenticationFailureHandler;

	@Autowired
	private CustomAuthenticationProvider provider;

	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
		auth.authenticationProvider(provider);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Allow anonymous access to selected requests
		http.authorizeRequests().antMatchers("/", "/login", "/libs/**", "/api/**")
				.permitAll();
		
		// Disable CSRF for API requests
		http.csrf().requireCsrfProtectionMatcher(new RequestMatcher() {
			
			private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
			
			private RegexRequestMatcher apiMatcher = new RegexRequestMatcher("/api/v1/.*", null);

			@Override
			public boolean matches(HttpServletRequest request) {
				// No CSRF due to allowedMethod
				if (allowedMethods.matcher(request.getMethod()).matches())
					return false;

				// No CSRF due to API call
				if (apiMatcher.matches(request))
					return false;

				// Apply CSRF for everything else that is not an API call or 
				// the request method does not match allowedMethod pattern
				return true;
			}
		});

		// Require authorization for all requests except for the aforementioned exceptions
		http.authorizeRequests().anyRequest().fullyAuthenticated();

		// Configure form based authentication for the web application
		http.formLogin()
			.loginPage("/login")
			.usernameParameter("username")
			.passwordParameter("password")
			.failureUrl("/login?error")
			.defaultSuccessUrl("/")
			.successHandler(authenticationSuccessHandler)
			.failureHandler(authenticationFailureHandler);

		// Configure logout page for the web application
		http.logout().logoutUrl("/logout").logoutSuccessUrl("/");

		http.exceptionHandling().accessDeniedPage("/login?error");

		// Refresh CSRF token 
		http.addFilterAfter(new CsrfTokenResponseHeaderBindingFilter(), CsrfFilter.class);
	}
}
