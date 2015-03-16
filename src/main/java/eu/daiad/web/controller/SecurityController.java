package eu.daiad.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;

import eu.daiad.web.security.model.CsrfConstants;

@RestController
public class SecurityController {

	@RequestMapping(value = "/csrf", method = RequestMethod.GET, produces = "application/json")
	public String csrf(HttpServletRequest request, HttpServletResponse response) {
		CsrfToken token = (CsrfToken) request
				.getAttribute(CsrfConstants.REQUEST_ATTRIBUTE_NAME);
		if (token != null) {
			response.setHeader(CsrfConstants.RESPONSE_HEADER_NAME,
					token.getHeaderName());
			response.setHeader(CsrfConstants.RESPONSE_PARAM_NAME,
					token.getParameterName());
			response.setHeader(CsrfConstants.RESPONSE_TOKEN_NAME,
					token.getToken());

			return token.getToken();
		}
		return "";
	}

}
