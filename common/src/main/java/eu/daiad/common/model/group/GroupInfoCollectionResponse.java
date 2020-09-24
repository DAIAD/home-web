package eu.daiad.common.model.group;

import java.util.List;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class GroupInfoCollectionResponse extends RestResponse {

    private List<GroupInfo> groups;

    public GroupInfoCollectionResponse(List<GroupInfo> groups) {
        this.groups = groups;
    }

    public GroupInfoCollectionResponse(ErrorCode code, String description) {
        super(code, description);
    }

    public List<GroupInfo> getGroups() {
        return groups;
    }

}