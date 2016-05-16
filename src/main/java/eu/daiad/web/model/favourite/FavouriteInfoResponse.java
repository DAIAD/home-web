package eu.daiad.web.model.favourite;

import eu.daiad.web.model.RestResponse;

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

	public FavouriteInfoResponse(String code, String description) {
		super(code, description);
	}

	public FavouriteInfo getFavouriteInfo() {
		return favouriteInfo;
	}
}
