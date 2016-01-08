package eu.daiad.web.model.export;

import eu.daiad.web.model.amphiro.AmphiroSession;

public class ExtendedSessionData extends AmphiroSession {

	private SessionUserData user;
	
	private SessionDeviceData device;

	public ExtendedSessionData() {
		this.user = new SessionUserData();
		this.device = new SessionDeviceData();
	}
	public SessionUserData getUser() {
		return user;
	}

	public SessionDeviceData getDevice() {
		return device;
	}

}
