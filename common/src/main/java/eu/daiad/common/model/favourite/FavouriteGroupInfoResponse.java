package eu.daiad.common.model.favourite;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class FavouriteGroupInfoResponse extends RestResponse {
	private FavouriteGroupInfo favouriteGroupInfo;

	public FavouriteGroupInfoResponse(FavouriteGroupInfo favouriteGroupInfo) {
		this.favouriteGroupInfo = favouriteGroupInfo;
	}

	public FavouriteGroupInfoResponse(ErrorCode code, String description) {
		super(code, description);
	}

	public FavouriteGroupInfo getFavouriteGroupInfo() {
		return favouriteGroupInfo;
	}
}
