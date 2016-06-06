package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.UUID;

public class AreaSpatialFilter extends SpatialFilter {

    private ArrayList<UUID> areas = new ArrayList<UUID>();

    public ArrayList<UUID> getAreas() {
        return areas;
    }

    @Override
    public EnumSpatialFilterType getType() {
        return EnumSpatialFilterType.AREA;
    }
}
