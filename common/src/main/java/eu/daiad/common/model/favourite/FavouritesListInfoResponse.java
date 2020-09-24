package eu.daiad.common.model.favourite;

import java.util.List;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class FavouritesListInfoResponse extends RestResponse {
	private List <FavouriteInfo> favouritesInfo;

	public FavouritesListInfoResponse(List <FavouriteInfo> favouritesInfo) {
		this.favouritesInfo = favouritesInfo;
	}

	public FavouritesListInfoResponse(ErrorCode code, String description) {
		super(code, description);
	}

	public List <FavouriteInfo> getFavouritesInfo() {
		return favouritesInfo;
	}
}
