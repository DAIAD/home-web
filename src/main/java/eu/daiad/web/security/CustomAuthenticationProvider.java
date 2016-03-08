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
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.profile.EnumUtilityMode;
import eu.daiad.web.model.profile.EnumWebMode;
import eu.daiad.web.model.security.AuthenticatedUser;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private UserDetailsService userService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		// Check application
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		EnumApplication application = EnumApplication.fromString(sra.getRequest().getParameter("application"));

		if (application == EnumApplication.UNDEFINED) {
			throw new BadCredentialsException("Application is unavailable");
		}

		// Check credentials
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		AuthenticatedUser user = (AuthenticatedUser) userService.loadUserByUsername(username);

		if ((user == null) || (!encoder.matches(password, user.getPassword()))) {
			throw new BadCredentialsException("Authentication has failed.");
		}

		// Check credentials for requested application
		switch (application) {
		case UTILITY:
			if (!user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
				throw new BadCredentialsException("Authorization has failed.");
			}
			if(!user.getUtilityMode().equals(EnumUtilityMode.ACTIVE)) {
				throw new BadCredentialsException("Applicaiton is not enabled.");
			}
			break;
		case HOME:
			if (!user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
				throw new BadCredentialsException("Authorization has failed.");
			}
			if(!user.getWebMode().equals(EnumWebMode.ACTIVE)) {
				throw new BadCredentialsException("Applicaiton is not enabled.");
			}
			break;
		default:
			throw new BadCredentialsException("Authorization has failed.");
		}

		return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), user.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}