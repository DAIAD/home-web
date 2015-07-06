package eu.daiad.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import eu.daiad.web.data.UserRepository;


@Service
public class CustomUserDetailsService implements UserDetailsService {
 
    @Autowired
    private UserRepository repository;
 
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    	try {
    		return repository.getUserByName(username);
    	} catch(Exception ex) {
    		// TODO : Add error handling
    	}
    	
    	return null;
    }
}