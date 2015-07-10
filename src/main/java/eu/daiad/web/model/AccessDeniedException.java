package eu.daiad.web.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class AccessDeniedException extends Exception {

	private static final long serialVersionUID = 4620031115578556756L;

	public AccessDeniedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccessDeniedException(String message) {
		super(message);
	}

}
