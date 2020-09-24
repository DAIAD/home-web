package eu.daiad.common.model.scheduling;

import eu.daiad.common.model.RestResponse;

public class ExecutionMessageResponse extends RestResponse {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
