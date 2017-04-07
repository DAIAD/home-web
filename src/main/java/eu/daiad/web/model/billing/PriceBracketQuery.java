package eu.daiad.web.model.billing;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.model.AuthenticatedRequest;

public class PriceBracketQuery extends AuthenticatedRequest {

    @JsonIgnore
    private UUID userKey;

    @JsonIgnore
    public UUID getUserKey() {
        return userKey;
    }
    
    @JsonProperty
    public void setUserKey(UUID userKey) {
        this.userKey = userKey;
    }
}
