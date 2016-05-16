package eu.daiad.web.model.favourite;

import java.util.UUID;

import eu.daiad.web.domain.application.Favourite;
import eu.daiad.web.domain.application.FavouriteAccount;
import eu.daiad.web.domain.application.FavouriteGroup;


public class FavouriteInfo {
	
	private UUID key;
	
	private UUID refId;

	private String name;

	private EnumFavouriteType type;

	private long additionDateMils;
	
	public FavouriteInfo(UUID refKey){
		this.key = null;
		this.refId = refKey;
		this.name = null;
		this.type = null;
		this.additionDateMils = -1;
	}
		
	public FavouriteInfo (Favourite favourite) {
		this.key = favourite.getKey();
		this.name = favourite.getLabel();		
		this.type = favourite.getType();
		this.additionDateMils = favourite.getCreatedOn().getMillis();

		switch (favourite.getType()){
		
		case ACCOUNT :
			FavouriteAccount accountFavourite = (FavouriteAccount) favourite;
			this.refId = accountFavourite.getAccount().getKey();
			break;
			
		case GROUP :
			FavouriteGroup groupFavourite = (FavouriteGroup) favourite;
			this.refId = groupFavourite.getGroup().getKey();
			break;
		
		default:
			this.refId = favourite.getKey();
		} 
	}

	public UUID getRefId() {
		return refId;
	}

	public String getName() {
		return name;
	}

	public EnumFavouriteType getType() {
		return type;
	}

	public long getAdditionDateMils() {
		return additionDateMils;
	}

	public UUID getKey() {
		return key;
	}

}
