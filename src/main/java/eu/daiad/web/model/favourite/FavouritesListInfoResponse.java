package eu.daiad.web.model.favourite;

import java.util.List;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.favourite.FavouriteInfo;

public class FavouritesListInfoResponse extends RestResponse {
	private List <FavouriteInfo> favouritesInfo;

	public FavouritesListInfoResponse(List <FavouriteInfo> favouritesInfo) {
		this.favouritesInfo = favouritesInfo;
	}

	public FavouritesListInfoResponse(String code, String description) {
		super(code, description);
	}

	public List <FavouriteInfo> getFavouritesInfo() {
		return favouritesInfo;
	}
}
