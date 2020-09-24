package eu.daiad.api.controller.advice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;
import eu.daiad.common.model.error.ResourceNotFoundException;
import eu.daiad.common.model.error.SharedErrorCode;

@ControllerAdvice(annotations = RestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErrorRestControllerAdvice {

	private static final Log logger = LogFactory.getLog(ErrorRestControllerAdvice.class);

	@Autowired
	Environment environment;

	@Autowired
	protected MessageSource messageSource;

	private String getMessage(ErrorCode code) {
		return messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);
	}

	protected String getMessage(ErrorCode code, Object... arguments) {
		String message = messageSource.getMessage(code.getMessageKey(), arguments, code.getMessageKey(), null);

		return message;
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<RestResponse> handleException(Exception ex) {
		logger.error("Unhandled exception has occured in Controller class.", ex);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		if (ex instanceof ResourceNotFoundException) {
			ErrorCode messageKey = SharedErrorCode.RESOURCE_NOT_FOUND;
			RestResponse r = new RestResponse(messageKey, this.getMessage(messageKey));

			return new ResponseEntity<RestResponse>(r, headers, HttpStatus.NOT_FOUND);
		} else if (ex instanceof AccessDeniedException) {
			ErrorCode messageKey = SharedErrorCode.AUTHORIZATION;
			RestResponse r = new RestResponse(messageKey, this.getMessage(messageKey));

			return new ResponseEntity<RestResponse>(r, headers, HttpStatus.FORBIDDEN);
		}

		ErrorCode messageKey = SharedErrorCode.UNKNOWN;
		RestResponse r = new RestResponse(messageKey, this.getMessage(messageKey));

		return new ResponseEntity<RestResponse>(r, headers, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseBody
	public ResponseEntity<RestResponse> handleException(HttpMessageNotReadableException ex) {
	    logger.warn("HTTP message is not readable.", ex);

		Throwable mostSpecificCause = ex.getMostSpecificCause();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		RestResponse r = null;

		if (mostSpecificCause == null) {
			ErrorCode messageKey = SharedErrorCode.PARSE_ERROR;
			r = new RestResponse(messageKey, this.getMessage(messageKey));

			return new ResponseEntity<RestResponse>(r, headers, HttpStatus.BAD_REQUEST);
		} else {
			ErrorCode messageKey = SharedErrorCode.PARSE_ERROR;
			if (Arrays.asList(environment.getActiveProfiles()).contains("development")) {
				r = new RestResponse(messageKey, mostSpecificCause.getMessage());
			} else {
				r = new RestResponse(messageKey, this.getMessage(messageKey));
			}

			return new ResponseEntity<RestResponse>(r, headers, HttpStatus.BAD_REQUEST);
		}
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseBody
	public ResponseEntity<RestResponse> handleException(HttpRequestMethodNotSupportedException ex) {
	    logger.warn("HTTP method not supported.", ex);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("method", ex.getMethod());

		ErrorCode messageKey = SharedErrorCode.METHOD_NOT_SUPPORTED;
		RestResponse r = new RestResponse(messageKey, this.getMessage(messageKey, properties));

		return new ResponseEntity<RestResponse>(r, headers, HttpStatus.METHOD_NOT_ALLOWED);
	}

}
