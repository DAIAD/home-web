package eu.daiad.common.model.logging;

import eu.daiad.common.model.AuthenticatedRequest;

public class LogEventQueryRequest extends AuthenticatedRequest {

	private LogEventQuery query;

	public LogEventQuery getQuery() {
		return query;
	}

	public void setQuery(LogEventQuery query) {
		this.query = query;
	}

}
