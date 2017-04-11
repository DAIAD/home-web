package eu.daiad.web.model.group;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class GroupInfoCollectionResponse extends RestResponse {

    private List<GroupInfo> groups;

    public GroupInfoCollectionResponse(List<GroupInfo> groups) {
        this.groups = groups;
    }

    public GroupInfoCollectionResponse(String code, String description) {
        super(code, description);
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

}