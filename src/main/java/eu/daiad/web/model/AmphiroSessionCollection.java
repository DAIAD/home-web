package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

public class AmphiroSessionCollection {

	private UUID deviceKey;

	private ArrayList<AmphiroSession> sessions;

	public AmphiroSessionCollection() {
		this.sessions = new ArrayList<AmphiroSession>();
	}

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

	public ArrayList<AmphiroSession> getSessions() {
		return sessions;
	}

	public void setSessions(ArrayList<AmphiroSession> sessions) {
		this.sessions = sessions;
	}
}
