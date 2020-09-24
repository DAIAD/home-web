package eu.daiad.utility.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import eu.daiad.common.repository.application.IUserRepository;

/**
 * Initializes application security configuration including roles and default
 * utility administration accounts.
 */
@Component
public class SecurityInitializer implements CommandLineRunner {

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    /**
     * Initializes security configuration.
     */
    @Override
    public void run(String... args) throws Exception {
        this.userRepository.initializeSecurityConfiguration();
    }

}
