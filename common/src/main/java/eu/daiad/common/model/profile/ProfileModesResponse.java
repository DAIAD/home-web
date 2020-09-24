package eu.daiad.common.model.profile;

import java.util.List;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class ProfileModesResponse extends RestResponse {

	private List <ProfileModes> profileModes;

	public ProfileModesResponse(List <ProfileModes> profileModes) {
		this.profileModes = profileModes;
	}

	public ProfileModesResponse(ErrorCode code, String description) {
		super(code, description);
	}

	public List <ProfileModes> getProfileSet() {
		return profileModes;
	}

}
