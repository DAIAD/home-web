package eu.daiad.web.model.spatial;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.domain.application.AreaGroupMemberEntity;
import eu.daiad.web.model.RestResponse;

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
