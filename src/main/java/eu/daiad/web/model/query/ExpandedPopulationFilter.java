package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.UUID;

public class ExpandedPopulationFilter {

	private String label;

	private ArrayList<UUID> users = new ArrayList<UUID>();

	private ArrayList<byte[]> hashes = new ArrayList<byte[]>();

	private ArrayList<byte[]> serials = new ArrayList<byte[]>();

	public ExpandedPopulationFilter() {

	}

	public ExpandedPopulationFilter(String label) {
		this.label = label;
	}

	public ExpandedPopulationFilter(String label, UUID user, byte[] hash) {
		this.label = label;
		this.users.add(user);
		this.hashes.add(hash);
	}

	public ExpandedPopulationFilter(String label, UUID[] users, byte[] hashes[]) {
		this.label = label;
		for (UUID userKey : users) {
			this.users.add(userKey);
		}
		for (byte[] hash : hashes) {
			this.hashes.add(hash);
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

	public ArrayList<byte[]> getHashes() {
		return hashes;
	}

	public ArrayList<byte[]> getSerials() {
		return serials;
	}

}
