package eu.daiad.web.model.error;

public enum UserErrorCode implements ErrorCode {
	ROLE_INITIALIZATION, ADMIN_INITIALIZATION, USERNANE_RESERVED, USERNANE_NOT_AVAILABLE, USERNANE_NOT_FOUND, 
	WHITELIST_MISMATCH, NO_ROLE_SELECTED, ROLE_NOT_FOUND;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
