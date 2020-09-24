package eu.daiad.common.repository.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.common.domain.application.AccountEntity;
import eu.daiad.common.domain.application.DataQueryEntity;
import eu.daiad.common.domain.application.FavouriteAccountEntity;
import eu.daiad.common.domain.application.FavouriteEntity;
import eu.daiad.common.domain.application.FavouriteGroupEntity;
import eu.daiad.common.domain.application.GroupEntity;
import eu.daiad.common.domain.application.GroupSegmentEntity;
import eu.daiad.common.model.error.FavouriteErrorCode;
import eu.daiad.common.model.error.GroupErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.error.UserErrorCode;
import eu.daiad.common.model.favourite.EnumFavouriteType;
import eu.daiad.common.model.favourite.FavouriteInfo;
import eu.daiad.common.model.favourite.UpsertFavouriteRequest;
import eu.daiad.common.model.group.EnumGroupType;
import eu.daiad.common.model.query.NamedDataQuery;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.repository.BaseRepository;

@Repository
@Transactional
public class JpaFavouriteRepository extends BaseRepository implements IFavouriteRepository {

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<FavouriteInfo> getFavourites(UUID userKey) {
        String queryString = "SELECT f FROM favourite f WHERE f.owner.key = :userKey";

        List<FavouriteEntity> favouriteEntities  = entityManager.createQuery(queryString, FavouriteEntity.class)
                                                                .setParameter("userKey", userKey)
                                                                .getResultList();

        List<FavouriteInfo> favouritesInfo = new ArrayList<FavouriteInfo>();

        for (FavouriteEntity favouriteEntity : favouriteEntities) {
            FavouriteInfo favouriteInfo = new FavouriteInfo(favouriteEntity);
            favouritesInfo.add(favouriteInfo);
        }

        return favouritesInfo;
    }

    @Override
    public void upsertFavourite(UpsertFavouriteRequest favouriteInfo) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            // Retrieve admin's account
            TypedQuery<AccountEntity> adminAccountQuery = entityManager.createQuery(
                            "SELECT a FROM account a WHERE a.key = :key", AccountEntity.class).setFirstResult(0)
                            .setMaxResults(1);
            adminAccountQuery.setParameter("key", user.getKey());

            AccountEntity adminAccount = adminAccountQuery.getSingleResult();

            // Checking if the Favourite's type is invalid
            if (favouriteInfo.getType() != EnumFavouriteType.GROUP
                            && favouriteInfo.getType() != EnumFavouriteType.ACCOUNT) {
                throw createApplicationException(FavouriteErrorCode.INVALID_FAVOURITE_TYPE);
            }

