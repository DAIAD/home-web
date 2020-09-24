package eu.daiad.common.model.favourite;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class FavouriteInfoResponse extends RestResponse {
	private FavouriteInfo favouriteInfo;

	public FavouriteInfoResponse(FavouriteInfo favouriteInfo) {
		if (favouriteInfo instanceof FavouriteGroupInfo){
			this.favouriteInfo = (FavouriteGroupInfo) favouriteInfo;
		} else if (favouriteInfo instanceof FavouriteAccountInfo){
			this.favouriteInfo = (FavouriteAccountInfo) favouriteInfo;
		} else {
			this.favouriteInfo = favouriteInfo;
		}
	}

	public FavouriteInfoResponse(ErrorCode code, String description) {
		super(code, description);
	}

	public FavouriteInfo getFavouriteInfo() {
		return favouriteInfo;
	}
}
