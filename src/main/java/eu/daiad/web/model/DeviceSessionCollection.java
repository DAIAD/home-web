package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

public class DeviceSessionCollection {

	private UUID deviceKey;

	private ArrayList<SessionData> sessions;

	public DeviceSessionCollection() {
		this.sessions = new ArrayList<SessionData>();
	}

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

	public ArrayList<SessionData> getSessions() {
		return sessions;
	}

	public void setSessions(ArrayList<SessionData> sessions) {
		this.sessions = sessions;
	}
}
