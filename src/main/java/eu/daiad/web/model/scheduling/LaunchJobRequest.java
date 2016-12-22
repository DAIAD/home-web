package eu.daiad.web.model.scheduling;

import java.util.Map;

import eu.daiad.web.model.AuthenticatedRequest;

public class LaunchJobRequest extends AuthenticatedRequest {

    private Map<String, String> parameters;

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

}
