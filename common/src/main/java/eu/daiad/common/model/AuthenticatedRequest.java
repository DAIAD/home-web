package eu.daiad.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.common.model.security.Credentials;

public class AuthenticatedRequest {

	@JsonIgnore
	private Credentials credentials;

	@JsonIgnore
	public Credentials getCredentials() {
		return this.credentials;
	}

	@JsonProperty
	public void setCredentials(Credentials value) {
		this.credentials = value;
	}
}
