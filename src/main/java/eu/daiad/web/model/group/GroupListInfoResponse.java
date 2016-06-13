package eu.daiad.web.model.group;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class GroupListInfoResponse extends RestResponse {

    private List<GroupInfo> groups;

    public GroupListInfoResponse(List<GroupInfo> groups) {
        this.groups = groups;
    }

    public GroupListInfoResponse(String code, String description) {
        super(code, description);
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

}