package eu.daiad.web.model.security;

import eu.daiad.web.model.AuthenticatedRequest;

public class PasswordChangeRequest extends AuthenticatedRequest {

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
