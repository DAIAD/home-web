package eu.daiad.web.model.query.savings;

import eu.daiad.web.model.AuthenticatedRequest;

public class CreateSavingScenarioRequest extends AuthenticatedRequest {

    private String title;

    SavingScenarioParameters parameters;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public SavingScenarioParameters getParameters() {
        return parameters;
    }

    public void setParameters(SavingScenarioParameters parameters) {
        this.parameters = parameters;
    }

}
