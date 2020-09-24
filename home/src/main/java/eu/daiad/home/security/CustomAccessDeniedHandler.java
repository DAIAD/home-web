package eu.daiad.home.security;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.util.AjaxUtils;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

	protected static final Log logger = LogFactory.getLog(CustomAccessDeniedHandler.class);

	private String errorPage = "/error/403";

	@Autowired
	protected MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

	@Autowired
	public CustomAccessDeniedHandler(@Value("${error-page:/error/403}") String errorPage) {
		this.errorPage = errorPage;
	}

	private String getMessage(String code) {
		return messageSource.getMessage(code, null, code, null);
	}

	@Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
	    logger.error(accessDeniedException);

		if (!response.isCommitted()) {
			if (errorPage != null) {
				if (AjaxUtils.isAjaxRequest(request)) {
					response.setContentType("application/json;charset=UTF-8");
					response.setHeader("Cache-Control", "no-cache");

					if (request.getRequestURI().equals("/logout")) {
						response.setStatus(HttpStatus.OK.value());

						response.getWriter().print(objectMapper.writeValueAsString(new RestResponse()));
					} else {
						response.setStatus(HttpStatus.FORBIDDEN.value());

						ErrorCode messageKey = SharedErrorCode.AUTHENTICATION;

						RestResponse r = new RestResponse(messageKey, getMessage(messageKey.getMessageKey()));

						response.getWriter().print(objectMapper.writeValueAsString(r));
					}
				} else {
					// Put exception into request scope (perhaps of use to a
					// view)
					request.setAttribute(WebAttributes.ACCESS_DENIED_403, accessDeniedException);

					// Set the 403 status code.
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);

					// forward to error page.
					RequestDispatcher dispatcher = request.getRequestDispatcher(errorPage);
					dispatcher.forward(request, response);
				}
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
			}
		}
	}

	public void setErrorPage(String errorPage) {
		if ((errorPage != null) && !errorPage.startsWith("/")) {
			throw new IllegalArgumentException("errorPage must begin with '/'");
		}

		this.errorPage = errorPage;
	}
}
