package eu.daiad.web.model;

public class DownloadFileRequest extends AuthenticatedRequest {

	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
}
