package eu.daiad.common.logging.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import eu.daiad.common.logging.MappedDiagnosticContextKeys;

public class MappedDiagnosticContextFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
		HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
	) throws ServletException, IOException {
		
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication auth = securityContext.getAuthentication();
        
        // Set current user (if any)
		MDC.put(MappedDiagnosticContextKeys.USERNAME, auth == null ? "-" : auth.getName());
        
        // Determine and set remote address
        String remoteAddress = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddress == null) {
            remoteAddress = request.getRemoteAddr();
        }
        MDC.put(MappedDiagnosticContextKeys.CLIENT_ADDRESS, remoteAddress);
        
		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}
}