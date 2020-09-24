package eu.daiad.common.model.amphiro;

import java.util.ArrayList;

import eu.daiad.common.model.RestResponse;

public class AmphiroSessionUpdateCollection extends RestResponse {

	private ArrayList<AmphiroSessionUpdate> updates = null;

	public AmphiroSessionUpdateCollection() {
		this.updates = new ArrayList<AmphiroSessionUpdate>();
	}

	public ArrayList<AmphiroSessionUpdate> getUpdates() {
		return updates;
	}

}
