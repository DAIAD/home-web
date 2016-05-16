package eu.daiad.web.logging;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class MappedDiagnosticContextFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					javax.servlet.FilterChain filterChain) throws ServletException, IOException {
		// Set default anonymous user
		MDC.put("session.username", "(null)");

		// Set remote address
		String remoteAddress = request.getHeader("X-FORWARDED-FOR");
		if (StringUtils.isBlank(remoteAddress)) {
			remoteAddress = request.getRemoteAddr();
		}
		MDC.put("session.remote-address", remoteAddress);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}
}