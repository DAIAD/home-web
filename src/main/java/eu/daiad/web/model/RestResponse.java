package eu.daiad.web.model;

import java.util.ArrayList;

public class RestResponse {

	private ArrayList<Error> errors = new ArrayList<Error>();

	public RestResponse() {
	}

	public RestResponse(int code, String description) {
		this.add(code, description);
	}

	public boolean getSuccess() {
		return (this.errors.size() == 0);
	}

	public ArrayList<Error> getErrors() {
		return this.errors;
	}

	public void add(int code, String description) {
		this.errors.add(new Error(code, description));
	}
}
