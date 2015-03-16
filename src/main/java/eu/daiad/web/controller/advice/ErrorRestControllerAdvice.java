package eu.daiad.web.controller.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.model.RestResponse;

@ControllerAdvice(annotations = RestController.class)
public class ErrorRestControllerAdvice {

	private static final Log logger = LogFactory
			.getLog(ErrorRestControllerAdvice.class);

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public RestResponse handleIOException(Exception ex) {
		logger.error("Unhandled exception has occured in Controller class.");
		logger.error(ex);

		return new RestResponse(-1, "An unhandled exception has occured");
	}

}
