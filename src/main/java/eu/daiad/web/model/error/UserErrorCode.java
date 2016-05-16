package eu.daiad.web.model.error;

public enum UserErrorCode implements ErrorCode {
	ROLE_INITIALIZATION, ADMIN_INITIALIZATION, USERNANE_RESERVED, USERNANE_NOT_AVAILABLE, USERNANE_NOT_FOUND, 
	WHITELIST_MISMATCH, USERNAME_EXISTS_IN_WHITELIST, NO_ROLE_SELECTED, ROLE_NOT_FOUND, UTILITY_DOES_NOT_EXIST,
	USERID_NOT_FOUND, ACCOUNT_ACCESS_RESTRICTED;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
