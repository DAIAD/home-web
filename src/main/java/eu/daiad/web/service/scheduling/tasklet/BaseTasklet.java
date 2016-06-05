package eu.daiad.web.service.scheduling.tasklet;

import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;

public abstract class BaseTasklet {

	protected ApplicationContext applicationContext;

	protected MessageSource messageSource;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

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
