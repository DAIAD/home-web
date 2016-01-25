package eu.daiad.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.model.RestResponse;

@Component
public class RESTLogoutSuccessHandler implements LogoutSuccessHandler {

	protected static final String REQUEST_ATTRIBUTE_NAME = "_csrf";

	protected static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";
	protected static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";
	protected static final String RESPONSE_TOKEN_NAME = "X-CSRF-TOKEN";

	protected static final String DEFAULT_CSRF_TOKEN_ATTR_NAME = HttpSessionCsrfTokenRepository.class
			.getName().concat(".CSRF_TOKEN");

	private boolean isAjaxRequest(HttpServletRequest request) {
		if (request.getMethod().equals("POST")
				&& "application/json".equals(request.getHeader("Content-Type"))) {
			return true;
		}

		String requestedWith = request.getHeader("X-Requested-With");
		return requestedWith != null ? "XMLHttpRequest".equals(requestedWith)
				: false;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		if (this.isAjaxRequest(request)) {
			response.setContentType("application/json;charset=UTF-8");
			response.setHeader("Cache-Control", "no-cache");
			response.setStatus(HttpStatus.OK.value());

			HttpSession session = request.getSession();
			if(session != null) {
				session.invalidate();
			}
			session = request.getSession();

			CsrfToken sessionToken = (CsrfToken) session
					.getAttribute(DEFAULT_CSRF_TOKEN_ATTR_NAME);
			CsrfToken requestToken = (CsrfToken) request
					.getAttribute(REQUEST_ATTRIBUTE_NAME);

			CsrfToken token = (sessionToken == null ? requestToken
					: sessionToken);

			if (token != null) {
				response.setHeader(RESPONSE_HEADER_NAME, token.getHeaderName());
				response.setHeader(RESPONSE_PARAM_NAME,
						token.getParameterName());
				response.setHeader(RESPONSE_TOKEN_NAME, token.getToken());
			}

			ObjectMapper mapper = new ObjectMapper();
			response.getWriter().print(
					mapper.writeValueAsString(new RestResponse()));
		} else {
			response.sendRedirect("/");
		}

	}

}
