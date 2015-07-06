package eu.daiad.web.security.model;

public class LoginStatus {

	private final boolean connected;

	private final String firstname;

	public LoginStatus() {
		this.connected = false;
		this.firstname = null;
	}

	public LoginStatus(boolean connected, String firstname) {
		this.connected = connected;
		this.firstname = firstname;
	}

	public boolean isConnected() {
		return connected;
	}

	public String getFirstname() {
		return firstname;
	}
}