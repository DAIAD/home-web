package eu.daiad.web.model;

public class Error {

	public static final int ERROR_UNKNOWN = 1;
	public static final int ERROR_PARSE = 2;
	public static final int ERROR_AUTHENTICATION = 3;
	public static final int ERROR_AUTHORIZATION = 4;
	
	public static final int ERROR_NOT_FOUND = 5;
	public static final int ERROR_FORBIDDEN = 6;
	
	private int code;
	
	private String description;
	
	public Error(int code, String description) {
		this.code = code;
		this.description = description;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
