package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.AreaGroupMemberEntity;

public interface ISpatialRepository {

    abstract Geometry getUserLocationByUserKey(UUID userKey);

    abstract List<AreaGroupMemberEntity> getAreasByAreaGroupKey(UUID groupKey);

    abstract List<AreaGroupMemberEntity> getAllAreas();

    abstract List<AreaGroupMemberEntity> getAllAreasByUtilityId(int utilityId);

    abstract List<AreaGroupMemberEntity> getAllAreasByUtilityKey(UUID utilityKey);

    abstract AreaGroupMemberEntity getAreaByKey(UUID areaKey);

}
