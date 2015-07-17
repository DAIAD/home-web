package eu.daiad.web.security.configuration;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.authentication.AuthenticationManager;
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

	@Autowired
	private AuthenticationManager authManager;

	/*
	 * @Autowired private FilterChainProxy chain;
	 */

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/", "/login", "/libs/**", "/api/**")
				.permitAll();
		
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

				// CSRF for everything else that is not an API call or an
				// allowedMethod
				return true;
			}
		});

		http.authorizeRequests().anyRequest().fullyAuthenticated();

		http.formLogin().loginPage("/login").usernameParameter("username")
				.passwordParameter("password").failureUrl("/login?error")
				.defaultSuccessUrl("/")
				.successHandler(authenticationSuccessHandler)
				.failureHandler(authenticationFailureHandler);

		http.logout().logoutUrl("/logout").logoutSuccessUrl("/");

		http.exceptionHandling().accessDeniedPage("/login?error");

	
		http.addFilterAfter(new CsrfTokenResponseHeaderBindingFilter(),
				CsrfFilter.class);
	}
}
