package eu.daiad.web.model.security;

import java.util.UUID;

import eu.daiad.web.model.RestResponse;

public class PasswordResetTokenCreateResponse extends RestResponse {

    private UUID token;

    public PasswordResetTokenCreateResponse(UUID token) {
        this.token = token;
    }

    public UUID getToken() {
        return token;
    }

}
