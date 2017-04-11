package eu.daiad.web.model.query.savings;

import java.util.UUID;

import eu.daiad.web.model.RestResponse;

public class CrateSavingScenarioResponse extends RestResponse {

    private UUID key;

    public CrateSavingScenarioResponse(UUID key) {
        this.key = key;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

}
