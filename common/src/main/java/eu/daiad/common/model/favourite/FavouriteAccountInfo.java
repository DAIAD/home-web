package eu.daiad.common.model.favourite;

import java.util.UUID;

import eu.daiad.common.domain.application.FavouriteAccountEntity;
import eu.daiad.common.domain.application.FavouriteEntity;
import eu.daiad.common.model.EnumGender;

public class FavouriteAccountInfo extends FavouriteInfo {
	
	private String accountName;
	private String email;
	private EnumGender gender;
	private long accountCreationDateMils;
	private String city;
	private String country;
	private int numOfDevices;
	
	public FavouriteAccountInfo (UUID accountKey) {
		super(accountKey);
	}

	public FavouriteAccountInfo(FavouriteEntity favourite) {
		super(favourite);
		
		FavouriteAccountEntity favouriteAccount = (FavouriteAccountEntity) favourite;
		if (favouriteAccount.getAccount().getFirstname() != null || favouriteAccount.getAccount().getLastname() != null){
			this.accountName = favouriteAccount.getAccount().getFirstname() + ' ' + favouriteAccount.getAccount().getLastname();
		} else {
			this.accountName = favouriteAccount.getAccount().getEmail();
		}
		
		this.email = favouriteAccount.getAccount().getEmail();
		this.gender = favouriteAccount.getAccount().getGender();
		this.accountCreationDateMils = favouriteAccount.getAccount().getCreatedOn().getMillis();
		this.city = favouriteAccount.getAccount().getCity();
		this.country = favouriteAccount.getAccount().getUtility().getCountry();
		this.numOfDevices = favouriteAccount.getAccount().getDevices().size();
		
	}

	public String getAccountName() {
		return accountName;
	}
	
	public String getEmail() {
		return email;
	}
	
	public EnumGender getGender(){
		return gender;
	}
	
	public long getAccountCreationDateMils(){
		return accountCreationDateMils;
	}
	
	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}
	
	public int getNumOfDevices() {
		return numOfDevices;
	}
	
}