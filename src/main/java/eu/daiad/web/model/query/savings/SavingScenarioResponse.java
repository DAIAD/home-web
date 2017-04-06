package eu.daiad.web.model.query.savings;

import eu.daiad.web.model.RestResponse;

public class SavingScenarioResponse extends RestResponse {

    private SavingScenario scenario;

    public SavingScenarioResponse(SavingScenario scenario) {
        this.scenario = scenario;
    }

    public SavingScenario getScenario() {
        return scenario;
    }

}
