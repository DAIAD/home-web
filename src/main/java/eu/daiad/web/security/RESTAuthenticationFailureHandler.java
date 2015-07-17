package eu.daiad.web.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class RESTAuthenticationFailureHandler extends
		SimpleUrlAuthenticationFailureHandler {
	
	private final Log logger = LogFactory.getLog(this.getClass());

	private boolean isAjaxRequest(HttpServletRequest request) {
		if (request.getMethod().equals("POST") && "application/json".equals(request.getHeader("Content-Type"))) {
			return true;
		}
		
		String requestedWith = request.getHeader("X-Requested-With");
		return requestedWith != null ? "XMLHttpRequest".equals(requestedWith)
				: false;
	}
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {	
		if (this.isAjaxRequest(request)) {
			if (response.isCommitted()) {
				logger.debug("Response has already been committed. Unable to send JSON response.");
				return;
			}
			try {
				response.setContentType("text/x-json;charset=UTF-8");
				response.setHeader("Cache-Control", "no-cache");
				response.sendError(HttpStatus.FORBIDDEN.value(), "Authentication has failed.");		
			} catch (Exception e) {
				logger.debug(e.getMessage());
			}

		} else {
			super.onAuthenticationFailure(request, response, exception);
		}
	}
}