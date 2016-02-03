package eu.daiad.web.model.error;

import java.util.HashMap;
import java.util.Map;

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
		if (ex instanceof ApplicationException) {
			ApplicationException appEx = (ApplicationException) ex;

			if (code != SharedErrorCode.UNKNOWN && code != appEx.getErrorCode()) {
				return new ApplicationException(ex.getMessage(), ex,  appEx.getErrorCode());
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
}
