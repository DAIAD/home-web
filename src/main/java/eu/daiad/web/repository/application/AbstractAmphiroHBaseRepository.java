package eu.daiad.web.repository.application;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import eu.daiad.web.repository.AbstractHBaseRepository;

/**
 * Abstract HBase repository specific to amphiro b1 data.
 */
public abstract class AbstractAmphiroHBaseRepository extends AbstractHBaseRepository {

    /**
     * Enforces more strict validation rules for amphiro b1 data.
     */
    @Value("${daiad.amphiro.validation-string:true}")
    protected boolean strictAmphiroValidation;

    /**
     * Logger for logging session data.
     */
    protected static final Log sessionLogger = LogFactory.getLog(LOGGER_SESSION);

    /**
     * Logger for logging measurement data.
     */
    protected static final Log sessionMemberLogger = LogFactory.getLog(LOGGER_MEMBER);

    /**
     * Logger for logging session member data.
     */
    protected static final Log sessionMeasurementLogger = LogFactory.getLog(LOGGER_MEASUREMENT);

    /**
     * Logger for logging ignored sessions (sessions that are not showers).
     */
    protected static final Log sessionIgnoreLogger = LogFactory.getLog(LOGGER_IGNORE);

    /**
     * Logger for logging historical showers that have been converted to
     * real-time ones manually by the user.
     */
    protected static final Log sessionRealTimeLogger = LogFactory.getLog(LOGGER_REAL_TIME);

    /**
     * Returns the current API version.
     *
     * @return the API version.
     */
    protected abstract String getVersion();

}
