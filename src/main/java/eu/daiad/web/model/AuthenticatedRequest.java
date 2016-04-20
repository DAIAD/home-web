package eu.daiad.web.model;

import eu.daiad.web.model.security.Credentials;

public class AuthenticatedRequest {

	private Credentials credentials;
	
	public Credentials getCredentials() {
		return this.credentials;
	}
	
	public void setCredentials(Credentials value) {
		this.credentials = value;
	}
}
