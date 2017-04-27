package eu.daiad.web.model.group;

import java.util.UUID;

import eu.daiad.web.model.RestResponse;

public class CommonsCreateRestResponse extends RestResponse {

    private UUID key;

    public CommonsCreateRestResponse(UUID key) {
        this.key = key;
    }

    public UUID getKey() {
        return key;
    }

}
