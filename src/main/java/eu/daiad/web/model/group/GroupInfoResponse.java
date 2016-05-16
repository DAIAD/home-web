package eu.daiad.web.model.group;

import eu.daiad.web.model.RestResponse;

public class GroupInfoResponse extends RestResponse{
	
	private GroupInfo groupInfo;

	public GroupInfoResponse(GroupInfo groupInfo) {
		this.groupInfo = groupInfo;
	}

	public GroupInfoResponse(String code, String description) {
		super(code, description);
	}

	public GroupInfo getGroupInfo() {
		return groupInfo;
	}
}