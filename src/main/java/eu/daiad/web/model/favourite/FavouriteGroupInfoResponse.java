package eu.daiad.web.model.favourite;

import eu.daiad.web.model.RestResponse;

public class FavouriteGroupInfoResponse extends RestResponse {
	private FavouriteGroupInfo favouriteGroupInfo;

	public FavouriteGroupInfoResponse(FavouriteGroupInfo favouriteGroupInfo) {
		this.favouriteGroupInfo = favouriteGroupInfo;
	}

	public FavouriteGroupInfoResponse(String code, String description) {
		super(code, description);
	}

	public FavouriteGroupInfo getFavouriteGroupInfo() {
		return favouriteGroupInfo;
	}
}
