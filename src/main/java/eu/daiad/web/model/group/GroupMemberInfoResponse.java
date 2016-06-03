package eu.daiad.web.model.group;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class GroupMemberInfoResponse extends RestResponse{
	
	private List <GroupMemberInfo> groupMembersInfo;

	public GroupMemberInfoResponse(List <GroupMemberInfo> groupMembersInfo) {
		this.groupMembersInfo = groupMembersInfo;
	}

	public GroupMemberInfoResponse(String code, String description) {
		super(code, description);
	}

	public List <GroupMemberInfo> getGroupMembersInfo() {
		return groupMembersInfo;
	}
}