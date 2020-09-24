package eu.daiad.home.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eu.daiad.common.model.error.Error;
import eu.daiad.common.model.error.SharedErrorCode;

/**
 * Provides actions for returning errors to the clients.
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ErrorController extends BasicErrorController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(ErrorController.class);

    /**
     * Create a new {@link ErrorController} instance.
     *
     * @param errorAttributes the error attributes.
     * @param errorProperties the configuration properties.
     */
    public ErrorController(
        ErrorAttributes errorAttributes,
        ServerProperties serverProperties,
        ObjectProvider<List<ErrorViewResolver>> errorViewResolversProvider
    ) {
        super(errorAttributes, serverProperties.getError(), errorViewResolversProvider.getIfAvailable());
    }

    /**
     * Returns a {@link Map} of the error attributes. The map can be used as the model of
     * an error page {@link ModelAndView}, or returned as a {@link ResponseBody}.
     *
     * @param request the HTTP request.
     * @param includeStackTrace if stack trace elements should be included.
     * @return a map of error attributes.
     */
    @Override
    protected Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
    	final ErrorAttributeOptions options = ErrorAttributeOptions.of(
			Include.MESSAGE,
			Include.EXCEPTION,
			Include.STACK_TRACE,
			Include.BINDING_ERRORS
		);
    	
        final Map<String, Object> attrs = this.getErrorAttributes(request, options);
        
        return attrs;
    }

    /**
     * Resolves response HTTP status for the request.
     *
     * @param request the HTTP request.
     * @return the response HTTP status.
     */
    @Override
    protected HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            logger.error("Failed to resolve HTTP status.", ex);

            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Generates error pages for web clients.
     *
     * @param request the HTTP request.
     * @param response the HTTP response.
     */
    @Override
    @RequestMapping(produces = "text/html")
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = this.getStatus(request);

        String path = (String) getErrorAttributes(request, false).get("path");

        switch (status) {
            case NOT_FOUND:
                return new ModelAndView("redirect:/error/404");
            case UNAUTHORIZED:
            case FORBIDDEN:
            case METHOD_NOT_ALLOWED:
                if ((path != null) && (path.equals("/logout"))) {
                    return new ModelAndView("redirect:");
                }
                return new ModelAndView("redirect:/error/403");
            default:
                return new ModelAndView("redirect:/error/500");
        }
    }

    /**
     * Generates errors for clients using the HTTP API.
     *
     * @param request the HTTP request.
     * @return the HTTP response.
     */
    @Override
    @RequestMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> attributes = this.getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));

        ArrayList<Error> errors = new ArrayList<Error>();

        String messageKey = SharedErrorCode.UNKNOWN.getMessageKey();

        if (attributes.containsKey("exception")) {
            if (attributes.get("exception").equals(HttpMessageNotReadableException.class.getName())) {
                messageKey = SharedErrorCode.PARSE_ERROR.getMessageKey();
            } else if (attributes.get("exception").equals(HttpRequestMethodNotSupportedException.class.getName())) {
                messageKey = SharedErrorCode.METHOD_NOT_SUPPORTED.getMessageKey();
            }
        }

        errors.add(new Error(messageKey, attributes.get("message").toString()));

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("success", false);
        body.put("errors", errors);

        return new ResponseEntity<Map<String, Object>>(body, getStatus(request));
    }
}
