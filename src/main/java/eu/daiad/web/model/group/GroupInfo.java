package eu.daiad.web.model.group;

import java.util.UUID;

import eu.daiad.web.domain.application.Group;
import eu.daiad.web.domain.application.GroupSegment;

public class GroupInfo {

    private UUID id;

    private String name;

    private int numberOfMembers;

    private long creationDateMils;

    private String country;

    public GroupInfo(Group group) {
        this.id = group.getKey();
        switch (group.getType()) {
            case SEGMENT:
                this.name = ((GroupSegment) group).getCluster().getName() + ": " + group.getName();
                break;
            default:
                this.name = group.getName();
                break;
        }
        this.numberOfMembers = group.getMembers().size();
        this.creationDateMils = group.getCreatedOn().getMillis();
        this.country = group.getUtility().getCountry();
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