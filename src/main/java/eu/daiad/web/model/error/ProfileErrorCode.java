package eu.daiad.web.model.error;

public enum ProfileErrorCode implements ErrorCode {
	PROFILE_NOT_SUPPORTED,
	PROFILE_VERSION_NOT_FOUND,
	HOUSEHOLD_MEMBER_DUPLICATE_INDEX;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + name());
	}
}
