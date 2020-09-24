package eu.daiad.common.model.user;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class UserRegistrationResponse extends RestResponse {

	private String userKey;

	public UserRegistrationResponse() {
		super();
	}

	public UserRegistrationResponse(ErrorCode code, String description) {
		super(code, description);
	}

	public String getUserKey() {
		return this.userKey;
	}

	public void setUserKey(String value) {
		this.userKey = value;
	}
}
