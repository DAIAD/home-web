package eu.daiad.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ErrorController extends BasicErrorController {

	private final ErrorAttributes errorAttributes;

	public ErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
		super(errorAttributes, errorProperties);
		this.errorAttributes = errorAttributes;
	}

	protected Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
		RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		return this.errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
	}

	protected HttpStatus getStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if (statusCode == null) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		try {
			return HttpStatus.valueOf(statusCode);
		} catch (Exception ex) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

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

	@Override
	@RequestMapping
	@ResponseBody
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		return super.error(request);
	}

}