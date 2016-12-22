package eu.daiad.web.job.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;

/**
 * Base class for task implementations.
 */
public abstract class BaseTask {

    /**
     * Spring application context.
     */
    @Autowired
    protected ApplicationContext applicationContext;

    /**
     * Resolves application messages and supports internationalization.
     */
    @Autowired
    protected MessageSource messageSource;

    /**
     * The step name.
     *
     * @return the step name.
     */
    public abstract String getName();

    /**
     * Creates a {@link ApplicationException} from the given {@link ErrorCode}.
     *
     * @param code the code.
     * @return the new exception.
     */
    protected ApplicationException createApplicationException(ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.create(code, pattern);
    }

    /**
     * Wraps a throwable with a new {@link ApplicationException}.
     *
     * @param t the throwable.
     * @return the new exception.
     */
    protected ApplicationException wrapApplicationException(Throwable t) {
        return this.wrapApplicationException(t, SharedErrorCode.UNKNOWN);
    }

    /**
     * Wraps a throwable with a new {@link ApplicationException} and assigns the given {@link ErrorCode}.
     *
     * @param t the throwable.
     * @param code the code.
     * @return the new exception.
     */
    protected ApplicationException wrapApplicationException(Throwable t, ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.wrap(t, code, pattern);
    }

}
