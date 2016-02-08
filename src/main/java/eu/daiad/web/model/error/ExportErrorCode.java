package eu.daiad.web.model.error;

import eu.daiad.web.model.error.ErrorCode;

public enum ExportErrorCode implements ErrorCode {
	PATH_CREATION_FAILED;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
