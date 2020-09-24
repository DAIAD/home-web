package eu.daiad.common.model.spatial;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.domain.application.AreaGroupEntity;
import eu.daiad.common.model.RestResponse;

public class AreaGroupCollectionResponse extends RestResponse {

    private List<AreaGroup> groups = new ArrayList<AreaGroup>();

    public AreaGroupCollectionResponse(List<AreaGroupEntity> entities) {
        if (entities != null) {
            for (AreaGroupEntity entity : entities) {
                groups.add(new AreaGroup(entity));
            }
        }
    }

    public List<AreaGroup> getGroups() {
        return groups;
    }

}
