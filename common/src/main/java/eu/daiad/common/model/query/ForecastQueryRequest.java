package eu.daiad.common.model.query;

import eu.daiad.common.model.AuthenticatedRequest;

public class ForecastQueryRequest extends AuthenticatedRequest {

    private ForecastQuery query;

    public ForecastQuery getQuery() {
        return query;
    }

    public void setQuery(ForecastQuery query) {
        this.query = query;
    }

}
