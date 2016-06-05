package eu.daiad.web.controller;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.validation.FieldError;

import com.ibm.icu.text.MessageFormat;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.Error;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;

/**
 * 
 * Base controller class providing helper methods for generating application messages and errors.
 *
 */
public abstract class BaseController {

	/**
	 * Resolves application messages and supported internationalization.
	 */
	@Autowired
	protected MessageSource messageSource;

	/**
	 * Interface representing the environment in which the current application is running.
	 */
	@Autowired
	private Environment environment;

	/**
	 * Returns a localized messages based on the error code.
	 * 
	 * @param code the error code.
	 * @return the localized message.
	 */
	protected String getMessage(String code) {
		return messageSource.getMessage(code, null, code, null);
	}

	/**
	 * Returns a localized messages based on the error code.
	 * 
	 * @param code the error code.
	 * @param properties properties for formatting a message.
	 * @return the localized message.
	 */
	protected String getMessage(String code, Map<String, Object> properties) {
		String message = messageSource.getMessage(code, null, code, null);

		MessageFormat msgFmt = new MessageFormat(message);

		return msgFmt.format(properties);
	}

	/**
	 * Returns a localized messages based on an {@link Exception}.
	 * 
	 * @param ex the exception
	 * @return the localized message.
	 */
	protected String getMessage(ApplicationException ex) {
		return this.getMessage(ex.getCode().getMessageKey(), ex.getProperties());
	}

	/**
	 * Returns a localized messages based on the error code.
	 * 
	 * @param error the error code.
	 * @return the localized message.
	 */
	protected String getMessage(ErrorCode error) {
		return this.getMessage(error.getMessageKey());
	}

	/**
	 * Returns a localized messages based on the error code.
	 * 
	 * @param error the error code.
	 * @param properties properties for formatting a message.
	 * @return the localized message.
	 */
	protected String getMessage(ErrorCode error, Map<String, Object> properties) {
		return this.getMessage(error.getMessageKey(), properties);
	}

	/**
	 * Returns an {@link Error} based on a {@link FieldError}.
	 * @param error the field error.
	 * @return the localized error.
	 */
	protected Error getError(FieldError error) {
		String code = error.getCodes()[0];

		return new Error(code, this.getMessage(code));
	}

	/**
	 * Returns an {@link Error} based on a {@link ErrorCode}.
	 * 
	 * @param error the error code.
	 * @return the localized error.
	 */
	protected Error getError(ErrorCode error) {
		return new Error(error.getMessageKey(), this.getMessage(error));
	}

	/**
	 * Returns an {@link Error} based on a {@link ErrorCode}.
	 * 
	 * @param error the error code.
	 * @param properties properties for formatting a message.
	 * @return a localized error.
	 */
	protected Error getError(ErrorCode error, Map<String, Object> properties) {
		return new Error(error.getMessageKey(), this.getMessage(error, properties));
	}

	/**
	 * Returns an {@link Error} based on a {@link Throwable}.
	 * 
	 * @param t the throwable.
	 * @return the localized error.
	 */
	protected Error getError(Throwable t) {
		return this.getError((Exception) t.getCause());
	}

	/**
	 * Returns an {@link Error} based on a {@link Exception}.
	 * 
	 * @param ex the exception.
	 * @return the localized error.
	 */
	protected Error getError(Exception ex) {
		if (ex instanceof ApplicationException) {
			ApplicationException applicationException = (ApplicationException) ex;

			return new Error(applicationException.getCode().getMessageKey(), this.getMessage(applicationException));
		}
		return new Error(SharedErrorCode.UNKNOWN.getMessageKey(), this.getMessage(SharedErrorCode.UNKNOWN
						.getMessageKey()));
	}

	/**
	 * Creates a response based on a {@link ErrorCode}.
	 * @param error the error code.
	 * @return the response.
	 */
	protected RestResponse createResponse(ErrorCode error) {
		RestResponse response = new RestResponse();

		response.add(this.getError(error));

		return response;
	}

	/**
	 * Creates a response based on a {@link ErrorCode}.
	 * 
	 * @param error the error code.
	 * @param entries properties for formatting a message.
	 * @return the response.
	 */
	protected RestResponse createResponse(ErrorCode error, AbstractMap.SimpleEntry<String, Object>[] entries) {
		RestResponse response = new RestResponse();

		Map<String, Object> properties = new HashMap<String, Object>();
		for (AbstractMap.SimpleEntry<String, Object> entry : entries) {
			properties.put(entry.getKey(), entry.getValue());
		}

		response.add(this.getError(error, properties));

		return response;
	}

	/**
	 * Creates a response based on a {@link ErrorCode}.
	 * 
	 * @param error the error code.
	 * @param properties properties for formatting a message.
	 * @return the response
	 */
	protected RestResponse createResponse(ErrorCode error, Map<String, Object> properties) {
		RestResponse response = new RestResponse();

		response.add(this.getError(error, properties));

		return response;
	}

	/**
	 * Returns information about the application runtime.
	 * 
	 * @return the application runtime.
	 */
	protected eu.daiad.web.model.Runtime getRuntime() {
		return new eu.daiad.web.model.Runtime(this.getActiveProfiles());
	}

	/**
	 * Returns an array of the application active profiles.
	 * 
	 * @return the active profiles.
	 */
	protected String[] getActiveProfiles() {
		return environment.getActiveProfiles();
	}

	/**
	 * Creates an {@link ApplicationException} based on an {@link ErrorCode}.
	 * 
	 * @param code the error code.
	 * @return the application exception.
	 */
	protected ApplicationException createApplicationException(ErrorCode code) {
		String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

		return ApplicationException.create(code, pattern);
	}

	/**
	 * Wraps an {@link Exception} with a new {@link ApplicationException} and sets its error code to 
	 * {@link ErrorCode}.
	 * 
	 * @param ex the exception to wrap.
	 * @param code the error code to set.
	 * @return the application exception.
	 */
	protected ApplicationException wrapApplicationException(Exception ex, ErrorCode code) {
		String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

		return ApplicationException.wrap(ex, code, pattern);
	}
}
