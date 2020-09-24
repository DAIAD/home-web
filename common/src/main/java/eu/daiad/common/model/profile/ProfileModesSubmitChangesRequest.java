package eu.daiad.common.model.profile;

import java.util.List;

public class ProfileModesSubmitChangesRequest {
	
	private List <ProfileModesChanges> modeChanges;

	public List<ProfileModesChanges> getModeChanges() {
		return modeChanges;
	}

	public void setModeChanges(List<ProfileModesChanges> modeChanges) {
		this.modeChanges = modeChanges;
	}
	
	
}