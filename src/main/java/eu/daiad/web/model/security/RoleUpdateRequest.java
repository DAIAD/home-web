package eu.daiad.web.model.security;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;

public class RoleUpdateRequest extends AuthenticatedRequest {

	private String username;

	@JsonDeserialize(using = EnumRole.Deserializer.class)
	private EnumRole role;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public EnumRole getRole() {
		return role;
	}

	public void setRole(EnumRole role) {
		this.role = role;
	}
}
