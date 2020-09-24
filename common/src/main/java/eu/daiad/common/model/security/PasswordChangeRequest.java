package eu.daiad.common.model.security;

import eu.daiad.common.model.AuthenticatedRequest;

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
