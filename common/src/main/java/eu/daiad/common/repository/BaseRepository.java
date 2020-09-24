package eu.daiad.common.repository;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ibm.icu.text.MessageFormat;

import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.ErrorCode;
import eu.daiad.common.model.security.AuthenticatedUser;

/**
 * Base repository that provides helper methods for message localization and
 * exception handling.
 */
public abstract class BaseRepository {

    /**
     * Message source for resolving messages, with support for the
     * parameterization and internationalization of such messages.
     */
    @Autowired
    protected MessageSource messageSource;

    /**
     * Resolves a message using the given {@link ErrorCode} and formats it using
     * the given properties.
     *
     * @param code the {@link ErrorCode}.
     * @return the formatted message.
     */
    protected String getMessage(ErrorCode code) {
        return messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);
    }

    /**
     * Resolves a message using the given {@link ErrorCode} and formats it using
     * the given properties.
     *
     * @param code the {@link ErrorCode}.
     * @param properties a map of key value pairs for formatting the message.
     * @return the formatted message.
     */
    protected String getMessage(ErrorCode code, Map<String, Object> properties) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        MessageFormat messageFormat = new MessageFormat(pattern);

        return messageFormat.format(properties);
    }

    /**
     * Creates an {@link ApplicationException} exception using the given {@link ErrorCode}.
     *
     * @param code the {@link ErrorCode}.
     * @return an {@link ApplicationException} exception.
     */
    protected ApplicationException createApplicationException(ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.create(code, pattern);
    }

    /**
     * Wraps an existing exception with a new instance of
     * {@link ApplicationException} using the given {@link ErrorCode}.
     *
     * @param ex the exception to wrap.
     * @param code the {@link ErrorCode}.
     * @return an {@link ApplicationException} exception.
     */
    protected ApplicationException wrapApplicationException(Exception ex, ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.wrap(ex, code, pattern);
    }

    /**
     * Return the currently authenticated user.
     *
     * @return the authenticated user.
     */
    protected AuthenticatedUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            return (AuthenticatedUser) auth.getPrincipal();
        }

        return null;
    }

    /**
     * Returns the utility id for the currently authenticated user.
     *
     * @return the utility id.
     */
    protected Integer getCurrentUtilityId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        }

        if (user != null) {
            return user.getUtilityId();
        }

        return null;
    }

    /**
     * Returns the utility key for the currently authenticated user.
     *
     * @return the utility key.
     */
    protected UUID getCurrentUtilityKey() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        }

        if (user != null) {
            return user.getUtilityKey();
        }

        return null;
    }
}
