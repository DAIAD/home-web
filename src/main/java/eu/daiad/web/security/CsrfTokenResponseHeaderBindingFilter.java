package eu.daiad.web.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {

	protected static final String REQUEST_ATTRIBUTE_NAME = "_csrf";
	
	protected static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";
	protected static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";
	protected static final String RESPONSE_TOKEN_NAME = "X-CSRF-TOKEN";
	
	public static final String DEFAULT_CSRF_TOKEN_ATTR_NAME = HttpSessionCsrfTokenRepository.class.getName().concat(".CSRF_TOKEN");
	
	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, javax.servlet.FilterChain filterChain)
			throws ServletException, IOException {
		CsrfToken requestToken = (CsrfToken) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
		CsrfToken sessionToken = (CsrfToken) request.getSession().getAttribute(DEFAULT_CSRF_TOKEN_ATTR_NAME);
		
		CsrfToken token = (sessionToken == null ? requestToken : sessionToken);
		
		if (token != null) {
			response.setHeader(RESPONSE_HEADER_NAME, token.getHeaderName());
			response.setHeader(RESPONSE_PARAM_NAME, token.getParameterName());
			response.setHeader(RESPONSE_TOKEN_NAME, token.getToken());
		}
		filterChain.doFilter(request, response);
	}
}