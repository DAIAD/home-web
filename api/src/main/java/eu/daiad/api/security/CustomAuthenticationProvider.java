package eu.daiad.api.security;

import org.apache.hadoop.util.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import eu.daiad.common.logging.MappedDiagnosticContextKeys;
import eu.daiad.common.model.EnumApplication;
import eu.daiad.common.model.profile.EnumMobileMode;
import eu.daiad.common.model.profile.EnumUtilityMode;
import eu.daiad.common.model.profile.EnumWebMode;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.repository.application.IUserRepository;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private UserDetailsService userService;

	@Autowired
	private IUserRepository userRepository;

	private RegexRequestMatcher apiMatcher = new RegexRequestMatcher("/api/v\\d+/.*", null);

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		AuthenticatedUser user = null;
		boolean impersonate = false;

		try {
			// Check credentials
			String[] username = StringUtils.split(authentication.getName(),':');
			String password = authentication.getCredentials().toString();

			if(username.length == 2) {
			    impersonate = true;
			}

			// Set authentication name
			MDC.put(MappedDiagnosticContextKeys.USERNAME, authentication.getName());

			// Check credentials
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			user = (AuthenticatedUser) userService.loadUserByUsername(username[0]);

			if ((user == null) || (!encoder.matches(password, user.getPassword()))) {
				throw new BadCredentialsException(String.format("Authentication has failed for user [%s].", authentication.getName()));
			}

			if (!user.isAccountNonLocked()) {
				throw new BadCredentialsException(String.format("Account [%s] is locked.", authentication.getName()));
			}

			// Set impersonating user
            if (impersonate) {
                if (user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
                    AuthenticatedUser impersonatedUser = (AuthenticatedUser) userService.loadUserByUsername(username[1]);

                    if (user.getUtilities().contains(impersonatedUser.getUtilityId())) {
                        user = impersonatedUser;
                    } else {
                        throw new BadCredentialsException(String.format("Cannot impersonate user [%s] with user [%s] from different utilities.", username[1], username[0]));
                    }
                } else {
                    throw new BadCredentialsException(String.format("User [%s] does not have the permission for impersonating other users.", username[0]));
                }
            }
			// Check application
			ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

			EnumApplication application = EnumApplication.fromString(sra.getRequest().getParameter("application"));

			if (application == EnumApplication.UNDEFINED) {
				if (user.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
					application = EnumApplication.UTILITY;
				} else if (apiMatcher.matches(sra.getRequest())) {
					application = EnumApplication.MOBILE;
				} else {
					throw new BadCredentialsException(String.format("Application is unavailable for user [%s].",
					                authentication.getName()));
				}
			}

			// Check credentials for requested application
			switch (application) {
				case UTILITY:
					if (!user.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
						throw new BadCredentialsException(
										String.format("Application UTILITY authorization has failed for user [%s] and role ROLE_ADMIN.",
										                authentication.getName()));
					}
					if (!user.getUtilityMode().equals(EnumUtilityMode.ACTIVE)) {
						throw new BadCredentialsException(
										String.format("Application UTILITY is not enabled for user [%s]. Current application mode [%s].",
										                authentication.getName(), user.getUtilityMode().toString()));
					}
					break;
				case HOME:
					if (!user.hasRole(EnumRole.ROLE_USER)) {
						throw new BadCredentialsException(String.format(
										"Application HOME authorization has failed for user [%s] and role ROLE_USER.",
										authentication.getName()));
					}
					if (!user.getWebMode().equals(EnumWebMode.ACTIVE)) {
						throw new BadCredentialsException(
										String.format("Application HOME is not enabled for user [%s]. Current application mode [%s].",
										                authentication.getName(), user.getWebMode().toString()));
					}
					break;
				case MOBILE:
					if (!user.hasRole(EnumRole.ROLE_USER)) {
						throw new BadCredentialsException(
										String.format("Application MOBILE authorization has failed for user [%s] and role ROLE_USER.",
										                authentication.getName()));
					}
					if ((!user.getMobileMode().equals(EnumMobileMode.ACTIVE))
									&& (!user.getMobileMode().equals(EnumMobileMode.INACTIVE))
									&& (!user.getMobileMode().equals(EnumMobileMode.LEARNING))) {
						throw new BadCredentialsException(
										String.format("Application MOBILE is not enabled for user [%s]. Current application mode [%s].",
										                authentication.getName(), user.getMobileMode().toString()));
					}
					break;
				default:
					throw new BadCredentialsException(String.format(
									"Authorization has failed. Application [%s] is not supported.",
									application.toString()));
			}

            if (!impersonate) {
                updateLoginStats(user.getId(), true);
            }
			return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), user.getAuthorities());
		} catch (Exception ex) {
			if (user != null) {
				updateLoginStats(user.getId(), false);
			}

			throw ex;
		}

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	private void updateLoginStats(int id, boolean success) {
		userRepository.updateLoginStats(id, success);
	};

}
