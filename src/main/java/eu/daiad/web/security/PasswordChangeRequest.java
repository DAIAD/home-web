package eu.daiad.web.security;

import eu.daiad.web.model.AuthenticatedRequest;

public class PasswordChangeRequest extends AuthenticatedRequest {

    private String username;

    private String password;

    public String getPassword() {
        if (password == null) {
            return "";
        }
        return password.trim();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}