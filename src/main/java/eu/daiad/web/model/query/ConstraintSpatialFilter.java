package eu.daiad.web.model.query;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ConstraintSpatialFilter extends SpatialFilter {

    @JsonDeserialize(using = EnumSpatialFilterOperation.Deserializer.class)
    private EnumSpatialFilterOperation operation;

    private Geometry geometry;

    private Double distance;

    public EnumSpatialFilterOperation getOperation() {
        return operation;
    }

    public void setOperation(EnumSpatialFilterOperation operation) {
        this.operation = operation;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Double getDistance() {
        if (this.operation == EnumSpatialFilterOperation.DISTANCE) {
            return null;
        }
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Override
    public EnumSpatialFilterType getType() {
        return EnumSpatialFilterType.CONSTRAINT;
    }

}
