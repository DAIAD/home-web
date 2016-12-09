package eu.daiad.web.model.security;

public class Credentials {

    private String username;

    private String password;

    private String version;

    public void setUsername(String value) {
        username = value;
    }

    public String getUsername() {
        if (username == null) {
            return "";
        }
        return username.trim();
    }

    public void setPassword(String value) {
        password = value;
    }

    public String getPassword() {
        if (password == null) {
            return "";
        }
        return password;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
