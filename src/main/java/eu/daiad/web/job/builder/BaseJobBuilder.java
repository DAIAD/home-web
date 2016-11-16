package eu.daiad.web.job.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;

public abstract class BaseJobBuilder {

    @Autowired
    protected MessageSource messageSource;

    protected ApplicationException createApplicationException(ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.create(code, pattern);
    }

    protected ApplicationException wrapApplicationException(Throwable t) {
        return this.wrapApplicationException(t, SharedErrorCode.UNKNOWN);
    }

    protected ApplicationException wrapApplicationException(Throwable t, ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.wrap(t, code, pattern);
    }

}