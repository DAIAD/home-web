package eu.daiad.web.model;

import java.util.ArrayList;

public class ShowerCollectionResult extends RestResponse {

	ArrayList<Shower> showers = null;

	public ShowerCollectionResult() {
		super();

		this.showers = new ArrayList<Shower>();
	}

	public ShowerCollectionResult(int code, String description) {
		super(code, description);
	}

	public ArrayList<Shower> getShowers() {
		return this.showers;
	}

	public void add(Shower shower) {
		this.add(shower);
	}

	public void add(DataPoint point) {
		Shower shower = null;
		for (int i = 0, count = this.showers.size(); i < count; i++) {
			if (this.showers.get(i).getId() == point.showerId) {
				shower = this.showers.get(i);
				break;
			}
		}
		if (shower == null) {
			shower = new Shower(point.showerId);
			this.showers.add(shower);
		}
		shower.add(point);
	}
}
