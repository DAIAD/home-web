package eu.daiad.common.repository.application;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import eu.daiad.common.domain.application.AccountEntity;
import eu.daiad.common.domain.application.AreaGroupEntity;
import eu.daiad.common.domain.application.AreaGroupMemberEntity;
import eu.daiad.common.domain.application.DeviceMeterEntity;
import eu.daiad.common.repository.BaseRepository;

/**
 * Provides methods for accessing application spatial data.
 */
@Repository
public class JpaSpatialRepository extends BaseRepository implements ISpatialRepository {

    @Value("${daiad.spatial.neighbourhood.group}")
    private String defaultAreaGroup;

    /**
     * Java Persistence entity manager.
     */
    @PersistenceContext
    EntityManager entityManager;

    /**
     * Gets the location of a user given her key.
     *
     * @param userKey the user key.
     * @return the user location or null if user is not found or the location is unknown.
     */
    @Override
    public Geometry getUserLocationByUserKey(UUID userKey) {
        AccountEntity account = null;

        // Get account
        String accountQueryString = "select a from account a where a.key = :userKey";

        TypedQuery<AccountEntity> accountQuery = entityManager.createQuery(accountQueryString, AccountEntity.class)
                                                              .setParameter("userKey", userKey)
                                                              .setFirstResult(0)
                                                              .setMaxResults(1);

        List<AccountEntity> accounts = accountQuery.getResultList();

        if (accounts.isEmpty()) {
            return null;
        }

        // If account location is set, return it
        account = accounts.get(0);
        if (account.getLocation() != null) {
            return account.getLocation();
        }

        // Attempt to find user location from smart water meter registrations.
        // If only one registration exists, the location of the meter is
        // returned.
        String meterQueryString = "select d from device_meter d where d.account.key = :userKey";

        TypedQuery<DeviceMeterEntity> meterQuery = entityManager.createQuery(meterQueryString, DeviceMeterEntity.class)
                                                                .setFirstResult(0);

        meterQuery.setParameter("userKey", userKey);

        List<DeviceMeterEntity> meters = meterQuery.getResultList();

        if ((!meters.isEmpty()) && (meters.get(0).getLocation() != null)) {
            return meters.get(0).getLocation();
        }

        return null;
    }

    /**
     * Gets the default area for the user with the given key.
     *
     * @param utilityKey the utility key.
     * @param userKey the user key.
     * @return the user's default area.
     */
    @Override
    public AreaGroupMemberEntity getUserDefaultAreaByUserKey(UUID utilityKey, UUID userKey) {
        if (StringUtils.isBlank(defaultAreaGroup)) {
            return null;
        }

        Geometry userLocation = getUserLocationByUserKey(userKey);
        if (userLocation == null) {
            return null;
        }
        if (userLocation.getSRID() == 0) {
            userLocation.setSRID(4326);
        }

        String queryString = "select    a from area_group_item a " +
                             "where     a.group.key = :groupKey and a.utility.key = :utilityKey and " +
                             "          contains(a.geometry, :userLocation) = true";

        TypedQuery<AreaGroupMemberEntity> areaQuery = entityManager.createQuery(queryString, AreaGroupMemberEntity.class)
                                                                   .setParameter("groupKey", UUID.fromString(defaultAreaGroup))
                                                                   .setParameter("utilityKey", utilityKey)
                                                                   .setParameter("userLocation", userLocation);

        List<AreaGroupMemberEntity> areas = areaQuery.getResultList();
        if(areas.isEmpty()) {
            return null;
        }

        return areas.get(0);
    }

    /**
     * Returns all area groups for a utility given its key.
     *
     * @param utilityKey the utility key.
     * @return a list of {@link AreaGroupEntity} entities.
     */
    @Override
    public List<AreaGroupEntity> getAreaGroupsByUtilityId(UUID utilityKey) {
        String groupQueryString = "select g from area_group g where g.utility.key = :utilityKey";

        TypedQuery<AreaGroupEntity> groupQuery = entityManager.createQuery(groupQueryString, AreaGroupEntity.class)
                                                              .setFirstResult(0);

        groupQuery.setParameter("utilityKey", utilityKey);

        return groupQuery.getResultList();
    }

    /**
     * Returns all areas for a utility given its key.
     *
     * @param utilityKey the utility key.
     * @return a list of {@link AreaGroupEntity} entities.
     */
    @Override
    public List<AreaGroupMemberEntity> getAreasByUtilityId(UUID utilityKey) {
        String areaQueryString = "select a from area_group_item a where a.utility.key = :utilityKey";

        TypedQuery<AreaGroupMemberEntity> areaQuery = entityManager.createQuery(areaQueryString, AreaGroupMemberEntity.class)
                                                                   .setParameter("utilityKey", utilityKey);
        return areaQuery.getResultList();
    }

    /**
     * Returns all the areas for the given area group key.
     *
     * @param groupKey the area group key.
     * @return a list of {@link AreaGroupMemberEntity} entities.
     */
    @Override
    public List<AreaGroupMemberEntity> getAreasByAreaGroupKey(UUID groupKey) {
        String areaQueryString = "select a from area_group_item a where a.group.key = :groupKey";

        TypedQuery<AreaGroupMemberEntity> areaQuery = entityManager.createQuery(areaQueryString, AreaGroupMemberEntity.class)
                                                                   .setFirstResult(0);

        areaQuery.setParameter("groupKey", groupKey);

        return areaQuery.getResultList();
    }

    /**
     * Returns all the areas for the given area group key and level.
     *
     * @param groupKey the area group key.
     * @param level the level index.
     * @return a list of {@link AreaGroupMemberEntity} entities.
     */
    @Override
    public List<AreaGroupMemberEntity> getAreasByAreaGroupKeyAndLevel(UUID groupKey, int level) {
        String areaQueryString = "select a from area_group_item a where a.group.key = :groupKey and a.levelIndex = :levelIndex";

        TypedQuery<AreaGroupMemberEntity> areaQuery = entityManager.createQuery(areaQueryString, AreaGroupMemberEntity.class)
                                                                   .setParameter("groupKey", groupKey)
                                                                   .setParameter("levelIndex", level);

        return areaQuery.getResultList();
    }

    /**
     * Returns an area given its key.
     *
     * @param areaKey the area key.
     * @return the area or null if it does not exist.
     */
    @Override
    public AreaGroupMemberEntity getAreaByKey(UUID areaKey) {
        String areaQueryString = "select a from area_group_item a where a.key = :areaKey";

        TypedQuery<AreaGroupMemberEntity> areaQuery = entityManager.createQuery(areaQueryString, AreaGroupMemberEntity.class)
                                                                   .setFirstResult(0)
                                                                   .setMaxResults(1);

        areaQuery.setParameter("areaKey", areaKey);

        List<AreaGroupMemberEntity> areas = areaQuery.getResultList();

        if (!areas.isEmpty()) {
            return areas.get(0);
        }

        return null;
   }

    /**
     * Returns all meters for a utility given its key.
     *
     * @param utilityKey the utility key.
     * @return a list of {@link DeviceMeterEntity} entities.
     */
    @Override
    public List<DeviceMeterEntity> getAllMetersByUtilityId(UUID utilityKey) {
        String meterQueryString = "select d from device_meter d where d.account.utility.key = :utilityKey";

        return entityManager.createQuery(meterQueryString, DeviceMeterEntity.class)
                     .setParameter("utilityKey", utilityKey)
                     .getResultList();

    }
}
