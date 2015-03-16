package eu.daiad.web.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import eu.daiad.web.security.model.*;

@Component
public class RESTAuthenticationSuccessHandler extends
		SimpleUrlAuthenticationSuccessHandler {

	@Value("${custom.security.user.name}")
	private String username;
	
	private final Log logger = LogFactory.getLog(this.getClass());

	private boolean isAjaxRequest(HttpServletRequest request) {
		String requestedWith = request.getHeader("X-Requested-With");
		return requestedWith != null ? "XMLHttpRequest".equals(requestedWith)
				: false;
	}

	protected static final String REQUEST_ATTRIBUTE_NAME = "_csrf";
	protected static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";
	protected static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";
	protected static final String RESPONSE_TOKEN_NAME = "X-CSRF-TOKEN";

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
				LoginStatus status = new LoginStatus(true, username);

				CsrfToken token = (CsrfToken) request
						.getAttribute(REQUEST_ATTRIBUTE_NAME);
				if (token != null) {
					response.setHeader(RESPONSE_HEADER_NAME,
							token.getHeaderName());
					response.setHeader(RESPONSE_PARAM_NAME,
							token.getParameterName());
					response.setHeader(RESPONSE_TOKEN_NAME, token.getToken());
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