package eu.daiad.web.model.query;

import eu.daiad.web.model.AuthenticatedRequest;

public class DataQueryRequest extends AuthenticatedRequest {

	private DataQuery query;

	public DataQuery getQuery() {
		return query;
	}

	public void setQuery(DataQuery query) {
		this.query = query;
	}

}
