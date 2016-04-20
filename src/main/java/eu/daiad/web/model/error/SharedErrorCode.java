package eu.daiad.web.model.error;

public enum SharedErrorCode implements ErrorCode {
	UNKNOWN, PARSE_ERROR, AUTHENTICATION, AUTHORIZATION, RESOURCE_NOT_FOUND, METHOD_NOT_SUPPORTED, FILE_DOES_NOT_EXIST, TIMEZONE_NOT_FOUND, DIR_CREATION_FAILED;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}
