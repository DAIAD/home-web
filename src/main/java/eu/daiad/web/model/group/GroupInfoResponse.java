package eu.daiad.web.model.group;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class GroupInfoResponse extends RestResponse{
	
	private List <GroupInfo> groupsInfo;

	public GroupInfoResponse(List <GroupInfo> groupsInfo) {
		this.groupsInfo = groupsInfo;
	}

	public GroupInfoResponse(String code, String description) {
		super(code, description);
	}

	public List <GroupInfo> getGroupsInfo() {
		return groupsInfo;
	}
}