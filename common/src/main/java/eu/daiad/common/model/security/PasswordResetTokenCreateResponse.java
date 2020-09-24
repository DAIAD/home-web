package eu.daiad.common.model.security;

import java.util.UUID;

import eu.daiad.common.model.RestResponse;

public class PasswordResetTokenCreateResponse extends RestResponse {

    private UUID token;

    public PasswordResetTokenCreateResponse(UUID token) {
        this.token = token;
    }

    public UUID getToken() {
        return token;
    }

}
