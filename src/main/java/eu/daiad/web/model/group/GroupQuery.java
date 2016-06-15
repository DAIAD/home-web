package eu.daiad.web.model.group;

import com.vividsolutions.jts.geom.Geometry;

public class GroupQuery {

    private String name;

    private Geometry geometry;

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
