package eu.daiad.web.model;

public class ExtendedSessionData extends SessionData {

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
