package eu.daiad.common.model.security;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.common.model.EnumApplication;

public class PasswordResetTokenCreateRequest {

    private String username;

    @JsonDeserialize(using = EnumApplication.Deserializer.class)
    private EnumApplication application;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public EnumApplication getApplication() {
        return application;
    }

    public void setApplication(EnumApplication application) {
        this.application = application;
    }

}
