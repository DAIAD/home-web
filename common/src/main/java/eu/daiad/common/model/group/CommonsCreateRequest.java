package eu.daiad.common.model.group;


import org.locationtech.jts.geom.Geometry;

import eu.daiad.common.model.AuthenticatedRequest;

public class CommonsCreateRequest extends AuthenticatedRequest {

    private String name;

    private String description;

    private Geometry geometry;

    private byte[] image;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

}
