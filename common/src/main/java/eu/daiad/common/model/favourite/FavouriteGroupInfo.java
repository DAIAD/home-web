package eu.daiad.common.model.favourite;

import java.util.UUID;

import eu.daiad.common.domain.application.FavouriteEntity;
import eu.daiad.common.domain.application.FavouriteGroupEntity;

public class FavouriteGroupInfo extends FavouriteInfo {
	
	private String groupName;
	private int size;
	private String country;
	private long groupCreationDateMils;
	
	public FavouriteGroupInfo(UUID groupKey) {
		super(groupKey);
	}

	public FavouriteGroupInfo(FavouriteEntity favourite) {
		super(favourite);
		
		FavouriteGroupEntity favouriteGroup = (FavouriteGroupEntity) favourite;
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