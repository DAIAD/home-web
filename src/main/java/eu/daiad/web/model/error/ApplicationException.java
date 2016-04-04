package eu.daiad.web.model.error;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ApplicationException extends RuntimeException {

	private static final long serialVersionUID = -6230724847321371745L;

	private ErrorCode code;

	private Map<String, Object> properties = new HashMap<String, Object>();

	public ApplicationException(ErrorCode code) {
		super();

		this.code = code;
	}

	public ApplicationException(String message, ErrorCode code) {
		super(message);

		this.code = code;
	}

	public ApplicationException(String message, Throwable cause, ErrorCode code) {
		super(message, cause);

		this.code = code;
	}

	public static ApplicationException wrap(Throwable ex, ErrorCode code) {
		Log logger = LogFactory.getLog(ex.getStackTrace()[0].getClassName());

		logger.error(ex.getMessage(), ex);

		if (ex instanceof ApplicationException) {
			ApplicationException appEx = (ApplicationException) ex;

			if (code != SharedErrorCode.UNKNOWN && code != appEx.getErrorCode()) {
				return new ApplicationException(ex.getMessage(), ex, appEx.getErrorCode());
			}
			return appEx;
		} else {
			return new ApplicationException(ex.getMessage(), ex, code);
		}
	}

	public static ApplicationException wrap(Throwable ex) {
		return ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
	}

	public ErrorCode getErrorCode() {
		return code;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public Object get(String key) {
		return this.properties.get(key);
	}

	public ApplicationException set(String key, Object value) {
		this.properties.put(key, value);

		return this;
	}

	public ApplicationException setCode(ErrorCode code) {
		this.code = code;

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
		if (StringUtils.isBlank(super.getMessage())) {
			StrBuilder builder = new StrBuilder();
			
			builder.appendln(this.getErrorCode().getMessageKey());
			
			for (String key : this.properties.keySet()) {
				builder.appendln(String.format("%s - %s", key, this.properties.get(key)));
			}
			
			return builder.toString();
		}
		return super.getMessage();
	}
}
