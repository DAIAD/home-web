package eu.daiad.common.model;

import java.util.ArrayList;

import eu.daiad.common.model.error.Error;
import eu.daiad.common.model.error.ErrorCode;

public class RestResponse {

    private ArrayList<Error> errors = new ArrayList<Error>();

    public RestResponse() {
    }

    public RestResponse(ErrorCode code, String description) {
        this.add(code, description);
    }

    public RestResponse(Error error) {
        this.errors.add(error);
    }

    public RestResponse(ArrayList<Error> errors) {
        this.errors.addAll(errors);
    }

    public boolean getSuccess() {
        return (this.errors.size() == 0);
    }

    public ArrayList<Error> getErrors() {
        return this.errors;
    }

    public void add(ErrorCode code, String description) {
        this.errors.add(new Error(code.getMessageKey(), description));
    }

    public void add(Error error) {
        this.errors.add(error);
    }

    public void add(ArrayList<Error> errors) {
        this.errors.addAll(errors);
    }

    public RestResponse toRestResponse() {
        if (this instanceof RestResponse) {
            return this;
        }

        return new RestResponse(this.getErrors());
    }
    
    public static RestResponse error(ErrorCode code, String message) {
    	return new RestResponse(code, message);
    }
}
