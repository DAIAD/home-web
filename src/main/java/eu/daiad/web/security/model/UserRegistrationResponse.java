package eu.daiad.web.security.model;

import eu.daiad.web.model.RestResponse;

public class UserRegistrationResponse extends RestResponse {

	private String applicationKey;
	
	public UserRegistrationResponse() {
		super();
	}

	public UserRegistrationResponse(int code, String description) {
		super(code, description);
	}
	
    public String getApplicationKey() {
    	return this.applicationKey;
    }
    
    public void setApplicationKey(String value) {
    	this.applicationKey = value;
    }
}
