package eu.daiad.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.daiad.web.model.Credentials;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.security.AuthenticationService;

public class BaseRestController extends BaseController {

	@Autowired
	private AuthenticationService authenticationService;

	protected AuthenticatedUser authenticate(Credentials credentials, EnumRole... roles) throws ApplicationException {
		// Authenticate user
		AuthenticatedUser user = this.authenticationService.authenticateAndGetUser(credentials);
		if (user == null) {
			throw new ApplicationException(SharedErrorCode.AUTHENTICATION);
		}
		// Check permissions
		for (EnumRole role : roles) {
			if (!user.hasRole(role)) {
				throw new ApplicationException(SharedErrorCode.AUTHORIZATION);
			}
		}
		// Store authenticated user
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
						user.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		return user;
	}
}
