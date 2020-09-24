package eu.daiad.common.model.error;

public enum FavouriteErrorCode implements ErrorCode {
	INVALID_FAVOURITE_TYPE, 
	FAVOURITE_DOES_NOT_EXIST, 
	FAVOURITE_ACCESS_RESTRICTED,
	;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}