package eu.daiad.web.model.group;

import eu.daiad.web.model.AuthenticatedRequest;

public class CommonsMemberQueryRequest extends AuthenticatedRequest {

    private CommonsMemberQuery query;

    public CommonsMemberQuery getQuery() {
        return query;
    }

    public void setQuery(CommonsMemberQuery query) {
        this.query = query;
    }

}
