package eu.daiad.web.model.error;

public enum ActionErrorCode implements ErrorCode {
	EXPORT_TYPE_NOT_SUPPORTED,
	USER_METER_REGISTRATION_FAILED,
	FILE_TYPE_NOT_SUPPORTED;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}

}
