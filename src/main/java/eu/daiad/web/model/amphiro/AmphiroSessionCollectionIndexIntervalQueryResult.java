package eu.daiad.web.model.amphiro;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class AmphiroSessionCollectionIndexIntervalQueryResult extends RestResponse {

	ArrayList<AmphiroSessionCollection> devices = null;

	public AmphiroSessionCollectionIndexIntervalQueryResult() {
		super();

		this.devices = new ArrayList<AmphiroSessionCollection>();
	}

	public AmphiroSessionCollectionIndexIntervalQueryResult(String code, String description) {
		super(code, description);
	}

	public ArrayList<AmphiroSessionCollection> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<AmphiroSessionCollection> devices) {
		this.devices = devices;
	}

}