            if (favouriteInfo.getType() == EnumFavouriteType.ACCOUNT) {
                // checking if the Account exists at all
                AccountEntity account = null;
                try {
                    TypedQuery<AccountEntity> accountQuery = entityManager.createQuery(
                                    "SELECT a FROM account a WHERE a.key = :key", AccountEntity.class).setFirstResult(0)
                                    .setMaxResults(1);
                    accountQuery.setParameter("key", favouriteInfo.getKey());

                    account = accountQuery.getSingleResult();
                } catch (NoResultException ex) {
                    throw wrapApplicationException(ex, UserErrorCode.USER_KEY_NOT_FOUND).set("key", favouriteInfo.getKey());
                }

                // Checking if the selected account belongs to the same utility
                // as administrator
                if (account.getUtility().getId() != adminAccount.getUtility().getId()) {
                    throw createApplicationException(UserErrorCode.ACCOUNT_ACCESS_RESTRICTED);
                }

                // Checking if the account is already an admin's favourite
                TypedQuery<FavouriteEntity> favouriteQuery = entityManager.createQuery(
                                "SELECT f FROM favourite f WHERE f.owner = :owner", FavouriteEntity.class).setFirstResult(0);
                favouriteQuery.setParameter("owner", adminAccount);

                List<FavouriteEntity> favourites = favouriteQuery.getResultList();

                FavouriteAccountEntity selectedFavourite = null;
                for (FavouriteEntity favourite : favourites) {
                    if (favourite.getType() == EnumFavouriteType.ACCOUNT) {
                        FavouriteAccountEntity favouriteAccount = (FavouriteAccountEntity) favourite;
                        if (favouriteAccount.getAccount().getId() == account.getId()) {
                            selectedFavourite = favouriteAccount;
                        }
                    }
                }

                // Favourite already exists just set the label
                if (selectedFavourite != null) {
                    selectedFavourite.setLabel(favouriteInfo.getLabel());

                } else {
                    selectedFavourite = new FavouriteAccountEntity();
                    selectedFavourite.setLabel(favouriteInfo.getLabel());
                    selectedFavourite.setOwner(adminAccount);
                    selectedFavourite.setCreatedOn(new DateTime());
                    selectedFavourite.setAccount(account);
                }

                entityManager.persist(selectedFavourite);
            } else {
                // checking if the Group exists at all
                GroupEntity group = null;
                try {
                    TypedQuery<GroupEntity> groupQuery = entityManager.createQuery(
                                    "SELECT g FROM group g WHERE g.key = :key", GroupEntity.class).setFirstResult(0)
                                    .setMaxResults(1);
                    groupQuery.setParameter("key", favouriteInfo.getKey());

                    group = groupQuery.getSingleResult();
                } catch (NoResultException ex) {
                    throw wrapApplicationException(ex, GroupErrorCode.GROUP_DOES_NOT_EXIST)
                    	.set("groupId", favouriteInfo.getKey());
                }

                // Checking if the selected group belongs to the same utility as
                // admin
                if (group.getUtility().getId() != adminAccount.getUtility().getId()) {
                    throw createApplicationException(GroupErrorCode.GROUP_ACCESS_RESTRICTED);
                }

                // Checking if the group is already an admin's favourite
                TypedQuery<FavouriteEntity> favouriteQuery = entityManager.createQuery(
                                "SELECT f FROM favourite f WHERE f.owner = :owner", FavouriteEntity.class).setFirstResult(0);
                favouriteQuery.setParameter("owner", adminAccount);

                List<FavouriteEntity> favourites = favouriteQuery.getResultList();

                FavouriteGroupEntity selectedFavourite = null;
                for (FavouriteEntity favourite : favourites) {
                    if (favourite.getType() == EnumFavouriteType.GROUP) {
                        FavouriteGroupEntity favouriteGroup = (FavouriteGroupEntity) favourite;
                        if (favouriteGroup.getGroup().getId() == group.getId()) {
                            selectedFavourite = favouriteGroup;
                        }
                    }
                }

                // Favourite already exists just set the label
                if (selectedFavourite != null) {
                    selectedFavourite.setLabel(favouriteInfo.getLabel());

                } else {
                    selectedFavourite = new FavouriteGroupEntity();
                    selectedFavourite.setLabel(favouriteInfo.getLabel());
                    selectedFavourite.setOwner(adminAccount);
                    selectedFavourite.setCreatedOn(new DateTime());
                    selectedFavourite.setGroup(group);
                }

                entityManager.persist(selectedFavourite);
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

            if (!user.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }

            FavouriteEntity favourite = null;
            // Check if favourite exists
            try {
                TypedQuery<FavouriteEntity> favouriteQuery = entityManager.createQuery(
                                "select f from favourite f where f.key = :favourite_id", FavouriteEntity.class)
                                .setFirstResult(0).setMaxResults(1);
                favouriteQuery.setParameter("favourite_id", favourite_id);
                favourite = favouriteQuery.getSingleResult();

                // Check that admin is the owner of the group
                // Get admin's account
                TypedQuery<eu.daiad.common.domain.application.AccountEntity> adminAccountQuery = entityManager.createQuery(
                                "select a from account a where a.id = :adminId",
                                eu.daiad.common.domain.application.AccountEntity.class).setFirstResult(0).setMaxResults(1);
                adminAccountQuery.setParameter("adminId", user.getId());
                AccountEntity adminAccount = adminAccountQuery.getSingleResult();

                if (favourite.getOwner() == adminAccount) {
                    entityManager.remove(favourite);

                } else {
                    throw createApplicationException(FavouriteErrorCode.FAVOURITE_ACCESS_RESTRICTED)
                    	.set("favouriteId", favourite_id);
                }

            } catch (NoResultException ex) {
                throw wrapApplicationException(ex, FavouriteErrorCode.FAVOURITE_DOES_NOT_EXIST)
                	.set("favouriteId", favourite_id);
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

        TypedQuery<eu.daiad.common.domain.application.AccountEntity> query = entityManager.createQuery(
                        "select a from account a where a.key = :key", eu.daiad.common.domain.application.AccountEntity.class);

        query.setParameter("key", ownerKey);

        eu.daiad.common.domain.application.AccountEntity owner = query.getSingleResult();

        query.setParameter("key", userKey);

        eu.daiad.common.domain.application.AccountEntity account = query.getSingleResult();

        eu.daiad.common.domain.application.FavouriteAccountEntity favorite = new eu.daiad.common.domain.application.FavouriteAccountEntity();
        favorite.setAccount(account);
        favorite.setCreatedOn(new DateTime());
        favorite.setOwner(owner);
        favorite.setLabel(account.getFullname());

        entityManager.persist(favorite);
    }

    @Override
    public void deleteUserFavorite(UUID ownerKey, UUID userKey) {
        TypedQuery<eu.daiad.common.domain.application.FavouriteAccountEntity> query = entityManager.createQuery(
                        "SELECT f FROM favourite_account f "
                                        + "WHERE f.owner.key = :ownerKey and f.account.key = :userKey",
                        eu.daiad.common.domain.application.FavouriteAccountEntity.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("ownerKey", ownerKey);
        query.setParameter("userKey", userKey);

        List<FavouriteAccountEntity> favorites = query.getResultList();

        if (!favorites.isEmpty()) {
            entityManager.remove(favorites.get(0));
        }
    }

    @Override
    public boolean isUserFavorite(UUID ownerKey, UUID userKey) {
        TypedQuery<eu.daiad.common.domain.application.FavouriteAccountEntity> query = entityManager.createQuery(
                        "SELECT f FROM favourite_account f "
                                        + "WHERE f.owner.key = :ownerKey and f.account.key = :userKey",
                        eu.daiad.common.domain.application.FavouriteAccountEntity.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("ownerKey", ownerKey);
        query.setParameter("userKey", userKey);

        return (!query.getResultList().isEmpty());
    }

    @Override
    public void addGroupFavorite(UUID ownerKey, UUID groupKey) {
        if (isGroupFavorite(ownerKey, groupKey)) {
            return;
        }

        TypedQuery<eu.daiad.common.domain.application.AccountEntity> accountQuery = entityManager.createQuery(
                        "select a from account a where a.key = :key", eu.daiad.common.domain.application.AccountEntity.class);

        accountQuery.setParameter("key", ownerKey);

        eu.daiad.common.domain.application.AccountEntity owner = accountQuery.getSingleResult();

        TypedQuery<eu.daiad.common.domain.application.GroupEntity> groupQuery = entityManager.createQuery(
                        "select g from group g where g.key = :key", eu.daiad.common.domain.application.GroupEntity.class);

        groupQuery.setParameter("key", groupKey);

        eu.daiad.common.domain.application.GroupEntity group = groupQuery.getSingleResult();

        eu.daiad.common.domain.application.FavouriteGroupEntity favorite = new eu.daiad.common.domain.application.FavouriteGroupEntity();
        favorite.setGroup(group);
        favorite.setCreatedOn(new DateTime());
        favorite.setOwner(owner);
        if (group.getType() == EnumGroupType.SEGMENT) {
            favorite.setLabel(((GroupSegmentEntity) group).getCluster().getName() + " - " + group.getName());
        } else {
            favorite.setLabel(group.getName());
        }

        entityManager.persist(favorite);
    }

    @Override
    public void deleteGroupFavorite(UUID ownerKey, UUID groupKey) {
        TypedQuery<eu.daiad.common.domain.application.FavouriteGroupEntity> query = entityManager.createQuery(
                        "SELECT f FROM favourite_group f "
                                        + "WHERE f.owner.key = :ownerKey and f.group.key = :groupKey",
                        eu.daiad.common.domain.application.FavouriteGroupEntity.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("ownerKey", ownerKey);
        query.setParameter("groupKey", groupKey);

        List<FavouriteGroupEntity> favorites = query.getResultList();

        if (!favorites.isEmpty()) {
            entityManager.remove(favorites.get(0));
        }
    }

    @Override
    public boolean isGroupFavorite(UUID ownerKey, UUID groupKey) {
        TypedQuery<eu.daiad.common.domain.application.FavouriteGroupEntity> query = entityManager.createQuery(
                        "SELECT f FROM favourite_group f "
                                        + "WHERE f.owner.key = :ownerKey and f.group.key = :groupKey",
                        eu.daiad.common.domain.application.FavouriteGroupEntity.class).setFirstResult(0).setMaxResults(1);

        query.setParameter("ownerKey", ownerKey);
        query.setParameter("groupKey", groupKey);

        return (!query.getResultList().isEmpty());
    }

    @Override
    public void insertFavouriteQuery(NamedDataQuery namedDataQuery, AccountEntity account) {
        try {
            TypedQuery<eu.daiad.common.domain.application.DataQueryEntity> queryCheck = entityManager.createQuery(
                            "SELECT d FROM data_query d WHERE d.owner.id = :accountId and d.name = :name",
                            eu.daiad.common.domain.application.DataQueryEntity.class).setFirstResult(0).setMaxResults(1);

            queryCheck.setParameter("accountId", account.getId());
            queryCheck.setParameter("name", namedDataQuery.getTitle());

            List<DataQueryEntity> duplicate = queryCheck.getResultList();

            String finalTitle;
            if(duplicate.size() > 0){
                finalTitle = namedDataQuery.getTitle() + " (duplicate title) " + duplicate.get(0).getId();
                
                TypedQuery<eu.daiad.common.domain.application.DataQueryEntity> queryUnfortunateCheck = entityManager.createQuery(
                                "SELECT d FROM data_query d WHERE d.owner.id = :accountId and d.name = :name",
                                eu.daiad.common.domain.application.DataQueryEntity.class).setFirstResult(0).setMaxResults(1);

                queryUnfortunateCheck.setParameter("accountId", account.getId());
                queryUnfortunateCheck.setParameter("name", finalTitle);
                List<DataQueryEntity> unfortunateDuplicate = queryUnfortunateCheck.getResultList();
                if(unfortunateDuplicate.size()>0){
                        int randomNum = ThreadLocalRandom.current().nextInt(1, 100);
                        finalTitle = finalTitle + " (duplicate) " + unfortunateDuplicate.get(0).getId()+ " " + randomNum;
                }
            
            } else {
                finalTitle = namedDataQuery.getTitle();
            }

            DataQueryEntity dataQueryEntity = new DataQueryEntity();

            dataQueryEntity.setType(namedDataQuery.getType());
            dataQueryEntity.setName(finalTitle);
            dataQueryEntity.setTags(namedDataQuery.getTags());
            dataQueryEntity.setReportName(namedDataQuery.getReportName());
            dataQueryEntity.setLevel(namedDataQuery.getLevel());
            dataQueryEntity.setField(namedDataQuery.getField());
            dataQueryEntity.setOverlap(namedDataQuery.getOverlap());
            dataQueryEntity.setQuery(objectMapper.writeValueAsString(namedDataQuery.getQueries()));
            dataQueryEntity.setOwner(account);
            dataQueryEntity.setUpdatedOn(DateTime.now());

            entityManager.persist(dataQueryEntity);

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void updateFavouriteQuery(NamedDataQuery namedDataQuery, AccountEntity account) {
        try {
            TypedQuery<eu.daiad.common.domain.application.DataQueryEntity> queryCheck = entityManager.createQuery(
                            "SELECT d FROM data_query d WHERE d.owner.id = :accountId and d.name = :name",
                            eu.daiad.common.domain.application.DataQueryEntity.class).setFirstResult(0).setMaxResults(1);

            queryCheck.setParameter("accountId", account.getId());
            queryCheck.setParameter("name", namedDataQuery.getTitle());

            List<DataQueryEntity> duplicate = queryCheck.getResultList();

            String finalTitle;
            if(duplicate.size() > 0){
                if(duplicate.get(0).getId() == namedDataQuery.getId()){
                    finalTitle = namedDataQuery.getTitle();
                } else {
                    finalTitle = namedDataQuery.getTitle() + " (duplicate title) "+ duplicate.get(0).getId();
                }
                
            } else {
                finalTitle = namedDataQuery.getTitle();
            }

            TypedQuery<eu.daiad.common.domain.application.DataQueryEntity> query = entityManager.createQuery(
                            "SELECT d FROM data_query d WHERE d.owner.id = :accountId and d.id = :id",
                            eu.daiad.common.domain.application.DataQueryEntity.class).setFirstResult(0).setMaxResults(1);

            query.setParameter("id", namedDataQuery.getId());
            query.setParameter("accountId", account.getId());

            DataQueryEntity dataQueryEntity = query.getSingleResult();

            dataQueryEntity.setName(finalTitle);
            dataQueryEntity.setQuery(objectMapper.writeValueAsString(namedDataQuery.getQueries()));
            dataQueryEntity.setTags(namedDataQuery.getTags());
            dataQueryEntity.setReportName(namedDataQuery.getReportName());
            dataQueryEntity.setLevel(namedDataQuery.getLevel());
            dataQueryEntity.setField(namedDataQuery.getField());
            dataQueryEntity.setOverlap(namedDataQuery.getOverlap());

            entityManager.persist(dataQueryEntity);

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void deleteFavouriteQuery(NamedDataQuery namedDataQuery, AccountEntity account) {

        try {
            TypedQuery<eu.daiad.common.domain.application.DataQueryEntity> query = entityManager.createQuery(
                            "SELECT d FROM data_query d WHERE d.owner.id = :accountId and d.id = :id",
                            eu.daiad.common.domain.application.DataQueryEntity.class).setFirstResult(0).setMaxResults(1);

            query.setParameter("id", namedDataQuery.getId());
            query.setParameter("accountId", account.getId());

            DataQueryEntity dataQueryEntity = query.getSingleResult();

            entityManager.remove(dataQueryEntity);

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void pinFavouriteQuery(long id, AccountEntity account) {

        try {
            TypedQuery<eu.daiad.common.domain.application.DataQueryEntity> query = entityManager.createQuery(
                            "SELECT d FROM data_query d WHERE d.owner.id = :accountId and d.id = :id",
                            eu.daiad.common.domain.application.DataQueryEntity.class).setFirstResult(0).setMaxResults(1);

            query.setParameter("id", id);
            query.setParameter("accountId", account.getId());

            DataQueryEntity dataQueryEntity = query.getSingleResult();
            dataQueryEntity.setPinned(true);

            entityManager.persist(dataQueryEntity);

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void unpinFavouriteQuery(long id, AccountEntity account) {

        try {
            TypedQuery<eu.daiad.common.domain.application.DataQueryEntity> query = entityManager.createQuery(
                            "SELECT d FROM data_query d WHERE d.owner.id = :accountId and d.id = :id",
                            eu.daiad.common.domain.application.DataQueryEntity.class).setFirstResult(0).setMaxResults(1);

            query.setParameter("id", id);
            query.setParameter("accountId", account.getId());

            List<DataQueryEntity> dataQueryEntities = query.getResultList();

            if(!dataQueryEntities.isEmpty()){
                DataQueryEntity dataQueryEntity = dataQueryEntities.get(0);
                dataQueryEntity.setPinned(false);
                entityManager.persist(dataQueryEntity);
            }

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public List<NamedDataQuery> getFavouriteQueriesForOwner(int accountId)
            throws JsonMappingException, JsonParseException, IOException{

        List<NamedDataQuery> namedDataQueries = new ArrayList<>();

        TypedQuery<eu.daiad.common.domain.application.DataQueryEntity> query = entityManager.createQuery(
                        "SELECT d FROM data_query d WHERE d.owner.id = :accountId order by d.updatedOn desc",
                        eu.daiad.common.domain.application.DataQueryEntity.class);
        query.setParameter("accountId", accountId);

        for(DataQueryEntity queryEntity : query.getResultList()){

            NamedDataQuery namedDataQuery = new NamedDataQuery();

            namedDataQuery.setId(queryEntity.getId());
            namedDataQuery.setType(queryEntity.getType());
            namedDataQuery.setTitle(queryEntity.getName());
            namedDataQuery.setTags(queryEntity.getTags());
            namedDataQuery.setReportName(queryEntity.getReportName());
            namedDataQuery.setLevel(queryEntity.getLevel());
            namedDataQuery.setField(queryEntity.getField());
            namedDataQuery.setOverlap(queryEntity.getOverlap());
            namedDataQuery.setPinned(queryEntity.isPinned());
            namedDataQuery.setQueries(queryEntity.toDataQuery());
            namedDataQuery.setCreatedOn(queryEntity.getUpdatedOn());

            namedDataQueries.add(namedDataQuery);
        }

        return namedDataQueries;
    }

    @Override
    public List<NamedDataQuery> getAllFavouriteQueries(){
        throw createApplicationException(SharedErrorCode.NOT_IMPLEMENTED);

//        TypedQuery<eu.daiad.common.domain.application.DataQueryEntity> query = entityManager.createQuery(
//                        "SELECT d FROM data_query d",
//                        eu.daiad.common.domain.application.DataQueryEntity.class);
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
