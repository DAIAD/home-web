package eu.daiad.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.profile.EnumMobileMode;
import eu.daiad.web.model.profile.EnumUtilityMode;
import eu.daiad.web.model.profile.EnumWebMode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IUserRepository;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private UserDetailsService userService;

	@Autowired
	private IUserRepository userRepository;

	private RegexRequestMatcher apiMatcher = new RegexRequestMatcher("/api/v1/.*", null);

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		AuthenticatedUser user = null;

		try {
			// Check credentials
			String username = authentication.getName();
			String password = authentication.getCredentials().toString();

			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			user = (AuthenticatedUser) userService.loadUserByUsername(username);

			if ((user == null) || (!encoder.matches(password, user.getPassword()))) {
				throw new BadCredentialsException("Authentication has failed.");
			}

			if (!user.isAccountNonLocked()) {
				throw new BadCredentialsException("Account is locked.");
			}

			// Check application
			ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

			EnumApplication application = EnumApplication.fromString(sra.getRequest().getParameter("application"));

			if (application == EnumApplication.UNDEFINED) {
				if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
					application = EnumApplication.UTILITY;
				} else if (apiMatcher.matches(sra.getRequest())) {
					application = EnumApplication.MOBILE;
				} else {
					throw new BadCredentialsException("Application is unavailable");
				}
			}

			// Check credentials for requested application
			switch (application) {
				case UTILITY:
					if (!user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
						throw new BadCredentialsException("Authorization has failed.");
					}
					if (!user.getUtilityMode().equals(EnumUtilityMode.ACTIVE)) {
						throw new BadCredentialsException("Application is not enabled.");
					}
					break;
				case HOME:
					if (!user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
						throw new BadCredentialsException("Authorization has failed.");
					}
					if (!user.getWebMode().equals(EnumWebMode.ACTIVE)) {
						throw new BadCredentialsException("Application is not enabled.");
					}
					break;
				case MOBILE:
					if (!user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
						throw new BadCredentialsException("Authorization has failed.");
					}
					if ((!user.getMobileMode().equals(EnumMobileMode.ACTIVE))
									&& (!user.getMobileMode().equals(EnumMobileMode.INACTIVE))
									&& (!user.getMobileMode().equals(EnumMobileMode.LEARNING))) {
						throw new BadCredentialsException("Application is not enabled.");
					}
					break;
				default:
					throw new BadCredentialsException("Authorization has failed.");
			}

			updateLoginStats(user.getId(), true);

			return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), user.getAuthorities());
		} catch (Exception ex) {
			if (user != null) {
				updateLoginStats(user.getId(), false);
			}

			throw ex;
		}

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	private void updateLoginStats(int id, boolean success) {
		userRepository.updateLoginStats(id, success);
	};

}