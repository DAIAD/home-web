package eu.daiad.web.model.group;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class GroupListInfoResponse extends RestResponse{
	
	private List <GroupInfo> groupListInfo;

	public GroupListInfoResponse(List <GroupInfo> groupListInfo) {
		this.groupListInfo = groupListInfo;
	}

	public GroupListInfoResponse(String code, String description) {
		super(code, description);
	}

	public List <GroupInfo> getGroupListInfo() {
		return groupListInfo;
	}
}