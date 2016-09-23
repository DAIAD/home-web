package eu.daiad.web.model.security;

import java.util.UUID;

public class PasswordResetTokenRedeemRequest {

    private UUID token;

    private String pin;

    private String password;

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

}
