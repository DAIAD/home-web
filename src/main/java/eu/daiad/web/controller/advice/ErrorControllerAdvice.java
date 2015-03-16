package eu.daiad.web.controller.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(annotations = Controller.class)
public class ErrorControllerAdvice {

	private static final Log logger = LogFactory
			.getLog(ErrorControllerAdvice.class);

	@ExceptionHandler(Exception.class)
	public String handleIOException(Exception ex) {
		logger.error("Unhandled exception has occured in Controller class.");
		logger.error(ex);

		return "error";
	}

}
