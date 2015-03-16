package eu.daiad.web.model;

public class ShowerResult extends RestResponse {

	private ShowerDetails shower;

	public ShowerResult() {
		super();
	}

	public ShowerResult(int code, String description) {
		super(code, description);
	}

	public ShowerDetails getShower() {
		return this.shower;
	}

	public void setShower(ShowerDetails shower) {
		this.shower = shower;
	}
}
