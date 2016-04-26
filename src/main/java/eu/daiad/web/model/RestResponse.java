package eu.daiad.web.model;

import java.util.ArrayList;

import eu.daiad.web.model.error.Error;
import eu.daiad.web.model.error.ErrorCode;

public class RestResponse {

	private ArrayList<Error> errors = new ArrayList<Error>();

	public RestResponse() {
	}

	public RestResponse(String code, String description) {
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
		this.errors.add(new Error(code.toString(), description));
	}

	public void add(String code, String description) {
		this.errors.add(new Error(code, description));
	}

	public void add(Error error) {
		this.errors.add(error);
	}

	public void add(ArrayList<Error> errors) {
		this.errors.addAll(errors);
	}
}
