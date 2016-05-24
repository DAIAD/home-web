package eu.daiad.web.model.favourite;

import eu.daiad.web.domain.application.FavouriteGroup;

import java.util.UUID;

import eu.daiad.web.domain.application.Favourite;

public class FavouriteGroupInfo extends FavouriteInfo {
	
	private String groupName;
	private int size;
	private String country;
	private long groupCreationDateMils;
	
	public FavouriteGroupInfo(UUID groupKey) {
		super(groupKey);
	}

	public FavouriteGroupInfo(Favourite favourite) {
		super(favourite);
		
		FavouriteGroup favouriteGroup = (FavouriteGroup) favourite;
		this.groupName = favouriteGroup.getGroup().getName();
		this.groupCreationDateMils = favouriteGroup.getGroup().getCreatedOn().getMillis();
		this.size = favouriteGroup.getGroup().getSize();
		this.country = favouriteGroup.getGroup().getUtility().getCountry();
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