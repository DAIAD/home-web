package eu.daiad.common.model.query.savings;

import eu.daiad.common.model.AuthenticatedRequest;

public class CreateSavingScenarioRequest extends AuthenticatedRequest {

    private String title;

    TemporalSavingsConsumerSelectionFilter parameters;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TemporalSavingsConsumerSelectionFilter getParameters() {
        return parameters;
    }

    public void setParameters(TemporalSavingsConsumerSelectionFilter parameters) {
        this.parameters = parameters;
    }

}
