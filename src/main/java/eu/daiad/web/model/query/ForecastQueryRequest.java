package eu.daiad.web.model.query;

import eu.daiad.web.model.AuthenticatedRequest;

public class ForecastQueryRequest extends AuthenticatedRequest {

    private ForecastQuery query;

    public ForecastQuery getQuery() {
        return query;
    }

    public void setQuery(ForecastQuery query) {
        this.query = query;
    }

}
