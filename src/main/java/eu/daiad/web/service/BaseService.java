package eu.daiad.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;

public abstract class BaseService {

	@Autowired
	protected MessageSource messageSource;

	protected ApplicationException createApplicationException(ErrorCode code) {
		String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

		return ApplicationException.create(code, pattern);
	}

	protected ApplicationException wrapApplicationException(Exception ex) {
		return this.wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
	}

	protected ApplicationException wrapApplicationException(Exception ex, ErrorCode code) {
		String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

		return ApplicationException.wrap(ex, code, pattern);
	}
}
