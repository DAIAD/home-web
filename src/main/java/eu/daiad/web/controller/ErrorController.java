package eu.daiad.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

public class ErrorController extends BasicErrorController {

	public ErrorController(ErrorAttributes errorAttributes) {
		super(errorAttributes);
	}

	private HttpStatus getStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request
				.getAttribute("javax.servlet.error.status_code");
		if (statusCode != null) {
			try {
				return HttpStatus.valueOf(statusCode);
			}
			catch (Exception ex) {
			}
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
	
	@Override
	@RequestMapping(value = "${error.path:/error}", produces = "text/html")
	public ModelAndView errorHtml(HttpServletRequest request) {
		HttpStatus status = this.getStatus(request);
		
		switch(status) {
			case NOT_FOUND:
				return new ModelAndView("notfound");
			case FORBIDDEN:
				return new ModelAndView("denied");
			default:
				return new ModelAndView("error");
		}
	}

	@Override
	@RequestMapping(value = "${error.path:/error}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		return super.error(request);
	}

}