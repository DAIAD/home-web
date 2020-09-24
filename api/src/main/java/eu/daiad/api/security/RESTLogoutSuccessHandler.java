package eu.daiad.api.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.security.CsrfConstants;
import eu.daiad.common.util.AjaxUtils;

@Component
public class RESTLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private ObjectMapper objectMapper;

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		if (AjaxUtils.isAjaxRequest(request)) {
			response.setContentType("application/json;charset=UTF-8");
			response.setHeader("Cache-Control", "no-cache");
			response.setStatus(HttpStatus.OK.value());

			HttpSession session = request.getSession();

			CsrfToken sessionToken = (CsrfToken) session .getAttribute(CsrfConstants.DEFAULT_CSRF_TOKEN_ATTR_NAME);
			CsrfToken requestToken = (CsrfToken) request .getAttribute(CsrfConstants.REQUEST_ATTRIBUTE_NAME);

			CsrfToken token = (sessionToken == null ? requestToken : sessionToken);

			if (token != null) {
				response.setHeader(CsrfConstants.RESPONSE_HEADER_NAME, token.getHeaderName());
				response.setHeader(CsrfConstants.RESPONSE_PARAM_NAME, token.getParameterName());
				response.setHeader(CsrfConstants.RESPONSE_TOKEN_NAME, token.getToken());
			}

			response.getWriter().print(objectMapper.writeValueAsString(new RestResponse()));
		} else {
			response.sendRedirect("/");
		}

	}

}
