package eu.daiad.web.model.amphiro;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class AmphiroSessionCollectionQueryResult extends RestResponse {

	ArrayList<AmphiroSessionCollection> devices = null;

	public AmphiroSessionCollectionQueryResult() {
		super();

		this.devices = new ArrayList<AmphiroSessionCollection>();
	}

	public AmphiroSessionCollectionQueryResult(String code, String description) {
		super(code, description);
	}

	public ArrayList<AmphiroSessionCollection> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<AmphiroSessionCollection> devices) {
		this.devices = devices;
	}

}
