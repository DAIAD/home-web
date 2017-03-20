package eu.daiad.web.model.security;

import eu.daiad.web.model.AuthenticatedRequest;

public class PasswordChangeRequest extends AuthenticatedRequest {

    private String username;

    private String password;

    private String captcha;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCaptcha() {
        return captcha;
    }
}
