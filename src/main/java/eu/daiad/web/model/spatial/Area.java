package eu.daiad.web.model.spatial;

import java.util.UUID;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import eu.daiad.web.domain.application.AreaGroupMemberEntity;

public class Area {

    private UUID key;

    private UUID groupKey;

    private String title;

    private DateTime createdOn;

    private Geometry geometry;

    private int levelIndex;

    public Area(AreaGroupMemberEntity entity) {
        key = entity.getKey();
        if (entity.getGroup() != null) {
            groupKey = entity.getGroup().getKey();
        }
        title = entity.getTitle();
        createdOn = entity.getCreatedOn();
        geometry = entity.getGeometry();
        levelIndex = entity.getLevelIndex();
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public UUID getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(UUID groupKey) {
        this.groupKey = groupKey;
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

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public int getLevelIndex() {
        return levelIndex;
    }

    public void setLevelIndex(int levelIndex) {
        this.levelIndex = levelIndex;
    }

}
