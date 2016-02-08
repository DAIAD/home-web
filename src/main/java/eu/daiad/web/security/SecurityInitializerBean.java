package eu.daiad.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import eu.daiad.web.data.IUserRepository;

@Component
public class SecurityInitializerBean implements CommandLineRunner {

	@Autowired
	private IUserRepository userRepository;

	@Override
	public void run(String... args) throws Exception {
		this.userRepository.initializeSecurityConfiguration();
	}

}
