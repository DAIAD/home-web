package eu.daiad.web.model.query;

import com.vividsolutions.jts.geom.Geometry;

public class LabeledGeometry {

    private String label;

    private Geometry geometry;

    public LabeledGeometry() {

    }

    public LabeledGeometry(String label, Geometry geometry) {
        this.label = label;
        this.geometry = geometry;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public boolean contains(Geometry g) {
        return this.geometry.contains(g);
    }

}
