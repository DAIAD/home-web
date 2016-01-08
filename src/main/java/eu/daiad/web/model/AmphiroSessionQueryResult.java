package eu.daiad.web.model;

public class AmphiroSessionQueryResult extends RestResponse {

	private AmphiroSessionDetails session;

	public AmphiroSessionQueryResult() {
		super();
	}

	public AmphiroSessionQueryResult(int code, String description) {
		super(code, description);
	}

	public AmphiroSessionDetails getSession() {
		return session;
	}

	public void setSession(AmphiroSessionDetails session) {
		this.session = session;
	}


}
