package eu.daiad.web.model.error;

public enum DeviceErrorCode implements ErrorCode {
	NOT_FOUND, NOT_SUPPORTED, ALREADY_EXISTS, CONFIGURATION_NOT_FOUND;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
