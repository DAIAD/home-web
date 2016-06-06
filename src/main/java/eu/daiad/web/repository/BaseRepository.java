package eu.daiad.web.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.ibm.icu.text.MessageFormat;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ErrorCode;

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
}
