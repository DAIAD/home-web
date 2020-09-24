package eu.daiad.common.model.favourite;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class FavouriteAccountInfoResponse extends RestResponse {
	private FavouriteAccountInfo favouriteAccountInfo;

	public FavouriteAccountInfoResponse(FavouriteAccountInfo favouriteAccountInfo) {
		this.favouriteAccountInfo = favouriteAccountInfo;
	}

	public FavouriteAccountInfoResponse(ErrorCode code, String description) {
		super(code, description);
	}

	public FavouriteAccountInfo getFavouriteAccountInfo() {
		return favouriteAccountInfo;
	}
}
