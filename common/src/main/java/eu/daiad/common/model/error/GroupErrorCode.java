package eu.daiad.common.model.error;

public enum GroupErrorCode implements ErrorCode {
	GROUP_EXISTS, GROUP_DOES_NOT_EXIST, GROUP_ACCESS_RESTRICTED;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
