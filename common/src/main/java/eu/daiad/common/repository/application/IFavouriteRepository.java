package eu.daiad.common.repository.application;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.daiad.common.domain.application.AccountEntity;
import eu.daiad.common.model.favourite.FavouriteInfo;
import eu.daiad.common.model.favourite.UpsertFavouriteRequest;
import eu.daiad.common.model.query.NamedDataQuery;

public interface IFavouriteRepository {

    abstract List<FavouriteInfo> getFavourites(UUID userKey);

    abstract void upsertFavourite(UpsertFavouriteRequest favouriteInfo);

    abstract void deleteFavourite(UUID favourite_id);

    abstract void addUserFavorite(UUID ownerKey, UUID userKey);

    abstract void addGroupFavorite(UUID ownerKey, UUID groupKey);

    abstract void deleteUserFavorite(UUID ownerKey, UUID userKey);

    abstract void deleteGroupFavorite(UUID ownerKey, UUID groupKey);

    abstract boolean isUserFavorite(UUID ownerKey, UUID userKey);

    abstract boolean isGroupFavorite(UUID ownerKey, UUID groupKey);

    abstract void insertFavouriteQuery(NamedDataQuery query, AccountEntity account);

    abstract void updateFavouriteQuery(NamedDataQuery namedDataQuery, AccountEntity account);

    abstract void deleteFavouriteQuery(NamedDataQuery namedDataQuery, AccountEntity account);

    abstract List<NamedDataQuery> getFavouriteQueriesForOwner(int accountId)
            throws JsonMappingException, JsonParseException, IOException;

    abstract void pinFavouriteQuery(long id, AccountEntity account);

    abstract void unpinFavouriteQuery(long id, AccountEntity account);

    abstract List<NamedDataQuery> getAllFavouriteQueries();

}
