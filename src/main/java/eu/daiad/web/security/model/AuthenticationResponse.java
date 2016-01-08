package eu.daiad.web.security.model;

import eu.daiad.web.model.RestResponse;

public class AuthenticationResponse  extends RestResponse {

	private String userKey;

	public AuthenticationResponse(String userKey) {
		super();
		
		this.userKey = userKey;
	}

	public AuthenticationResponse(int code, String description) {
		super(code, description);
	}
	
    public String getUserKey() {
    	return this.userKey;
    }
    
    public void setUserKey(String value) {
    	this.userKey = value;
    }
}
