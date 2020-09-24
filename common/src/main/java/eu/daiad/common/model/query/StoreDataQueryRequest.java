package eu.daiad.common.model.query;

import eu.daiad.common.model.AuthenticatedRequest;

public class StoreDataQueryRequest extends AuthenticatedRequest {

    private NamedDataQuery namedQuery;

    public NamedDataQuery getNamedQuery() {
        return namedQuery;
    }

    public void setNamedQuery(NamedDataQuery namedQuery) {
        this.namedQuery = namedQuery;
    }

}
