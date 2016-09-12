package eu.daiad.web.model.security;

public class PasswordResetMailModel {

    private AuthenticatedUser user;

    private String url;

    private String pin;

    public PasswordResetMailModel(AuthenticatedUser user, String url, String pin) {
        this.user = user;
        this.url = url;
        this.pin = pin;
    }

    public String getUrl() {
        return url;
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    public String getPin() {
        return pin;
    }

}
