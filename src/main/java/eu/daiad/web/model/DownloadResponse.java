package eu.daiad.web.model;

public class DownloadResponse extends RestResponse {
	
	private String token;

	public DownloadResponse() {
		super();
	}

	public DownloadResponse(String token) {
		super();
		
		this.token = token;
	}
	
	public DownloadResponse(int code, String description) {
		super(code, description);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
