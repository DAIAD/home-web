package eu.daiad.web.service;

import java.util.ArrayList;
import java.util.UUID;

public class DataQueryUserCollection {

	private String label;

	private ArrayList<UUID> users = new ArrayList<UUID>();

	public DataQueryUserCollection() {

	}

	public DataQueryUserCollection(String label) {
		this.label = label;
	}

	public DataQueryUserCollection(String label, UUID user) {
		this.label = label;
		this.users.add(user);
	}

	public DataQueryUserCollection(String label, UUID[] users) {
		this.label = label;
		for (UUID userKey : users) {
			this.users.add(userKey);
		}
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ArrayList<UUID> getUsers() {
		return users;
	}

}
