package eu.daiad.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.data.ProfileRepository;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.security.model.ApplicationUser;
import eu.daiad.web.security.model.AuthenticationResponse;

@Component
public class RESTAuthenticationSuccessHandler extends
		SimpleUrlAuthenticationSuccessHandler {

	private final Log logger = LogFactory.getLog(this.getClass());

	private boolean isAjaxRequest(HttpServletRequest request) {
		if (request.getMethod().equals("POST")
				&& "application/json".equals(request.getHeader("Content-Type"))) {
			return true;
		}

		String requestedWith = request.getHeader("X-Requested-With");
		return requestedWith != null ? "XMLHttpRequest".equals(requestedWith)
				: false;
	}

	protected static final String REQUEST_ATTRIBUTE_NAME = "_csrf";

	protected static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";
	protected static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";
	protected static final String RESPONSE_TOKEN_NAME = "X-CSRF-TOKEN";

	protected static final String DEFAULT_CSRF_TOKEN_ATTR_NAME = HttpSessionCsrfTokenRepository.class
			.getName().concat(".CSRF_TOKEN");

	@Autowired
	private ProfileRepository profileRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		clearAuthenticationAttributes(request);

		if (this.isAjaxRequest(request)) {
			if (response.isCommitted()) {
				logger.debug("Response has already been committed. Unable to send JSON response.");
				return;
			}
			try {

				Authentication auth = SecurityContextHolder.getContext()
						.getAuthentication();

				ApplicationUser user = (ApplicationUser) auth.getPrincipal();

				Profile profile = profileRepository.getProfileByUsername(user.getUsername());
				
				AuthenticationResponse authenticationResponse = new AuthenticationResponse(profile);

				CsrfToken sessionToken = (CsrfToken) request.getSession()
						.getAttribute(DEFAULT_CSRF_TOKEN_ATTR_NAME);
				CsrfToken requestToken = (CsrfToken) request
						.getAttribute(REQUEST_ATTRIBUTE_NAME);

				CsrfToken token = (sessionToken == null ? requestToken
						: sessionToken);

				if (token != null) {
					response.setHeader(RESPONSE_HEADER_NAME,
							token.getHeaderName());
					response.setHeader(RESPONSE_PARAM_NAME,
							token.getParameterName());
					response.setHeader(RESPONSE_TOKEN_NAME, token.getToken());
				}

				response.setContentType("application/json;charset=UTF-8");
				response.setHeader("Cache-Control", "no-cache");
				response.setStatus(HttpStatus.OK.value());

				ObjectMapper mapper = new ObjectMapper();
				response.getWriter().print(
						mapper.writeValueAsString(authenticationResponse));
			} catch (Exception e) {
				logger.debug(e.getMessage());
			}

		} else {
			super.onAuthenticationSuccess(request, response, authentication);
		}
	}
}