package eu.daiad.web.model.scheduling;

import eu.daiad.web.model.RestResponse;

public class ExecutionMessageResponse extends RestResponse {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
