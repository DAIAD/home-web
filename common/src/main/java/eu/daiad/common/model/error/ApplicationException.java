package eu.daiad.common.model.error;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import com.ibm.icu.text.MessageFormat;

import eu.daiad.common.logging.MappedDiagnosticContextKeys;

public class ApplicationException extends RuntimeException {

	private static final long serialVersionUID = -6230724847321371745L;

	private ErrorCode code;

	private String pattern;

	private Map<String, Object> properties = new HashMap<String, Object>();

	private ApplicationException(ErrorCode code, String pattern) {
		super();

		this.code = code;
		this.pattern = pattern;
	}

	public static ApplicationException create(ErrorCode code, String pattern) {
		MDC.put(MappedDiagnosticContextKeys.ERROR_CATEGORY, code.getClass().getSimpleName());
		MDC.put(MappedDiagnosticContextKeys.ERROR_CODE, code.toString());

		return new ApplicationException(code, pattern);
	}

	private ApplicationException(Throwable cause, ErrorCode code, String pattern) {
		super(cause);

		this.code = code;
		this.pattern = pattern;
	}

	public static ApplicationException wrap(Throwable cause, ErrorCode code, String pattern) {
		if (cause instanceof ApplicationException) {
			ApplicationException applicationException = (ApplicationException) cause;

			if (code != SharedErrorCode.UNKNOWN && code != applicationException.getCode()) {

				MDC.put(MappedDiagnosticContextKeys.ERROR_CATEGORY, code.getClass().getSimpleName());
				MDC.put(MappedDiagnosticContextKeys.ERROR_CODE, code.toString());

				return new ApplicationException(cause, code, pattern);
			}
			return applicationException;
		} else {
			MDC.put(MappedDiagnosticContextKeys.ERROR_CATEGORY, code.getClass().getSimpleName());
			MDC.put(MappedDiagnosticContextKeys.ERROR_CODE, code.toString());

			return new ApplicationException(cause, code, pattern);
		}
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public ApplicationException set(String key, Object value) {
		this.properties.put(key, value);

		return this;
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		if (this.getCause() != null) {
			return this.getCause().getStackTrace();
		}
		return super.getStackTrace();
	}

	@Override
	public String getMessage() {
		if (StringUtils.isEmpty(this.pattern)) {
			return this.code.getMessageKey();
		}

		MessageFormat messageFormat = new MessageFormat(pattern);

		return messageFormat.format(properties);
	}

	public ErrorCode getCode() {
		return code;
	}

	public String getPattern() {
		return this.pattern;
	}
}
