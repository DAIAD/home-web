package eu.daiad.web.model.commons;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import eu.daiad.web.model.AuthenticatedRequest;

public class CreateCommonsRequest extends AuthenticatedRequest {

	@Valid
	@NotNull
	private Community community;

	public Community getCommunity() {
		return community;
	}

	public void setCommunity(Community community) {
		this.community = community;
	}

}
