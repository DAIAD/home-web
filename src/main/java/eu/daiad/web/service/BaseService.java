package eu.daiad.web.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.ibm.icu.text.MessageFormat;

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

    /**
     * Returns a localized message based on the error code.
     *
     * @param code the error code.
     * @return the localized message.
     */
    protected String getMessage(String code) {
        return messageSource.getMessage(code, null, code, null);
    }

    /**
     * Creates a localized message based on the error code and formats the
     * message using the given set of properties.
     *
     * @param code the error code.
     * @param properties the properties for formatting the message.
     * @return the localized message.
     */
    protected String getMessage(String code, Map<String, Object> properties) {
        String message = messageSource.getMessage(code, null, code, null);

        MessageFormat msgFmt = new MessageFormat(message);

        return msgFmt.format(properties);
    }

    /**
     * Returns a localized message based on an {@link ErrorCode}.
     *
     * @param error the error code.
     * @return the localized message.
     */
    protected String getMessage(ErrorCode error) {
        return getMessage(error.getMessageKey());
    }

    /**
     *
     * Returns a localized message based on an {@link ErrorCode}.
     *
     * @param error the error code.
     * @param keyValuePairs the properties for formatting the message expressed as key value pairs.
     * @return the localized message.
     */
    protected String getMessage(ErrorCode error, String... keyValuePairs) {
        Map<String, Object> properties = new HashMap<String, Object>();

        for (int i = 0, count = keyValuePairs.length; i < count; i += 2) {
            properties.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return getMessage(error, properties);
    }

    /**
     * Creates a localized message based on the {@link ErrorCode} and formats
     * the message using the given set of properties.
     *
     * @param error the error code.
     * @param properties the properties for formatting the message.
     * @return the localized message.
     */
    protected String getMessage(ErrorCode error, Map<String, Object> properties) {
        return getMessage(error.getMessageKey(), properties);
    }

}
