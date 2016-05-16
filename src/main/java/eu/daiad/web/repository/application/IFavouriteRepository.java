package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.favourite.UpsertFavouriteRequest;
import eu.daiad.web.model.favourite.FavouriteAccountInfo;
import eu.daiad.web.model.favourite.FavouriteGroupInfo;
import eu.daiad.web.model.favourite.FavouriteInfo;

public interface IFavouriteRepository {
	
	public abstract List <FavouriteInfo> getFavourites();
	
	public abstract FavouriteAccountInfo checkFavouriteAccount(UUID account_id);

	public abstract FavouriteGroupInfo checkFavouriteGroup(UUID group_id);

	public abstract void upsertFavourite(UpsertFavouriteRequest favouriteInfo);
	
}
