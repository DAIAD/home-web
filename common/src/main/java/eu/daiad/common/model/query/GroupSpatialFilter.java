package eu.daiad.common.model.query;

import java.util.UUID;

public class GroupSpatialFilter extends SpatialFilter {

    private UUID group;

    public UUID getGroup() {
        return group;
    }

    public void setGroup(UUID group) {
        this.group = group;
    }

    @Override
    public EnumSpatialFilterType getType() {
        return EnumSpatialFilterType.GROUP;
    }
}
