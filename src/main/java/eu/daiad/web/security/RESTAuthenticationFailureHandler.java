package eu.daiad.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.util.AjaxUtils;

@Component
public class RESTAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private final Log logger = LogFactory.getLog(this.getClass());

	@Autowired
	protected MessageSource messageSource;

	private String getMessage(String code) {
		return messageSource.getMessage(code, null, code, null);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {
		if (AjaxUtils.isAjaxRequest(request)) {
			if (response.isCommitted()) {
				logger.warn("Response has already been committed. Unable to send JSON response.");
				return;
			}
			try {
				response.setContentType("application/json;charset=UTF-8");
				response.setHeader("Cache-Control", "no-cache");
				response.setStatus(HttpStatus.FORBIDDEN.value());

				String messageKey = SharedErrorCode.AUTHENTICATION.getMessageKey();
				RestResponse r = new RestResponse(messageKey, this.getMessage(messageKey));

				ObjectMapper mapper = new ObjectMapper();
				response.getWriter().print(mapper.writeValueAsString(r));
			} catch (Exception ex) {
				logger.warn(ex);
			}
		} else {
			super.onAuthenticationFailure(request, response, exception);
		}
	}
}