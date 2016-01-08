package eu.daiad.web.model;

import java.util.ArrayList;

public class AmphiroSessionCollectionQueryResult extends RestResponse {

	ArrayList<AmphiroSessionCollection> devices = null;

	public AmphiroSessionCollectionQueryResult() {
		super();

		this.devices = new ArrayList<AmphiroSessionCollection>();
	}

	public AmphiroSessionCollectionQueryResult(int code, String description) {
		super(code, description);
	}

	public ArrayList<AmphiroSessionCollection> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<AmphiroSessionCollection> devices) {
		this.devices = devices;
	}

}
