package eu.daiad.web.controller.advice;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

import eu.daiad.web.model.ResourceNotFoundException;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.SharedErrorCode;

@ControllerAdvice()
public class ErrorRestControllerAdvice {

	private static final Log logger = LogFactory.getLog(ErrorRestControllerAdvice.class);

	@Autowired
	Environment environment;

	@Autowired
	protected MessageSource messageSource;

	private String getMessage(String code) {
		return messageSource.getMessage(code, null, code, null);
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<RestResponse> handleException(Exception ex) {
		logger.error("Unhandled exception has occured in Controller class.", ex);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		if (ex instanceof ResourceNotFoundException) {
			String messageKey = SharedErrorCode.RESOURCE_NOT_FOUND.getMessageKey();
			RestResponse r = new RestResponse(messageKey, this.getMessage(messageKey));

			return new ResponseEntity<RestResponse>(r, headers, HttpStatus.NOT_FOUND);
		} else if (ex instanceof AccessDeniedException) {
			String messageKey = SharedErrorCode.AUTHORIZATION.getMessageKey();
			RestResponse r = new RestResponse(messageKey, this.getMessage(messageKey));

			return new ResponseEntity<RestResponse>(r, headers, HttpStatus.FORBIDDEN);
		}

		String messageKey = SharedErrorCode.UNKNOWN.getMessageKey();
		RestResponse r = new RestResponse(messageKey, this.getMessage(messageKey));

		return new ResponseEntity<RestResponse>(r, headers, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseBody
	public ResponseEntity<RestResponse> handleException(HttpMessageNotReadableException ex) {
		Throwable mostSpecificCause = ex.getMostSpecificCause();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		RestResponse r = null;

		if (mostSpecificCause == null) {
			String messageKey = SharedErrorCode.PARSE_ERROR.getMessageKey();
			r = new RestResponse(messageKey, this.getMessage(messageKey));

			return new ResponseEntity<RestResponse>(r, headers, HttpStatus.BAD_REQUEST);
		} else {
			String messageKey = SharedErrorCode.PARSE_ERROR.getMessageKey();
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
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String messageKey = SharedErrorCode.METHOD_NOT_SUPPORTED.getMessageKey();
		RestResponse r = new RestResponse(messageKey, this.getMessage(messageKey));

		return new ResponseEntity<RestResponse>(r, headers, HttpStatus.METHOD_NOT_ALLOWED);
	}

}
