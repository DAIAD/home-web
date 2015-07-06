package eu.daiad.web.security;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class SimpleCORSFilter implements Filter {

	private Pattern allowedMethods = Pattern
			.compile("^(GET|POST|OPTIONS|DELETE)$");
	
	private RegexRequestMatcher apiMatcher = new RegexRequestMatcher(
			"/api/v1/.*", null);

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		
		if (allowedMethods.matcher(request.getMethod()).matches() && apiMatcher.matches(request)) {
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods",
					"POST, GET, OPTIONS, DELETE");
			response.setHeader("Access-Control-Max-Age", "3600");
			response.setHeader("Access-Control-Allow-Headers",
					"x-requested-with,content-type");
		}
		chain.doFilter(req, res);
	}

	public void init(FilterConfig filterConfig) {
	}

	public void destroy() {
	}

}