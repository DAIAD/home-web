package eu.daiad.web.model.group;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class GroupMemberInfoResponse extends RestResponse {

    private List<GroupMemberInfo> members;

    public GroupMemberInfoResponse(List<GroupMemberInfo> members) {
        this.members = members;
    }

    public GroupMemberInfoResponse(String code, String description) {
        super(code, description);
    }

    public List<GroupMemberInfo> getMembers() {
        return members;
    }

}