package eu.daiad.web.model.spatial;

import java.util.UUID;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import eu.daiad.web.domain.application.AreaGroupEntity;

public class AreaGroup {

    private UUID key;

    private String title;

    private DateTime createdOn;

    private Geometry boundingBox;

    private int levelCount;

    public AreaGroup(AreaGroupEntity entity) {
        key = entity.getKey();
        title = entity.getTitle();
        createdOn = entity.getCreatedOn();
        boundingBox = entity.getBoundingBox();
        levelCount = entity.getLevelCount();
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Geometry getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(Geometry boundingBox) {
        this.boundingBox = boundingBox;
    }

    public int getLevelCount() {
        return levelCount;
    }

    public void setLevelCount(int levelCount) {
        this.levelCount = levelCount;
    }

}
