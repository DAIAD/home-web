package eu.daiad.web.model.query.savings;

import eu.daiad.web.model.AuthenticatedRequest;

public class SavingScenarioQueryRequest extends AuthenticatedRequest {

    private SavingScenarioQuery query;

    public SavingScenarioQuery getQuery() {
        return query;
    }

    public void setQuery(SavingScenarioQuery query) {
        this.query = query;
    }

}
