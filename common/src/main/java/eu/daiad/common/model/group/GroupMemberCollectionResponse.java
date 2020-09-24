package eu.daiad.common.model.group;

import java.util.List;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class GroupMemberCollectionResponse extends RestResponse {

    private List<GroupMember> members;

    public GroupMemberCollectionResponse(List<GroupMember> members) {
        this.members = members;
    }

    public GroupMemberCollectionResponse(ErrorCode code, String description) {
        super(code, description);
    }

    public List<GroupMember> getMembers() {
        return members;
    }

}