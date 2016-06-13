package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.favourite.UpsertFavouriteRequest;
import eu.daiad.web.model.favourite.FavouriteAccountInfo;
import eu.daiad.web.model.favourite.FavouriteGroupInfo;
import eu.daiad.web.model.favourite.FavouriteInfo;

public interface IFavouriteRepository {

    abstract List<FavouriteInfo> getFavourites();

    abstract FavouriteAccountInfo checkFavouriteAccount(UUID account_id);

    abstract FavouriteGroupInfo checkFavouriteGroup(UUID group_id);

    abstract void upsertFavourite(UpsertFavouriteRequest favouriteInfo);

    abstract void deleteFavourite(UUID favourite_id);

    abstract void addFavorite(UUID ownerKey, UUID userKey);

    abstract void deleteFavorite(UUID ownerKey, UUID userKey);

    abstract boolean isFavorite(UUID ownerKey, UUID userKey);

}
