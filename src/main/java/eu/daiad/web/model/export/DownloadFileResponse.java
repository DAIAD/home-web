package eu.daiad.web.model.export;

import eu.daiad.web.model.RestResponse;

public class DownloadFileResponse extends RestResponse {

	private String token;

	public DownloadFileResponse() {
		super();
	}

	public DownloadFileResponse(String token) {
		super();

		this.token = token;
	}

	public DownloadFileResponse(String code, String description) {
		super(code, description);
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
