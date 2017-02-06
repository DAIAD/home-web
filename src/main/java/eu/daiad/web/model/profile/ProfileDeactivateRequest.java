package eu.daiad.web.model.profile;

import java.util.UUID;

public class ProfileDeactivateRequest {

    private UUID userkey;

    public UUID getUserkey() {
        return userkey;
    }

    public void setUserkey(UUID userkey) {
        this.userkey = userkey;
    }

}
