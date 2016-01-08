package eu.daiad.web.model.export;

import eu.daiad.web.model.AuthenticatedRequest;

public class DownloadFileRequest extends AuthenticatedRequest {

	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	
}
