package eu.daiad.web.model.group;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class GroupMemberCollectionResponse extends RestResponse {

    private List<GroupMember> members;

    public GroupMemberCollectionResponse(List<GroupMember> members) {
        this.members = members;
    }

    public GroupMemberCollectionResponse(String code, String description) {
        super(code, description);
    }

    public List<GroupMember> getMembers() {
        return members;
    }

}