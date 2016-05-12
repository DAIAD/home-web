package eu.daiad.web.model.amphiro;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class AmphiroSessionCollectionTimeIntervalQueryResult extends RestResponse {

	ArrayList<AmphiroSessionCollection> devices = null;

	public AmphiroSessionCollectionTimeIntervalQueryResult() {
		super();

		this.devices = new ArrayList<AmphiroSessionCollection>();
	}

	public AmphiroSessionCollectionTimeIntervalQueryResult(String code, String description) {
		super(code, description);
	}

	public ArrayList<AmphiroSessionCollection> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<AmphiroSessionCollection> devices) {
		this.devices = devices;
	}

}
