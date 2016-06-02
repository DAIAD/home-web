package eu.daiad.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;

/**
 * Base service class providing helper methods for generating application messages and errors.
 *
 */
public abstract class BaseService {

	/**
	 * Resolves application messages and supported internationalization.
	 */
	@Autowired
	protected MessageSource messageSource;

	/**
	 * Creates an {@link ApplicationException} exception with a localized message.
	 * 
	 * @param code unique error code.
	 * @return the application exception.
	 */
	protected ApplicationException createApplicationException(ErrorCode code) {
		String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

		return ApplicationException.create(code, pattern);
	}

	/**
	 * Wraps an {@link Exception} and creates a new {@link ApplicationException} with a localized
	 * message. By default the {@code code} is set to {@link SharedErrorCode#UNKNOWN}.
	 * 
	 * @param ex the exception.
	 * @return the application exception.
	 */
	protected ApplicationException wrapApplicationException(Exception ex) {
		return this.wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
	}

	/**
	 * Wraps an {@link Exception} and creates a new {@link ApplicationException} with a localized
	 * message and the given {@link ErrorCode}.
	 *  
	 * @param ex the exception.
	 * @param code the error code.
	 * @return the application exception.
	 */
	protected ApplicationException wrapApplicationException(Exception ex, ErrorCode code) {
		String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

		return ApplicationException.wrap(ex, code, pattern);
	}
}
