package eu.daiad.web.model.amphiro;

import eu.daiad.web.model.RestResponse;

public class AmphiroSessionIndexIntervalQueryResult extends RestResponse {

	private AmphiroSessionDetails session;

	public AmphiroSessionIndexIntervalQueryResult() {
		super();
	}

	public AmphiroSessionIndexIntervalQueryResult(String code, String description) {
		super(code, description);
	}

	public AmphiroSessionDetails getSession() {
		return session;
	}

	public void setSession(AmphiroSessionDetails session) {
		this.session = session;
	}

}
