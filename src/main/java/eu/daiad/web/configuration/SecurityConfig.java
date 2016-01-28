package eu.daiad.web.configuration;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import eu.daiad.web.security.CsrfTokenResponseHeaderBindingFilter;
import eu.daiad.web.security.CustomAccessDeniedHandler;
import eu.daiad.web.security.CustomAuthenticationProvider;
import eu.daiad.web.security.RESTAuthenticationFailureHandler;
import eu.daiad.web.security.RESTAuthenticationSuccessHandler;
import eu.daiad.web.security.RESTLogoutSuccessHandler;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Bean
	protected ErrorProperties errorProperties() {
		return new ErrorProperties();
	}

	@Autowired
	private RESTAuthenticationSuccessHandler authenticationSuccessHandler;

	@Autowired
	private RESTAuthenticationFailureHandler authenticationFailureHandler;

	@Autowired
	private RESTLogoutSuccessHandler logoutSuccessHandler;

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
		http.authorizeRequests()
				.antMatchers("/", "/login", "/error/**", "/home/",
						"/utility/", "/assets/**", "/api/**").permitAll();

		// Disable CSRF for API requests
		http.csrf().requireCsrfProtectionMatcher(new RequestMatcher() {

			private Pattern allowedMethods = Pattern
					.compile("^(GET|HEAD|TRACE|OPTIONS)$");

			private RegexRequestMatcher apiMatcher = new RegexRequestMatcher(
					"/api/v1/.*", null);

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
		}).ignoringAntMatchers("/login");

		// Require authorization for all requests except for the aforementioned
		// exceptions
		http.authorizeRequests().anyRequest().fullyAuthenticated();

		// Configure form based authentication for the web application
		http.formLogin().loginPage("/login").usernameParameter("username")
				.passwordParameter("password")
				.successHandler(authenticationSuccessHandler)
				.failureHandler(authenticationFailureHandler);

		// Configure logout page for the web application
		http.logout().logoutUrl("/logout")
				.logoutSuccessHandler(logoutSuccessHandler);

		http.exceptionHandling().accessDeniedHandler(
				new CustomAccessDeniedHandler("/error/403"));

		// Refresh CSRF token
		http.addFilterAfter(new CsrfTokenResponseHeaderBindingFilter(),
				CsrfFilter.class);
	}
}
