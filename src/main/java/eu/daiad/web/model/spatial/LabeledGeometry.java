package eu.daiad.web.model.spatial;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.AreaGroupMemberEntity;

public class LabeledGeometry {

    @JsonIgnore
    private UUID key;

    private String label;

    private Geometry geometry;

    public LabeledGeometry() {

    }

    public LabeledGeometry(AreaGroupMemberEntity area) {
        key = area.getKey();
        label = area.getTitle();
        geometry = area.getGeometry();
    }

    public LabeledGeometry(UUID key, String label, Geometry geometry) {
        this.key = key;
        this.label = label;
        this.geometry = geometry;
    }

    public UUID getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public boolean contains(Geometry g) {
        if ((geometry == null) || (g == null)) {
            return false;
        }
        return geometry.contains(g);
    }
}
