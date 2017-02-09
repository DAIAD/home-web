package eu.daiad.web.model.group;

import eu.daiad.web.model.AuthenticatedRequest;

public class CommonsQueryRequest extends AuthenticatedRequest {

    private CommonsQuery query;

    public CommonsQuery getQuery() {
        return query;
    }

    public void setQuery(CommonsQuery query) {
        this.query = query;
    }

}
