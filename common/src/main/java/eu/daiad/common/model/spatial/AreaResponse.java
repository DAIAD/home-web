package eu.daiad.common.model.spatial;

import eu.daiad.common.domain.application.AreaGroupMemberEntity;
import eu.daiad.common.model.RestResponse;

public class AreaResponse extends RestResponse {

    private Area area;

    public AreaResponse(AreaGroupMemberEntity entity) {
        if (entity != null) {
            area = new Area(entity);
        }
    }

    public Area getArea() {
        return area;
    }

}
