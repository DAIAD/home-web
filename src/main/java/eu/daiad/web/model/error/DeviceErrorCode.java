package eu.daiad.web.model.error;

public enum DeviceErrorCode implements ErrorCode {
	NOT_FOUND, NOT_SUPPORTED, ALREADY_EXISTS;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
