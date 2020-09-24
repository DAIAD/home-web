package eu.daiad.common.model.query.savings;

import eu.daiad.common.model.RestResponse;

public class SavingScenarioResponse extends RestResponse {

    private SavingScenario scenario;

    public SavingScenarioResponse(SavingScenario scenario) {
        this.scenario = scenario;
    }

    public SavingScenario getScenario() {
        return scenario;
    }

}
