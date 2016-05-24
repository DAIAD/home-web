package eu.daiad.web.model.favourite;

import eu.daiad.web.model.RestResponse;

public class FavouriteAccountInfoResponse extends RestResponse {
	private FavouriteAccountInfo favouriteAccountInfo;

	public FavouriteAccountInfoResponse(FavouriteAccountInfo favouriteAccountInfo) {
		this.favouriteAccountInfo = favouriteAccountInfo;
	}

	public FavouriteAccountInfoResponse(String code, String description) {
		super(code, description);
	}

	public FavouriteAccountInfo getFavouriteAccountInfo() {
		return favouriteAccountInfo;
	}
}
