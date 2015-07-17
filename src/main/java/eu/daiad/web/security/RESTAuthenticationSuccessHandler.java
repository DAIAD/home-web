package eu.daiad.web.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import eu.daiad.web.security.model.*;

@Component
public class RESTAuthenticationSuccessHandler extends
		SimpleUrlAuthenticationSuccessHandler {

	private final Log logger = LogFactory.getLog(this.getClass());

	private boolean isAjaxRequest(HttpServletRequest request) {
		if (request.getMethod().equals("POST") && "application/json".equals(request.getHeader("Content-Type"))) {
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

	public static final String DEFAULT_CSRF_TOKEN_ATTR_NAME = HttpSessionCsrfTokenRepository.class.getName().concat(".CSRF_TOKEN");
	
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
				String username = "";
				
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (!(auth instanceof AnonymousAuthenticationToken)) {
					username = ((DaiadUser) auth.getPrincipal()).getFirstname();
				}
				
				LoginStatus status = new LoginStatus(true, username);

				CsrfToken sessionToken = (CsrfToken) request.getSession().getAttribute(DEFAULT_CSRF_TOKEN_ATTR_NAME);			
				CsrfToken requestToken = (CsrfToken) request.getAttribute(REQUEST_ATTRIBUTE_NAME);

				if ((requestToken != null) || (sessionToken !=null)) {
					response.setHeader(RESPONSE_HEADER_NAME, requestToken.getHeaderName());
					response.setHeader(RESPONSE_PARAM_NAME,	requestToken.getParameterName());
					response.setHeader(RESPONSE_TOKEN_NAME, (sessionToken == null ? requestToken.getToken() : sessionToken.getToken()));
				}

				ObjectMapper mapper = new ObjectMapper();

				String output = mapper.writeValueAsString(status);

				response.setContentType("text/x-json;charset=UTF-8");
				response.setHeader("Cache-Control", "no-cache");

				response.getWriter().print(output);
			} catch (Exception e) {
				logger.debug(e.getMessage());
			}

		} else {
			super.onAuthenticationSuccess(request, response, authentication);
		}
	}
}