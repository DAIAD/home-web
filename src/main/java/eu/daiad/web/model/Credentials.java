package eu.daiad.web.model;

public class Credentials {

	private String username;
	
	private String password;

	public void setUsername(String value) {
		this.username = value;
	}

	public String getUsername() {
		if(this.username == null) {
			return "";
		}
		return this.username.trim();
	}
	
	public void setPassword(String value) {
		this.password = value;
	}

	public String getPassword() {
		if(this.password == null) {
			return "";
		}
		return this.password;
	}
}
