package eu.daiad.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import eu.daiad.web.model.security.CsrfConstants;

public class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {

	private RegexRequestMatcher apiMatcher = new RegexRequestMatcher("/api/v\\d+/.*", null);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					javax.servlet.FilterChain filterChain) throws ServletException, IOException {
		// Do not add CSRF token to API calls
		if (!apiMatcher.matches(request)) {
			CsrfToken requestToken = (CsrfToken) request.getAttribute(CsrfConstants.REQUEST_ATTRIBUTE_NAME);
			CsrfToken sessionToken = (CsrfToken) request.getSession().getAttribute(
							CsrfConstants.DEFAULT_CSRF_TOKEN_ATTR_NAME);

			CsrfToken token = (sessionToken == null ? requestToken : sessionToken);

			if (token != null) {
				response.setHeader(CsrfConstants.RESPONSE_HEADER_NAME, token.getHeaderName());
				response.setHeader(CsrfConstants.RESPONSE_PARAM_NAME, token.getParameterName());
				response.setHeader(CsrfConstants.RESPONSE_TOKEN_NAME, token.getToken());
			}
		}
		filterChain.doFilter(request, response);
	}
}