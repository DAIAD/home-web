package eu.daiad.web.model.amphiro;

import eu.daiad.web.model.RestResponse;

public class AmphiroSessionTimeIntervalQueryResult extends RestResponse {

	private AmphiroSessionDetails session;

	public AmphiroSessionTimeIntervalQueryResult() {
		super();
	}

	public AmphiroSessionTimeIntervalQueryResult(String code, String description) {
		super(code, description);
	}

	public AmphiroSessionDetails getSession() {
		return session;
	}

	public void setSession(AmphiroSessionDetails session) {
		this.session = session;
	}

}
