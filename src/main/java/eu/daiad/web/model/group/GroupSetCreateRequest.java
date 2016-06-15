package eu.daiad.web.model.group;

import java.util.UUID;

public class GroupSetCreateRequest {

    private String title;

    private UUID[] members;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID[] getMembers() {
        return members;
    }

    public void setMembers(UUID[] members) {
        this.members = members;
    }

}
