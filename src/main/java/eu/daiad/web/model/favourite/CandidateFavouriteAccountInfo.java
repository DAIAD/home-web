package eu.daiad.web.model.favourite;

import eu.daiad.web.domain.application.Account;
import eu.daiad.web.model.EnumGender;

public class CandidateFavouriteAccountInfo extends FavouriteAccountInfo {
	
	private String accountName;
	private String email;
	private EnumGender gender;
	private long accountCreationDateMils;
	private String city;
	private String country;
	private int numOfDevices;
	

	public CandidateFavouriteAccountInfo(Account account) {
		super(account.getKey());
		
		if (account.getFirstname() != null && account.getLastname() != null){
			this.accountName = account.getFirstname() + ' ' + account.getLastname();
		} else {
			this.accountName = account.getEmail();
		}
		
		this.email = account.getEmail();
		
		this.gender = account.getGender();
		
		this.accountCreationDateMils = account.getCreatedOn().getMillis();
		
		this.city = account.getCity();
		
		this.country = account.getCountry();
		
		this.numOfDevices = account.getDevices().size();
	}

	public String getAccountName() {
		return accountName;
	}

	public String getEmail() {
		return email;
	}

	public EnumGender getGender() {
		return gender;
	}

	public long getAccountCreationDateMils() {
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
