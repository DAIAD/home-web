package eu.daiad.web.repository.application;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.domain.application.Account;
import eu.daiad.web.domain.application.DataQueryEntity;
import eu.daiad.web.domain.application.Favourite;
import eu.daiad.web.domain.application.FavouriteAccount;
import eu.daiad.web.domain.application.FavouriteGroup;
import eu.daiad.web.domain.application.Group;
import eu.daiad.web.domain.application.GroupSegment;
import eu.daiad.web.model.error.FavouriteErrorCode;
import eu.daiad.web.model.error.GroupErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.favourite.CandidateFavouriteAccountInfo;
import eu.daiad.web.model.favourite.CandidateFavouriteGroupInfo;
import eu.daiad.web.model.favourite.EnumFavouriteType;
import eu.daiad.web.model.favourite.FavouriteAccountInfo;
import eu.daiad.web.model.favourite.FavouriteGroupInfo;
import eu.daiad.web.model.favourite.FavouriteInfo;
import eu.daiad.web.model.favourite.UpsertFavouriteRequest;
import eu.daiad.web.model.group.EnumGroupType;
import eu.daiad.web.model.query.NamedDataQuery;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.BaseRepository;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
@Transactional("applicationTransactionManager")
public class JpaFavouriteRepository extends BaseRepository implements IFavouriteRepository {

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public List<FavouriteInfo> getFavourites() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            // Retrieve admin's account
            TypedQuery<Account> accountQuery = entityManager.createQuery("SELECT a FROM account a WHERE a.key = :key",
                            Account.class).setFirstResult(0).setMaxResults(1);
            accountQuery.setParameter("key", user.getKey());

            Account adminAccount = accountQuery.getSingleResult();

            TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
                            "SELECT f FROM favourite f WHERE f.owner = :owner", Favourite.class).setFirstResult(0);
            favouriteQuery.setParameter("owner", adminAccount);

            List<Favourite> favourites = favouriteQuery.getResultList();
            List<FavouriteInfo> favouritesInfo = new ArrayList<FavouriteInfo>();

            for (Favourite favourite : favourites) {
                FavouriteInfo favouriteInfo = new FavouriteInfo(favourite);
                favouritesInfo.add(favouriteInfo);
            }

            return favouritesInfo;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public FavouriteAccountInfo checkFavouriteAccount(UUID account_id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            FavouriteAccountInfo favouriteAccountInfo = null;

            // Retrieve admin's account
            TypedQuery<Account> adminAccountQuery = entityManager.createQuery(
                            "SELECT a FROM account a WHERE a.key = :key", Account.class).setFirstResult(0)
                            .setMaxResults(1);
            adminAccountQuery.setParameter("key", user.getKey());

            Account adminAccount = adminAccountQuery.getSingleResult();

            TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
                            "SELECT f FROM favourite f WHERE f.owner = :owner", Favourite.class).setFirstResult(0);
            favouriteQuery.setParameter("owner", adminAccount);

            List<Favourite> favourites = favouriteQuery.getResultList();

            for (Favourite favourite : favourites) {
                if (favourite.getType() == EnumFavouriteType.ACCOUNT) {
                    FavouriteAccount favouriteAccount = (FavouriteAccount) favourite;
                    if (favouriteAccount.getAccount().getKey().equals(account_id)) {
                        favouriteAccountInfo = new FavouriteAccountInfo(favouriteAccount);
                    }
                }
            }

            // If the given account does not match with any existing favourite
            // we try to retrieve it
            // in order to send a CandidateFavouriteAccountInfo Object
            if (favouriteAccountInfo == null) {
                try {
                    TypedQuery<Account> accountQuery = entityManager.createQuery(
                                    "SELECT a FROM account a WHERE a.key = :key", Account.class).setFirstResult(0)
                                    .setMaxResults(1);
                    accountQuery.setParameter("key", account_id);

                    Account account = accountQuery.getSingleResult();

                    return new CandidateFavouriteAccountInfo(account);

                } catch (NoResultException ex) {
                    throw wrapApplicationException(ex, UserErrorCode.USERID_NOT_FOUND).set("account_id", account_id);
                }
            }

            return favouriteAccountInfo;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public FavouriteGroupInfo checkFavouriteGroup(UUID group_id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            FavouriteGroupInfo favouriteGroupInfo = null;

            // Retrieve admin's account
            TypedQuery<Account> accountQuery = entityManager.createQuery("SELECT a FROM account a WHERE a.key = :key",
                            Account.class).setFirstResult(0).setMaxResults(1);
            accountQuery.setParameter("key", user.getKey());

            Account adminAccount = accountQuery.getSingleResult();

            TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
                            "SELECT f FROM favourite f WHERE f.owner = :owner", Favourite.class).setFirstResult(0);
            favouriteQuery.setParameter("owner", adminAccount);

            List<Favourite> favourites = favouriteQuery.getResultList();

            for (Favourite favourite : favourites) {
                if (favourite.getType() == EnumFavouriteType.GROUP) {
                    FavouriteGroup favouriteGroup = (FavouriteGroup) favourite;
                    if (favouriteGroup.getGroup().getKey().equals(group_id)) {
                        favouriteGroupInfo = new FavouriteGroupInfo(favouriteGroup);
                    }
                }
            }

            // If the given group does not match with any existing favourite we
            // try to retrieve it
            // in order to send a CandidateFavouriteGroupInfo Object
            if (favouriteGroupInfo == null) {
                try {
                    TypedQuery<Group> groupQuery = entityManager.createQuery(
                                    "SELECT g FROM group g WHERE g.key = :key", Group.class).setFirstResult(0)
                                    .setMaxResults(1);
                    groupQuery.setParameter("key", group_id);

                    Group group = groupQuery.getSingleResult();

                    return new CandidateFavouriteGroupInfo(group);

                } catch (NoResultException ex) {
                    throw wrapApplicationException(ex, GroupErrorCode.GROUP_DOES_NOT_EXIST).set("groupId", group_id);
                }
            }

            return favouriteGroupInfo;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

    }

    @Override
    public void upsertFavourite(UpsertFavouriteRequest favouriteInfo) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            // Retrieve admin's account
            TypedQuery<Account> adminAccountQuery = entityManager.createQuery(
                            "SELECT a FROM account a WHERE a.key = :key", Account.class).setFirstResult(0)
                            .setMaxResults(1);
            adminAccountQuery.setParameter("key", user.getKey());

            Account adminAccount = adminAccountQuery.getSingleResult();

            // Checking if the Favourite's type is invalid
            if (favouriteInfo.getType() != EnumFavouriteType.GROUP
                            && favouriteInfo.getType() != EnumFavouriteType.ACCOUNT) {
                throw createApplicationException(FavouriteErrorCode.INVALID_FAVOURITE_TYPE);
            }

            if (favouriteInfo.getType() == EnumFavouriteType.ACCOUNT) {
                // checking if the Account exists at all
                Account account = null;
                try {
                    TypedQuery<Account> accountQuery = entityManager.createQuery(
                                    "SELECT a FROM account a WHERE a.key = :key", Account.class).setFirstResult(0)
                                    .setMaxResults(1);
                    accountQuery.setParameter("key", favouriteInfo.getKey());

                    account = accountQuery.getSingleResult();
                } catch (NoResultException ex) {
                    throw wrapApplicationException(ex, UserErrorCode.USERID_NOT_FOUND).set("accountId",
                                    favouriteInfo.getKey());
                }

                // Checking if the selected account belongs to the same utility
                // as admin
                if (account.getUtility().getId() != adminAccount.getUtility().getId()) {
                    throw createApplicationException(UserErrorCode.ACCOUNT_ACCESS_RESTRICTED);
                }

                // Checking if the account is already an admin's favourite
                TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
                                "SELECT f FROM favourite f WHERE f.owner = :owner", Favourite.class).setFirstResult(0);
                favouriteQuery.setParameter("owner", adminAccount);

                List<Favourite> favourites = favouriteQuery.getResultList();

                FavouriteAccount selectedFavourite = null;
                for (Favourite favourite : favourites) {
                    if (favourite.getType() == EnumFavouriteType.ACCOUNT) {
                        FavouriteAccount favouriteAccount = (FavouriteAccount) favourite;
                        if (favouriteAccount.getAccount().getId() == account.getId()) {
                            selectedFavourite = favouriteAccount;
                        }
                    }
                }

                // Favourite already exists just set the label
                if (selectedFavourite != null) {
                    selectedFavourite.setLabel(favouriteInfo.getLabel());

                } else {
                    selectedFavourite = new FavouriteAccount();
                    selectedFavourite.setLabel(favouriteInfo.getLabel());
                    selectedFavourite.setOwner(adminAccount);
                    selectedFavourite.setCreatedOn(new DateTime());
                    selectedFavourite.setAccount(account);
                }

                this.entityManager.persist(selectedFavourite);
            } else {
                // checking if the Group exists at all
                Group group = null;
                try {
                    TypedQuery<Group> groupQuery = entityManager.createQuery(
                                    "SELECT g FROM group g WHERE g.key = :key", Group.class).setFirstResult(0)
                                    .setMaxResults(1);
                    groupQuery.setParameter("key", favouriteInfo.getKey());

                    group = groupQuery.getSingleResult();
                } catch (NoResultException ex) {
                    throw wrapApplicationException(ex, GroupErrorCode.GROUP_DOES_NOT_EXIST).set("groupId",
                                    favouriteInfo.getKey());
                }

                // Checking if the selected group belongs to the same utility as
                // admin
                if (group.getUtility().getId() != adminAccount.getUtility().getId()) {
                    throw createApplicationException(GroupErrorCode.GROUP_ACCESS_RESTRICTED);
                }

                // Checking if the group is already an admin's favourite
                TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
                                "SELECT f FROM favourite f WHERE f.owner = :owner", Favourite.class).setFirstResult(0);
                favouriteQuery.setParameter("owner", adminAccount);

                List<Favourite> favourites = favouriteQuery.getResultList();

                FavouriteGroup selectedFavourite = null;
                for (Favourite favourite : favourites) {
                    if (favourite.getType() == EnumFavouriteType.GROUP) {
                        FavouriteGroup favouriteGroup = (FavouriteGroup) favourite;
                        if (favouriteGroup.getGroup().getId() == group.getId()) {
                            selectedFavourite = favouriteGroup;
                        }
                    }
                }

                // Favourite already exists just set the label
                if (selectedFavourite != null) {
                    selectedFavourite.setLabel(favouriteInfo.getLabel());

                } else {
                    selectedFavourite = new FavouriteGroup();
                    selectedFavourite.setLabel(favouriteInfo.getLabel());
                    selectedFavourite.setOwner(adminAccount);
                    selectedFavourite.setCreatedOn(new DateTime());
                    selectedFavourite.setGroup(group);
                }

                this.entityManager.persist(selectedFavourite);
            }

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

    }

    @Override
    public void deleteFavourite(UUID favourite_id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            if (!user.hasRole("ROLE_ADMIN") && !user.hasRole("ROLE_SUPERUSER")) {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }

            Favourite favourite = null;
            // Check if favourite exists
            try {
                TypedQuery<Favourite> favouriteQuery = entityManager.createQuery(
                                "select f from favourite f where f.key = :favourite_id", Favourite.class)
                                .setFirstResult(0).setMaxResults(1);
                favouriteQuery.setParameter("favourite_id", favourite_id);
                favourite = favouriteQuery.getSingleResult();

                // Check that admin is the owner of the group
                // Get admin's account
                TypedQuery<eu.daiad.web.domain.application.Account> adminAccountQuery = entityManager.createQuery(
                                "select a from account a where a.id = :adminId",
                                eu.daiad.web.domain.application.Account.class).setFirstResult(0).setMaxResults(1);
                adminAccountQuery.setParameter("adminId", user.getId());
                Account adminAccount = adminAccountQuery.getSingleResult();

                if (favourite.getOwner() == adminAccount) {
                    this.entityManager.remove(favourite);

                } else {
                    throw createApplicationException(FavouriteErrorCode.FAVOURITE_ACCESS_RESTRICTED).set("favouriteId",
                                    favourite_id);
                }

            } catch (NoResultException ex) {
                throw wrapApplicationException(ex, FavouriteErrorCode.FAVOURITE_DOES_NOT_EXIST).set("favouriteId",
                                favourite_id);
            }

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void addUserFavorite(UUID ownerKey, UUID userKey) {
        if (isUserFavorite(ownerKey, userKey)) {
            return;
        }

        TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager.createQuery(
                        "select a from account a where a.key = :key", eu.daiad.web.domain.application.Account.class);

        query.setParameter("key", ownerKey);

        eu.daiad.web.domain.application.Account owner = query.getSingleResult();

        query.setParameter("key", userKey);

        eu.daiad.web.domain.application.Account account = query.getSingleResult();

        eu.daiad.web.domain.application.FavouriteAccount favorite = new eu.daiad.web.domain.application.FavouriteAccount();
        favorite.setAccount(account);
        favorite.setCreatedOn(new DateTime());
        favorite.setOwner(owner);
        favorite.setLabel(account.getFullname());

        entityManager.persist(favorite);
    }

    @Override
    public void deleteUserFavorite(UUID ownerKey, UUID userKey) {
        TypedQuery<eu.daiad.web.domain.application.FavouriteAccount> query = entityManager.createQuery(
                        "SELECT f FROM favourite_account f "
                                        + "WHERE f.owner.key = :ownerKey and f.account.key = :userKey",
                        eu.daiad.web.domain.application.FavouriteAccount.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("ownerKey", ownerKey);
        query.setParameter("userKey", userKey);

        List<FavouriteAccount> favorites = query.getResultList();

        if (!favorites.isEmpty()) {
            entityManager.remove(favorites.get(0));
        }
    }

    @Override
    public boolean isUserFavorite(UUID ownerKey, UUID userKey) {
        TypedQuery<eu.daiad.web.domain.application.FavouriteAccount> query = entityManager.createQuery(
                        "SELECT f FROM favourite_account f "
                                        + "WHERE f.owner.key = :ownerKey and f.account.key = :userKey",
                        eu.daiad.web.domain.application.FavouriteAccount.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("ownerKey", ownerKey);
        query.setParameter("userKey", userKey);

        return (!query.getResultList().isEmpty());
    }

    @Override
    public void addGroupFavorite(UUID ownerKey, UUID groupKey) {
        if (isGroupFavorite(ownerKey, groupKey)) {
            return;
        }

        TypedQuery<eu.daiad.web.domain.application.Account> accountQuery = entityManager.createQuery(
                        "select a from account a where a.key = :key", eu.daiad.web.domain.application.Account.class);

        accountQuery.setParameter("key", ownerKey);

        eu.daiad.web.domain.application.Account owner = accountQuery.getSingleResult();

        TypedQuery<eu.daiad.web.domain.application.Group> groupQuery = entityManager.createQuery(
                        "select g from group g where g.key = :key", eu.daiad.web.domain.application.Group.class);

        groupQuery.setParameter("key", groupKey);

        eu.daiad.web.domain.application.Group group = groupQuery.getSingleResult();

        eu.daiad.web.domain.application.FavouriteGroup favorite = new eu.daiad.web.domain.application.FavouriteGroup();
        favorite.setGroup(group);
        favorite.setCreatedOn(new DateTime());
        favorite.setOwner(owner);
        if (group.getType() == EnumGroupType.SEGMENT) {
            favorite.setLabel(((GroupSegment) group).getCluster().getName() + " - " + group.getName());
        } else {
            favorite.setLabel(group.getName());
        }

        entityManager.persist(favorite);
    }

    @Override
    public void deleteGroupFavorite(UUID ownerKey, UUID groupKey) {
        TypedQuery<eu.daiad.web.domain.application.FavouriteGroup> query = entityManager.createQuery(
                        "SELECT f FROM favourite_group f "
                                        + "WHERE f.owner.key = :ownerKey and f.group.key = :groupKey",
                        eu.daiad.web.domain.application.FavouriteGroup.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("ownerKey", ownerKey);
        query.setParameter("groupKey", groupKey);

        List<FavouriteGroup> favorites = query.getResultList();

        if (!favorites.isEmpty()) {
            entityManager.remove(favorites.get(0));
        }
    }

    @Override
    public boolean isGroupFavorite(UUID ownerKey, UUID groupKey) {
        TypedQuery<eu.daiad.web.domain.application.FavouriteGroup> query = entityManager.createQuery(
                        "SELECT f FROM favourite_group f "
                                        + "WHERE f.owner.key = :ownerKey and f.group.key = :groupKey",
                        eu.daiad.web.domain.application.FavouriteGroup.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("ownerKey", ownerKey);
        query.setParameter("groupKey", groupKey);

        return (!query.getResultList().isEmpty());
    }

    @Override
    public void insertFavouriteQuery(NamedDataQuery namedDataQuery, Account account) {

        try {

            DataQueryEntity dataQueryEntity = new DataQueryEntity();
            
            dataQueryEntity.setType(namedDataQuery.getType());
            dataQueryEntity.setName(namedDataQuery.getTitle());
            dataQueryEntity.setTags(namedDataQuery.getTags());
            dataQueryEntity.setQuery(new ObjectMapper().writeValueAsString(namedDataQuery.getQuery()));
            dataQueryEntity.setOwner(account);
            dataQueryEntity.setUpdatedOn(DateTime.now());
            
            this.entityManager.persist(dataQueryEntity);

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }    

    @Override
    public void updateFavouriteQuery(NamedDataQuery namedDataQuery, Account account) {

        //TODO - resolve when same favourite title already exists       
        try {             
            TypedQuery<eu.daiad.web.domain.application.DataQueryEntity> query = entityManager.createQuery(
                            "SELECT d FROM data_query d WHERE d.owner.id = :accountId and d.id = :id",
                            eu.daiad.web.domain.application.DataQueryEntity.class).setFirstResult(0).setMaxResults(1);
            
            query.setParameter("id", namedDataQuery.getId());
            query.setParameter("accountId", account.getId());

            DataQueryEntity dataQueryEntity = query.getSingleResult();
            
            dataQueryEntity.setName(namedDataQuery.getTitle());
            dataQueryEntity.setQuery(new ObjectMapper().writeValueAsString(namedDataQuery.getQuery()));
            dataQueryEntity.setTags(namedDataQuery.getTags());
        
            this.entityManager.persist(dataQueryEntity);

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }  
    
    @Override
    public void deleteFavouriteQuery(NamedDataQuery namedDataQuery, Account account) {
        
        try {             
            TypedQuery<eu.daiad.web.domain.application.DataQueryEntity> query = entityManager.createQuery(
                            "SELECT d FROM data_query d WHERE d.owner.id = :accountId and d.id = :id",
                            eu.daiad.web.domain.application.DataQueryEntity.class).setFirstResult(0).setMaxResults(1);
            
            query.setParameter("id", namedDataQuery.getId());
            query.setParameter("accountId", account.getId());

            DataQueryEntity dataQueryEntity = query.getSingleResult();
        
            this.entityManager.remove(dataQueryEntity);

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }      
    
    @Override
    public List<NamedDataQuery> getFavouriteQueriesForOwner(int accountId) 
            throws JsonMappingException, JsonParseException, IOException{
        
        List<NamedDataQuery> namedDataQueries = new ArrayList<>();
             
        TypedQuery<eu.daiad.web.domain.application.DataQueryEntity> query = entityManager.createQuery(
                        "SELECT d FROM data_query d WHERE d.owner.id = :accountId order by d.updatedOn desc",
                        eu.daiad.web.domain.application.DataQueryEntity.class);    
        query.setParameter("accountId", accountId);

        for(DataQueryEntity queryEntity : query.getResultList()){

            NamedDataQuery namedDataQuery = new NamedDataQuery();
            
            namedDataQuery.setId(queryEntity.getId());
            namedDataQuery.setType(queryEntity.getType());
            namedDataQuery.setTitle(queryEntity.getName());
            namedDataQuery.setTags(queryEntity.getTags());
            namedDataQuery.setQuery(queryEntity.toDataQuery());
            namedDataQuery.setCreatedOn(queryEntity.getUpdatedOn());

            namedDataQueries.add(namedDataQuery);
        }

        return namedDataQueries;
    }

    @Override
    public List<NamedDataQuery> getAllFavouriteQueries(){
        throw createApplicationException(SharedErrorCode.NOT_IMPLEMENTED);
        
//        TypedQuery<eu.daiad.web.domain.application.DataQueryEntity> query = entityManager.createQuery(
//                        "SELECT d FROM data_query d",
//                        eu.daiad.web.domain.application.DataQueryEntity.class);    
//        
//        List<NamedDataQuery> namedDataQueries = new ArrayList<>();
//        try {        
//            for(DataQueryEntity queryEntity : query.getResultList()){
//
//                    NamedDataQuery namedDataQuery = new NamedDataQuery();
//
//                    namedDataQuery.setId(queryEntity.getId());
//                    namedDataQuery.setType(queryEntity.getType());
//                    namedDataQuery.setTitle(queryEntity.getName());
//                    namedDataQuery.setTags(queryEntity.getTags());
//                    namedDataQuery.setQuery(queryEntity.toDataQuery());
//                    namedDataQuery.setCreatedOn(queryEntity.getUpdatedOn());
//
//                    namedDataQueries.add(namedDataQuery);
//
//            }
//        } catch (JsonMappingException ex) {
//            Logger.getLogger(JpaFavouriteRepository.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(JpaFavouriteRepository.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return namedDataQueries;
    }    
}
