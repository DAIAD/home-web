package eu.daiad.web.repository;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ibm.icu.text.MessageFormat;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;

public class BaseRepository {

    @Autowired
    protected MessageSource messageSource;

    protected String getMessage(ErrorCode code, Map<String, Object> properties) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        MessageFormat messageFormat = new MessageFormat(pattern);

        return messageFormat.format(properties);
    }

    protected ApplicationException createApplicationException(ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.create(code, pattern);
    }

    protected ApplicationException wrapApplicationException(Exception ex, ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.wrap(ex, code, pattern);
    }

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
