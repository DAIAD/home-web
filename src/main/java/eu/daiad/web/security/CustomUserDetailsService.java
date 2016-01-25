package eu.daiad.web.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import eu.daiad.web.data.IUserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private static final Log logger = LogFactory
			.getLog(CustomUserDetailsService.class);

	@Autowired
	private IUserRepository repository;

	@Override
	public UserDetails loadUserByUsername(final String username)
			throws UsernameNotFoundException {
		try {
			return repository.getUserByName(username);
		} catch (Exception ex) {
			logger.error(String.format("Failed to load user [%s].", username),
					ex);
		}

		return null;
	}
}