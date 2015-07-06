package eu.daiad.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
 
    @Autowired
    private UserDetailsService userService;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        UserDetails user = userService.loadUserByUsername(username);

        if ((user == null) || (!encoder.matches(password, user.getPassword()))) {
            throw new BadCredentialsException("Authentication has failed.");
        }
        
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
 
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}