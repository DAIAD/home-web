package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.favourite.UpsertFavouriteRequest;
import eu.daiad.web.model.favourite.FavouriteAccountInfo;
import eu.daiad.web.model.favourite.FavouriteGroupInfo;
import eu.daiad.web.model.favourite.FavouriteInfo;
import eu.daiad.web.model.query.NamedDataQuery;

import eu.daiad.web.domain.application.AccountEntity;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;

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
    
    abstract void insertFavouriteQuery(NamedDataQuery query, AccountEntity account);
    
    abstract void updateFavouriteQuery(NamedDataQuery namedDataQuery, AccountEntity account);
    
    abstract void deleteFavouriteQuery(NamedDataQuery namedDataQuery, AccountEntity account);
        
    abstract List<NamedDataQuery> getFavouriteQueriesForOwner(int accountId) 
            throws JsonMappingException, JsonParseException, IOException;
    
    abstract List<NamedDataQuery> getAllFavouriteQueries();

}
