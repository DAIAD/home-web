package eu.daiad.web.model.error;

public enum MessageErrorCode implements ErrorCode {
	MESSAGE_TYPE_NOT_SUPPORTED;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
