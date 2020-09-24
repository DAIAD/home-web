package eu.daiad.common.model.group;

import java.util.UUID;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import eu.daiad.common.domain.application.AccountEntity;

public class GroupMember {

    private UUID key;

    private String username;

    private String fullName;

    private Geometry location;

    private DateTime createdOn;

    private boolean favourite;

    public GroupMember(AccountEntity account) {
        key = account.getKey();
        username = account.getUsername();
        fullName = account.getFullname();
        location = account.getLocation();
        createdOn = account.getCreatedOn();
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Geometry getLocation() {
        return location;
    }

    public void setLocation(Geometry location) {
        this.location = location;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

}
