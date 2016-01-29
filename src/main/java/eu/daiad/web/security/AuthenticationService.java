package eu.daiad.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.ApplicationUser;
import eu.daiad.web.model.Credentials;

@Service
public class AuthenticationService {

	@Autowired
	private UserDetailsService userService;

	public boolean authenticate(Credentials credentials) {
		if (credentials == null) {
			return false;
		}
		return this.authenticate(credentials.getUsername(),
				credentials.getPassword());
	}

	public boolean authenticate(String username, String password) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		UserDetails user = userService.loadUserByUsername(username);

		if ((user == null) || (!encoder.matches(password, user.getPassword()))) {
			return false;
		}

		return true;
	}

	public ApplicationUser authenticateAndGetUser(Credentials credentials) {
		if (credentials == null) {
			return null;
		}
		return this.authenticateAndGetUser(credentials.getUsername(),
				credentials.getPassword());
	}

	public ApplicationUser authenticateAndGetUser(String username,
			String password) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		UserDetails user = userService.loadUserByUsername(username);

		if ((user == null) || (!encoder.matches(password, user.getPassword()))) {
			return null;
		}

		return (ApplicationUser) user;
	}
}
