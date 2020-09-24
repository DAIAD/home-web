package eu.daiad.common.model.group;

import eu.daiad.common.model.AuthenticatedRequest;

public class CommonsQueryRequest extends AuthenticatedRequest {

    private CommonsQuery query;

    public CommonsQuery getQuery() {
        return query;
    }

    public void setQuery(CommonsQuery query) {
        this.query = query;
    }

}
