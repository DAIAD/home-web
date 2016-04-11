package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.UUID;

import eu.daiad.web.service.DataQueryUserCollection;

public class PopulationFilter {

	private ArrayList<DataQueryUserCollection> groups = new ArrayList<DataQueryUserCollection>();

	public PopulationFilter() {

	}

	public PopulationFilter(String label, UUID user) {
		this.add(label, user);
	}

	public PopulationFilter(String label, UUID[] users) {
		this.add(label, users);
	}

	public ArrayList<DataQueryUserCollection> getGroups() {
		return groups;
	}

	public void add(String label, UUID user) {
		this.groups.add(new DataQueryUserCollection(label, user));
	}

	public void add(String label, UUID[] users) {
		this.groups.add(new DataQueryUserCollection(label, users));
	}

}
