package eu.daiad.web.model;

public class ExportException extends Exception {

	private static final long serialVersionUID = -4383967914614147300L;

	public ExportException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExportException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExportException(String message) {
		super(message);
	}

	public ExportException(Throwable cause) {
		super(cause);
	}

	
}
