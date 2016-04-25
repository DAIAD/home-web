package eu.daiad.web.model.group;

import java.util.UUID;

import eu.daiad.web.domain.application.Group;

public class GroupInfo {
	
	private UUID id;

	private String name;

	private int numberOfMembers;

	private long creationDateMils;

	public GroupInfo (Group group) {
		this.id = group.getKey();
		this.name = group.getName();		
		this.numberOfMembers = group.getMembers().size();
		this.creationDateMils = group.getCreatedOn().getMillis();
	}
	
	public UUID getId() {
		return id;
	}
	public String getName() {
		return name;
	}

	public int getNumberOfMembers() {
		return numberOfMembers;
	}

	public long getCreationDateMils() {
		return creationDateMils;
	}
	
}