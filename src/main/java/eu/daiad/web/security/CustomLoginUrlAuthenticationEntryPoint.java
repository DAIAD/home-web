package eu.daiad.web.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.util.AjaxUtils;

public class CustomLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

    private final String REQUIRE_AUHTENTICATION_HEADER = "X-Require-Authentication";

    public CustomLoginUrlAuthenticationEntryPoint(String loginFormUrl, boolean forceHttps) {
        super(loginFormUrl);

        setForceHttps(forceHttps);
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
                    throws IOException, ServletException {

        if (AjaxUtils.isAjaxRequest(request)) {
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader(REQUIRE_AUHTENTICATION_HEADER, request.getRequestURL().toString());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());

            RestResponse r = new RestResponse(SharedErrorCode.SESSION_EXPIRED.getMessageKey(), "Session has expired.");

            ObjectMapper mapper = buildObjectMapper();
            response.getWriter().print(mapper.writeValueAsString(r));
        } else {
            super.commence(request, response, authException);
        }
    }

    private ObjectMapper buildObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // Add additional modules to JSON parser
        builder.modules(new JodaModule(), new JtsModule());

        return builder.build();
    }
}
