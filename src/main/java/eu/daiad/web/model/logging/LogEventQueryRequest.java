package eu.daiad.web.model.logging;

import eu.daiad.web.model.AuthenticatedRequest;

public class LogEventQueryRequest extends AuthenticatedRequest {

	private LogEventQuery query;

	public LogEventQuery getQuery() {
		return query;
	}

	public void setQuery(LogEventQuery query) {
		this.query = query;
	}

}
