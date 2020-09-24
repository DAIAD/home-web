package eu.daiad.common.model.spatial;

import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.daiad.common.domain.application.AreaGroupMemberEntity;

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
