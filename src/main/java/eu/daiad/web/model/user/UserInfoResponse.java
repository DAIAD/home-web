package eu.daiad.web.model.user;

import eu.daiad.web.model.RestResponse;

public class UserInfoResponse extends RestResponse{
	
	private UserInfo userInfo;

	public UserInfoResponse(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public UserInfoResponse(String code, String description) {
		super(code, description);
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}
}