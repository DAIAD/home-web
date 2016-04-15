package eu.daiad.web.model.profile;

import java.util.List;
import java.util.UUID;

public class ProfileModesChanges {
	
	private UUID id;
	private List <ProfileModeChange> changes;

	
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public List <ProfileModeChange> getChanges() {
		return changes;
	}
	public void setChanges(List <ProfileModeChange> changes) {
		this.changes = changes;
	}
}