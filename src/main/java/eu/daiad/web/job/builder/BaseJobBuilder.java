package eu.daiad.web.job.builder;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;

/**
 * Helper job builder class.
 */
public abstract class BaseJobBuilder {

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
     * Convenient factory for a {@link JobBuilder} for building jobs instances.
     */
    @Autowired
    protected JobBuilderFactory jobBuilderFactory;

    /**
     * Convenient factory for a {@link StepBuilder} for building job steps.
     */
    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    /**
     * Creates a new {@link ApplicationException} for the given {@link ErrorCode}.
     *
     * @param code the error code.
     * @return the new exception.
     */
    protected ApplicationException createApplicationException(ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.create(code, pattern);
    }

    /**
     * Creates a new {@link ApplicationException} for the given {@link Throwable}.
     *
     * @param t an instance of {@link Throwable}.
     * @return the new exception.
     */
    protected ApplicationException wrapApplicationException(Throwable t) {
        return this.wrapApplicationException(t, SharedErrorCode.UNKNOWN);
    }

    /**
     * Creates a new {@link ApplicationException} for the given {@link ErrorCode} and {@link Throwable}.
     * @param t an instance of {@link Throwable}.
     * @param code the error code.
     * @return the new exception.
     */
    protected ApplicationException wrapApplicationException(Throwable t, ErrorCode code) {
        String pattern = messageSource.getMessage(code.getMessageKey(), null, code.getMessageKey(), null);

        return ApplicationException.wrap(t, code, pattern);
    }

}