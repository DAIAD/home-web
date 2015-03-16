package eu.daiad.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

public class ErrorController extends BasicErrorController {

	public ErrorController(ErrorAttributes errorAttributes) {
		super(errorAttributes);
	}

	@Override
	@RequestMapping(value = "${error.path:/error}", produces = "text/html")
	public ModelAndView errorHtml(HttpServletRequest request) {
		return super.errorHtml(request);
	}

	@Override
	@RequestMapping(value = "${error.path:/error}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		return super.error(request);
	}

}