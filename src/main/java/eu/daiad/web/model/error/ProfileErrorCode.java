package eu.daiad.web.model.error;

import eu.daiad.web.model.error.ErrorCode;

public enum ProfileErrorCode implements ErrorCode {
	PROFILE_NOT_SUPPORTED,
	PROFILE_VERSION_NOT_FOUND;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
