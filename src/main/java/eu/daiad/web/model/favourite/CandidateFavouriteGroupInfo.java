package eu.daiad.web.model.favourite;

import eu.daiad.web.domain.application.Group;

public class CandidateFavouriteGroupInfo extends FavouriteGroupInfo {
	
	private String groupName;
	private int size;
	private String country;
	private long groupCreationDateMils;

	public CandidateFavouriteGroupInfo(Group group){
		super(group.getKey());
		this.groupName = group.getName();
		this.groupCreationDateMils = group.getCreatedOn().getMillis();
		this.size = group.getSize();
		this.country = group.getUtility().getCountry();
		
	}

	public String getGroupName() {
		return groupName;
	}

	public int getSize() {
		return size;
	}

	public String getCountry() {
		return country;
	}
	
	public long getGroupCreationDateMils(){
		return groupCreationDateMils;
	}
	
}