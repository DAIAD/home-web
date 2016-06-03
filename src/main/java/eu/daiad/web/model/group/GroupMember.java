package eu.daiad.web.model.group;

import java.util.UUID;

public class GroupMember {

	private UUID key;

	private String username;

	private long addeOn;

	private String fullname;

	public UUID getKey() {
		return key;
	}

	public void setKey(UUID key) {
		this.key = key;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public long getAddeOn() {
		return addeOn;
	}

	public void setAddeOn(long addeOn) {
		this.addeOn = addeOn;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
}
