package eu.daiad.web.model.query.savings;

import eu.daiad.web.model.AuthenticatedRequest;

public class BudgetQueryRequest extends AuthenticatedRequest {

    private BudgetQuery query;

    public BudgetQuery getQuery() {
        return query;
    }

    public void setQuery(BudgetQuery query) {
        this.query = query;
    }

}
