package eu.daiad.common.model.group;

import org.locationtech.jts.geom.Geometry;

public class GroupQuery {

    private Integer size;

    private String name;

    private Geometry geometry;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

}
