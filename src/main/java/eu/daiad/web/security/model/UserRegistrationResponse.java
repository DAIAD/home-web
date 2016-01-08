package eu.daiad.web.security.model;

import eu.daiad.web.model.RestResponse;

public class UserRegistrationResponse extends RestResponse {

	private String userKey;
	
	public UserRegistrationResponse() {
		super();
	}

	public UserRegistrationResponse(int code, String description) {
		super(code, description);
	}
	
    public String getUserKey() {
    	return this.userKey;
    }
    
    public void setUserKey(String value) {
    	this.userKey = value;
    }
}
