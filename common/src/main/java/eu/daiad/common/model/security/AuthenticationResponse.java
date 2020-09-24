package eu.daiad.common.model.security;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.Runtime;
import eu.daiad.common.model.error.ErrorCode;
import eu.daiad.common.model.profile.Profile;

public class AuthenticationResponse extends RestResponse {

    private Runtime runtime;

    private Profile profile;

    private String[] roles;

    public AuthenticationResponse(Runtime runtime, Profile profile, String[] roles) {
        super();

        this.runtime = runtime;
        this.profile = profile;
        this.roles = roles;
    }

    public AuthenticationResponse(ErrorCode code, String description) {
        super(code, description);
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public String[] getRoles() {
        return roles;
    }
}
