package eu.daiad.web.model.profile;

import java.util.ArrayList;

public class ProfileHousehold {
	
	private int id;
	
	private ArrayList<ProfileHouseholdMember> members;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<ProfileHouseholdMember> getMembers() {
		return members;
	}

	public void setMembers(ArrayList<ProfileHouseholdMember> members) {
		this.members = members;
	}
	

}
