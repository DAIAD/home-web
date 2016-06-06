package eu.daiad.web.model.query;

import java.util.ArrayList;

public class CustomSpatialFilter extends SpatialFilter {

    private ArrayList<LabeledGeometry> geometries = new ArrayList<LabeledGeometry>();

    public ArrayList<LabeledGeometry> getGeometries() {
        return geometries;
    }

    @Override
    public EnumSpatialFilterType getType() {
        return EnumSpatialFilterType.CUSTOM;
    }

}
