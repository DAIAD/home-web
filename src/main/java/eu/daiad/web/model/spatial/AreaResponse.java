package eu.daiad.web.model.spatial;

import eu.daiad.web.domain.application.AreaGroupMemberEntity;
import eu.daiad.web.model.RestResponse;

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
