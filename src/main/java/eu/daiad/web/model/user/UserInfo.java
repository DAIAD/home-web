package eu.daiad.web.model.user;

import java.util.UUID;

import eu.daiad.web.model.EnumGender;


public class UserInfo {
	
	private UUID id;

	private String firstName;
	
	private String lastName;

	private String email;
	
	private EnumGender gender;
	
	private long registrationDateMils;
	
	private String country;
	
	private String city;
	
	private String address;

	private String postalCode;	
	
	public UserInfo (eu.daiad.web.domain.application.Account account) {
		this.id = account.getKey();
		this.firstName = account.getFirstname();
		this.lastName = account.getLastname();
		this.email = account.getEmail();
		this.gender = account.getGender();
		this.registrationDateMils = account.getCreatedOn().getMillis();
		this.country = account.getCountry();
		this.city = account.getCity();
		this.address = account.getAddress();
		this.postalCode = account.getPostalCode();
	}

	public UUID getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public EnumGender getGender() {
		return gender;
	}

	public long getRegistrationDateMils() {
		return registrationDateMils;
	}

	public String getCountry() {
		return country;
	}

	public String getCity() {
		return city;
	}

	public String getAddress() {
		return address;
	}

	public String getPostalCode() {
		return postalCode;
	}
	
}