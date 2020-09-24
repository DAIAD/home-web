package eu.daiad.common.model.group;

import eu.daiad.common.model.AuthenticatedRequest;

public class GroupQueryRequest extends AuthenticatedRequest {

    private GroupQuery query;

    public GroupQuery getQuery() {
        return query;
    }

    public void setQuery(GroupQuery query) {
        this.query = query;
    }

}
