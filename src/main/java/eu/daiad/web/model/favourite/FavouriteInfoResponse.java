package eu.daiad.web.model.favourite;

import java.util.List;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.favourite.FavouriteInfo;

public class FavouriteInfoResponse extends RestResponse {
	private List <FavouriteInfo> favouritesInfo;

	public FavouriteInfoResponse(List <FavouriteInfo> favouritesInfo) {
		this.favouritesInfo = favouritesInfo;
	}

	public FavouriteInfoResponse(String code, String description) {
		super(code, description);
	}

	public List <FavouriteInfo> getFavouritesInfo() {
		return favouritesInfo;
	}
}
