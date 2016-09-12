package eu.daiad.web.model.security;

import java.util.UUID;

public class PasswordResetToken {

    private UUID token;

    private String pin;

    public PasswordResetToken(UUID token, String pin) {
        this.token = token;
        this.pin = pin;
    }

    public UUID getToken() {
        return token;
    }

    public String getPin() {
        return pin;
    }

}
