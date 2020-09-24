package eu.daiad.common.model.error;

public enum SharedErrorCode implements ErrorCode {
	UNKNOWN,
	NOT_IMPLEMENTED,
	RESOURCE_RELEASE_FAILED,
	PARSE_ERROR,
	JSON_SERIALIZE_ERROR,
	INVALID_PARSED_OBJECT,
	REQUIRED_FIELD,
	INVALID_FIELD,
	AUTHENTICATION,
	AUTHENTICATION_NO_CREDENTIALS,
	AUTHENTICATION_USERNAME,
	AUTHORIZATION,
	AUTHORIZATION_ANONYMOUS_SESSION,
	AUTHORIZATION_MISSING_ROLE,
	AUTHORIZATION_UTILITY_ACCESS_DENIED,
	SESSION_EXPIRED,
	RESOURCE_NOT_FOUND,
	METHOD_NOT_SUPPORTED,
	RESOURCE_DOES_NOT_EXIST,
	INVALID_TIME_ZONE,
	TIMEZONE_NOT_FOUND,
	LOCALE_NOT_SUPPORTED,
	DIR_CREATION_FAILED,
	INVALID_SRID,
	FILESYSTEM_NOT_SUPPORTED,
	INVALID_DATE_FORMAT,
	;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + name());
	}

	public static SharedErrorCode fromStatusCode(int value) {
		switch (value) {
		case 404:
			return SharedErrorCode.RESOURCE_NOT_FOUND;
		}
		return SharedErrorCode.UNKNOWN;
	}
	
}
