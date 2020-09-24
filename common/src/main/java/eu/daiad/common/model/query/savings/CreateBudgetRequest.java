package eu.daiad.common.model.query.savings;

import eu.daiad.common.model.AuthenticatedRequest;

public class CreateBudgetRequest extends AuthenticatedRequest {

    private String title;

    BudgetParameters parameters;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BudgetParameters getParameters() {
        return parameters;
    }

    public void setParameters(BudgetParameters parameters) {
        this.parameters = parameters;
    }

}
