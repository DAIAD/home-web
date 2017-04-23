package eu.daiad.web.model.query.savings;

import eu.daiad.web.model.RestResponse;

public class BudgetResponse extends RestResponse {

    private Budget budget;

    public BudgetResponse(Budget budget) {
        this.budget = budget;
    }

    public Budget getBudget() {
        return budget;
    }

}
