package eu.daiad.home.security;

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

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.util.AjaxUtils;

@Component
public class RESTAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private final Log logger = LogFactory.getLog(this.getClass());

	@Autowired
	protected MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

	private String getMessage(String code) {
		return messageSource.getMessage(code, null, code, null);
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		logger.error(exception);

		if (AjaxUtils.isAjaxRequest(request)) {
			if (response.isCommitted()) {
				logger.warn("Response has already been committed. Unable to send JSON response.");
				return;
			}
			try {
				response.setContentType("application/json;charset=UTF-8");
				response.setHeader("Cache-Control", "no-cache");
				response.setStatus(HttpStatus.FORBIDDEN.value());

				ErrorCode messageKey = SharedErrorCode.AUTHENTICATION;
				RestResponse r = new RestResponse(messageKey, getMessage(messageKey.getMessageKey()));

				response.getWriter().print(objectMapper.writeValueAsString(r));
			} catch (Exception ex) {
				logger.warn(ex);
			}
		} else {
			super.onAuthenticationFailure(request, response, exception);
		}
	}
}