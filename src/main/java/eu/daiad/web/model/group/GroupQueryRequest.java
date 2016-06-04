package eu.daiad.web.model.group;

import eu.daiad.web.model.AuthenticatedRequest;

public class GroupQueryRequest extends AuthenticatedRequest {

    private GroupQuery query;

    public GroupQuery getQuery() {
        return query;
    }

    public void setQuery(GroupQuery query) {
        this.query = query;
    }

}
