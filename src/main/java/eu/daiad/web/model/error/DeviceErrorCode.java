package eu.daiad.web.model.error;

public enum DeviceErrorCode implements ErrorCode {
	NOT_FOUND, METER_NOT_FOUND, NOT_SUPPORTED, ALREADY_EXISTS, CONFIGURATION_NOT_FOUND, LOG_DATA_UPLOAD_FAILED, DEVICE_OWNER_NOT_FOUND, DEVICE_ACCESS_DENIED;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
