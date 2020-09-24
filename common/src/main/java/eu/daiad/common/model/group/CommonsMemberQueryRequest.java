package eu.daiad.common.model.group;

import eu.daiad.common.model.AuthenticatedRequest;

public class CommonsMemberQueryRequest extends AuthenticatedRequest {

    private CommonsMemberQuery query;

    public CommonsMemberQuery getQuery() {
        return query;
    }

    public void setQuery(CommonsMemberQuery query) {
        this.query = query;
    }

}
