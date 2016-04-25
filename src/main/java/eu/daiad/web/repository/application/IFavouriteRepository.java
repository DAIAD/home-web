package eu.daiad.web.repository.application;

import java.util.List;

import eu.daiad.web.model.favourite.FavouriteInfo;

public interface IFavouriteRepository {
	
	public abstract List <FavouriteInfo> getFavourites();
	
}
