package eu.daiad.common.model.error;

public enum DataErrorCode implements ErrorCode {
	TIME_GRANULARITY_NOT_SUPPORTED,
	DELETE_NOT_ALLOWED_FOR_HISTORY,
	NO_SESSION_FOUND_FOR_MEASUREMENT,
	HISTORY_SESSION_MEASUREMENT_FOUND,
	MEASUREMENT_NO_UNIQUE_INDEX,
	SESSION_NOT_FOUND,
	DUPLICATE_SESSION_ID,
	HBASE_INVALID_COLUMN,
	REALTIME_ALREADY_EXISTS;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + name());
	}
}
