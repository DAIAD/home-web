package eu.daiad.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.daiad.web.model.Credentials;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;

public class BaseRestController extends BaseController {

	@Autowired
	private AuthenticationProvider authenticationProvider;

	protected AuthenticatedUser authenticate(Credentials credentials, EnumRole... roles) throws ApplicationException {
		try {
			UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) this.authenticationProvider
							.authenticate(new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword()));

			if (authentication == null) {
				throw new ApplicationException(SharedErrorCode.AUTHENTICATION);
			}

			// Check permissions
			for (EnumRole role : roles) {
				if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority(role.toString()))) {
					throw new ApplicationException(SharedErrorCode.AUTHORIZATION);
				}
			}
			// Store authenticated user
			SecurityContextHolder.getContext().setAuthentication(authentication);

			return (AuthenticatedUser) authentication.getPrincipal();
		} catch (BadCredentialsException ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.AUTHORIZATION);
		}
	}
}
