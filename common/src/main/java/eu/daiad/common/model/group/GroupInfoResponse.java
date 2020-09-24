package eu.daiad.common.model.group;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class GroupInfoResponse extends RestResponse{
	
	private GroupInfo groupInfo;

	public GroupInfoResponse(GroupInfo groupInfo) {
		this.groupInfo = groupInfo;
	}

	public GroupInfoResponse(ErrorCode code, String description) {
		super(code, description);
	}

	public GroupInfo getGroupInfo() {
		return groupInfo;
	}
}