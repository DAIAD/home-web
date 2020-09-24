package eu.daiad.common.model.spatial;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.domain.application.AreaGroupMemberEntity;
import eu.daiad.common.model.RestResponse;

public class AreaCollectionResponse extends RestResponse {

    private List<Area> areas = new ArrayList<Area>();

    public AreaCollectionResponse(List<AreaGroupMemberEntity> entities) {
        if (entities != null) {
            for (AreaGroupMemberEntity entity : entities) {
                areas.add(new Area(entity));
            }
        }
    }

    public List<Area> getAreas() {
        return areas;
    }

}
