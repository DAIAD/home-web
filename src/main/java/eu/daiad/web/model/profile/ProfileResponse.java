package eu.daiad.web.model.profile;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.Runtime;

public class ProfileResponse extends RestResponse {

    private Runtime runtime;

    private Profile profile;

    private String[] roles;

    public ProfileResponse(Runtime runtime, Profile profile, String[] roles) {
        this.runtime = runtime;
        this.profile = profile;
        this.roles = roles;
    }

    public ProfileResponse(String code, String description) {
        super(code, description);
    }

    public Profile getProfile() {
        return profile;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public String[] getRoles() {
        return roles;
    }

}
