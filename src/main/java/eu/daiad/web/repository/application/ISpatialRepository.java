package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.AreaGroupEntity;
import eu.daiad.web.domain.application.AreaGroupMemberEntity;

/**
 * Provides methods for accessing application spatial data.
 */
public interface ISpatialRepository {

    /**
     * Gets the location of a user given her key.
     *
     * @param userKey the user key.
     * @return the user location or null if user is not found or the location is unknown.
     */
    Geometry getUserLocationByUserKey(UUID userKey);

    /**
     * Gets the default area for the user with the given key.
     *
     * @param utilityKey the utility key.
     * @param userKey the user key.
     * @return the user's default area.
     */
    AreaGroupMemberEntity getUserDefaultAreaByUserKey(UUID utilityKey, UUID userKey);

    /**
     * Returns all area groups for a utility given its key.
     *
     * @param utilityKey the utility key.
     * @return a list of {@link AreaGroupEntity} entities.
     */
    List<AreaGroupEntity> getAreaGroupsByUtilityId(UUID utilityKey);

    /**
     * Returns all areas for a utility given its key.
     *
     * @param utilityKey the utility key.
     * @return a list of {@link AreaGroupEntity} entities.
     */
    List<AreaGroupMemberEntity> getAreasByUtilityId(UUID utilityKey);

    /**
     * Returns all the areas for the given area group key.
     *
     * @param groupKey the area group key.
     * @return a list of {@link AreaGroupMemberEntity} entities.
     */
    List<AreaGroupMemberEntity> getAreasByAreaGroupKey(UUID groupKey);

    /**
     * Returns all the areas for the given area group key and level.
     *
     * @param groupKey the area group key.
     * @param level the level index.
     * @return a list of {@link AreaGroupMemberEntity} entities.
     */
    List<AreaGroupMemberEntity> getAreasByAreaGroupKeyAndLevel(UUID groupKey, int level);

    /**
     * Returns an area given its key.
     *
     * @param areaKey the area key.
     * @return the area or null if it does not exist.
     */
    AreaGroupMemberEntity getAreaByKey(UUID areaKey);

}
