package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.favourite.UpsertFavouriteRequest;
import eu.daiad.web.model.favourite.FavouriteAccountInfo;
import eu.daiad.web.model.favourite.FavouriteGroupInfo;
import eu.daiad.web.model.favourite.FavouriteInfo;
import eu.daiad.web.model.query.DataQuery;
import eu.daiad.web.model.query.NamedDataQuery;

public interface IFavouriteRepository {

    abstract List<FavouriteInfo> getFavourites();

    abstract FavouriteAccountInfo checkFavouriteAccount(UUID account_id);

    abstract FavouriteGroupInfo checkFavouriteGroup(UUID group_id);

    abstract void upsertFavourite(UpsertFavouriteRequest favouriteInfo);

    abstract void deleteFavourite(UUID favourite_id);

    abstract void addUserFavorite(UUID ownerKey, UUID userKey);

    abstract void addGroupFavorite(UUID ownerKey, UUID groupKey);

    abstract void deleteUserFavorite(UUID ownerKey, UUID userKey);

    abstract void deleteGroupFavorite(UUID ownerKey, UUID groupKey);

    abstract boolean isUserFavorite(UUID ownerKey, UUID userKey);

    abstract boolean isGroupFavorite(UUID ownerKey, UUID groupKey);
    
    abstract void insertFavouriteQuery(NamedDataQuery query);

}
