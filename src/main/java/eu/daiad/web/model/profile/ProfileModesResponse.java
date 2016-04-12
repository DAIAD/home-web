package eu.daiad.web.model.profile;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class ProfileModesResponse extends RestResponse {

	private List <ProfileModes> profileModes;

	public ProfileModesResponse(List <ProfileModes> profileModes) {
		this.profileModes = profileModes;
	}

	public ProfileModesResponse(String code, String description) {
		super(code, description);
	}

	public List <ProfileModes> getProfileSet() {
		return profileModes;
	}

}
