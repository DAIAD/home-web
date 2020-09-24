package eu.daiad.common.model.query.savings;

import eu.daiad.common.model.AuthenticatedRequest;

public class SavingScenarioQueryRequest extends AuthenticatedRequest {

    private SavingScenarioQuery query;

    public SavingScenarioQuery getQuery() {
        return query;
    }

    public void setQuery(SavingScenarioQuery query) {
        this.query = query;
    }

}
