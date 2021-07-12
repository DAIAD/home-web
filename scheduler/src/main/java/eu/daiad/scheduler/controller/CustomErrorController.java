package eu.daiad.scheduler.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Custom error controller that overrides default error handling behavior for
 * browser requests.
 *
 * For browser requests, the default "whitelabel" error view is replaced. The
 * HTTP status code is always set to OK (200). For 404 errors, the default index
 * view is returned and navigation is deferred to the client-side router. Any
 * other error is redirected to client-side /error/500 route.
 *
 * For API requests, controller advice exception handlers are used {@link RestControllerAdvice}.
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CustomErrorController extends BasicErrorController {

    public CustomErrorController(
        ErrorAttributes errorAttributes,
        ServerProperties serverProperties,
        ObjectProvider<List<ErrorViewResolver>> errorViewResolversProvider
    ) {
        super(errorAttributes, serverProperties.getError(), errorViewResolversProvider.getIfAvailable());
    }

    @Override
    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        final HttpStatus status = this.getStatus(request);

        // Always set status code to OK (200)
        response.setStatus(HttpStatus.OK.value());

        // For 404 error return the default page
        if (status == HttpStatus.NOT_FOUND) {
            return new ModelAndView("index");
        }

        // For all other errors redirect to 500 error page
        return new ModelAndView("redirect:/error/500");
    }

}