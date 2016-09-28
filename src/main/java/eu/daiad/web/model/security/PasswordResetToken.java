package eu.daiad.web.model.security;

import java.util.UUID;

public class PasswordResetToken {

    private UUID token;

    private String pin;
    
    private String locale;

    public PasswordResetToken(UUID token, String pin, String locale) {
        this.token = token;
        this.pin = pin;
        this.locale = locale;
    }

    public UUID getToken() {
        return token;
    }

    public String getPin() {
        return pin;
    }
    
    public String getLocale() {
        return locale;
    }

}
