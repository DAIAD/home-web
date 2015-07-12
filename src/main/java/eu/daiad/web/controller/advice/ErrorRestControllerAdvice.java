package eu.daiad.web.controller.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.AccessDeniedException;

import eu.daiad.web.model.ResourceNotFoundException;
import eu.daiad.web.model.RestResponse;

@ControllerAdvice(annotations = RestController.class)
public class ErrorRestControllerAdvice {

	private static final Log logger = LogFactory
			.getLog(ErrorRestControllerAdvice.class);

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<RestResponse> handleException(Exception ex) {
		logger.error("Unhandled exception has occured in Controller class.", ex);

		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);

	    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
	    
	    String message = "An unhandled exception has occured.";
	    
		if(ex instanceof ResourceNotFoundException) {
			status = HttpStatus.NOT_FOUND;
			message = ex.getMessage();
		} else if(ex instanceof AccessDeniedException) {
			status = HttpStatus.FORBIDDEN;
			message = ex.getMessage();
		}
		
		return new ResponseEntity<RestResponse>(new RestResponse(-1, message), headers, status);
	}

}
