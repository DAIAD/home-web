package eu.daiad.common.model.group;

import java.util.List;

import eu.daiad.common.model.RestResponse;

public class GroupQueryResponse extends RestResponse {

    private List<Group> groups;

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

}
