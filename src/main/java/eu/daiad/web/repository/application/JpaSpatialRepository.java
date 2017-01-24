package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AreaGroupEntity;
import eu.daiad.web.domain.application.AreaGroupMemberEntity;
import eu.daiad.web.domain.application.DeviceMeterEntity;
import eu.daiad.web.repository.BaseRepository;

/**
 * Provides methods for accessing application spatial data.
 */
@Repository
public class JpaSpatialRepository extends BaseRepository implements ISpatialRepository {

    /**
     * Java Persistence entity manager.
     */
    @PersistenceContext(unitName = "default")
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
                                                              .setFirstResult(0)
                                                              .setMaxResults(1);

        accountQuery.setParameter("userKey", userKey);

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
     * Returns all area groups for a utility given its key.
     *
     * @param utilityKey the utility key.
     * @return a list of {@link AreaGroupEntity} entities.
     */
    @Override
    public List<AreaGroupEntity> getAreasGroupsByUtilityId(UUID utilityKey) {
        String groupQueryString = "select g from area_group g where g.utility.key = :utilityKey";

        TypedQuery<AreaGroupEntity> groupQuery = entityManager.createQuery(groupQueryString, AreaGroupEntity.class)
                                                              .setFirstResult(0);

        groupQuery.setParameter("utilityKey", utilityKey);

        return groupQuery.getResultList();
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

        areaQuery.setParameter("groupKey", areaKey);

        List<AreaGroupMemberEntity> areas = areaQuery.getResultList();

        if (!areas.isEmpty()) {
            return areas.get(0);
        }

        return null;
   }
}
