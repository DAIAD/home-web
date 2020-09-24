package eu.daiad.common.model.amphiro;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class AmphiroSessionIndexIntervalQueryResult extends RestResponse {

	private AmphiroSessionDetails session;

	public AmphiroSessionIndexIntervalQueryResult() {
		super();
	}

	public AmphiroSessionIndexIntervalQueryResult(ErrorCode code, String description) {
		super(code, description);
	}

	public AmphiroSessionDetails getSession() {
		return session;
	}

	public void setSession(AmphiroSessionDetails session) {
		this.session = session;
	}

}
