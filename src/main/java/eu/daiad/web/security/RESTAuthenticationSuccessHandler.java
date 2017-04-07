package eu.daiad.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.Runtime;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.AuthenticationResponse;
import eu.daiad.web.model.security.CsrfConstants;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IProfileRepository;
import eu.daiad.web.util.AjaxUtils;

@Component
public class RESTAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private Jackson2ObjectMapperBuilder objectMapperBuilder;

	@Autowired
	private IProfileRepository profileRepository;

	@Autowired
	private Environment environment;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
		clearAuthenticationAttributes(request);

		if (AjaxUtils.isAjaxRequest(request)) {
			if (response.isCommitted()) {
				logger.warn("Response has already been committed. Unable to send JSON response.");
				return;
			}
			try {

				Authentication auth = SecurityContextHolder.getContext().getAuthentication();

				AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

				Profile profile;
                if (user.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
					profile = profileRepository.getProfileByUserKey(user.getKey(), EnumApplication.UTILITY);
				} else {
					profile = profileRepository.getProfileByUserKey(user.getKey(), EnumApplication.HOME);
				}

				AuthenticationResponse authenticationResponse = new AuthenticationResponse(
	                new Runtime(environment.getActiveProfiles()),
	                profile,
	                user.roleToStringArray());

				CsrfToken sessionToken = (CsrfToken) request.getSession().getAttribute(CsrfConstants.DEFAULT_CSRF_TOKEN_ATTR_NAME);
				CsrfToken requestToken = (CsrfToken) request.getAttribute(CsrfConstants.REQUEST_ATTRIBUTE_NAME);

				CsrfToken token = (sessionToken == null ? requestToken : sessionToken);

				if (token != null) {
					response.setHeader(CsrfConstants.RESPONSE_HEADER_NAME, token.getHeaderName());
					response.setHeader(CsrfConstants.RESPONSE_PARAM_NAME, token.getParameterName());
					response.setHeader(CsrfConstants.RESPONSE_TOKEN_NAME, token.getToken());
				}

				response.setContentType("application/json;charset=UTF-8");
				response.setHeader("Cache-Control", "no-cache");
				response.setStatus(HttpStatus.OK.value());

				ObjectMapper mapper = objectMapperBuilder.build();
				response.getWriter().print(mapper.writeValueAsString(authenticationResponse));
			} catch (Exception ex) {
				logger.error(ex);
			}

		} else {
			super.onAuthenticationSuccess(request, response, authentication);
		}
	}
}