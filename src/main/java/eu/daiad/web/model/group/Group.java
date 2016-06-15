package eu.daiad.web.model.group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

public abstract class Group {

    private UUID key;

    private UUID utilityKey;

    private String name;

    private long createdOn;

    private Geometry geometry;

    private Integer size;

    private List<UUID> members = new ArrayList<UUID>();

    private boolean favorite;

    public EnumGroupType getType() {
        return EnumGroupType.UNDEFINED;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public UUID getUtilityKey() {
        return utilityKey;
    }

    public void setUtilityKey(UUID utilityKey) {
        this.utilityKey = utilityKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void setMembers(List<UUID> members) {
        this.members = members;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

}
