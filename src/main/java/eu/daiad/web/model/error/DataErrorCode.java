package eu.daiad.web.model.error;

public enum DataErrorCode implements ErrorCode {
	TIME_GRANULARITY_NOT_SUPPORTED, DELETE_NOT_ALLOWED_FOR_HISTORY, NO_SESSION_FOUND_FOR_MEASUREMENT, HISTORY_SESSION_MEASUREMENT_TIMESTAMP_MISMATCH;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
