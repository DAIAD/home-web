package eu.daiad.common.model.query;

import eu.daiad.common.model.AuthenticatedRequest;

public class DataQueryRequest extends AuthenticatedRequest {

	private DataQuery query;

	public DataQuery getQuery() {
		return query;
	}

	public void setQuery(DataQuery query) {
		this.query = query;
	}

}
