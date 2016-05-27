package eu.daiad.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;

public class BaseRestController extends BaseController {

	@Autowired
	private AuthenticationProvider authenticationProvider;

	protected AuthenticatedUser authenticate(Credentials credentials, EnumRole... roles) throws ApplicationException {
		try {
			if (credentials == null) {
				throw createApplicationException(SharedErrorCode.AUTHENTICATION_NO_CREDENTIALS);
			}

			UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) this.authenticationProvider
							.authenticate(new UsernamePasswordAuthenticationToken(credentials.getUsername(),
											credentials.getPassword()));

			if (authentication == null) {
				throw createApplicationException(SharedErrorCode.AUTHENTICATION_USERNAME).set("username",
								credentials.getUsername());
			}

			// Check permissions
			if (roles != null) {
				for (EnumRole role : roles) {
					if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority(role.toString()))) {
						throw createApplicationException(SharedErrorCode.AUTHORIZATION_MISSING_ROLE).set("role",
										role.toString());
					}
				}
			}

			// Store authenticated user
			SecurityContextHolder.getContext().setAuthentication(authentication);

			return (AuthenticatedUser) authentication.getPrincipal();
		} catch (BadCredentialsException ex) {
			throw wrapApplicationException(ex, SharedErrorCode.AUTHENTICATION_USERNAME).set("username",
							credentials.getUsername());
		}
	}
}
