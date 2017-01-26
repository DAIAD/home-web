package eu.daiad.web.model.group;

import java.util.UUID;

import eu.daiad.web.domain.application.GroupEntity;
import eu.daiad.web.domain.application.GroupSegmentEntity;

public class GroupInfo {

    private UUID id;

    private String name;

    private int numberOfMembers;

    private long creationDateMils;

    private String country;

    public GroupInfo(GroupEntity group) {
        id = group.getKey();
        switch (group.getType()) {
            case SEGMENT:
                name = ((GroupSegmentEntity) group).getCluster().getName() + ": " + group.getName();
                break;
            default:
                name = group.getName();
                break;
        }
        numberOfMembers = group.getSize();
        creationDateMils = group.getCreatedOn().getMillis();
        country = group.getUtility().getCountry();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfMembers() {
        return numberOfMembers;
    }

    public long getCreationDateMils() {
        return creationDateMils;
    }

    public String getCountry() {
        return country;
    }

}