package eu.daiad.web.model.error;

public enum FavouriteErrorCode implements ErrorCode {
	INVALID_FAVOURITE_TYPE;

	@Override
	public String getMessageKey() {
		return (this.getClass().getSimpleName() + '.' + this.name());
	}
}