package eu.daiad.web.security.model;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.EnumGender;
import eu.daiad.web.util.GenderDeserializer;

public class UserRegistrationRequest {

	private String username;
	
	private String password;

    private String firstname;

    private String lastname;
    
    @JsonDeserialize(using = GenderDeserializer.class)
    private EnumGender gender;

	private DateTime birthdate;
    
    private String country;

    private String timezone;
    
    private String postalCode;
	
    public EnumGender getGender() {
    	if (this.gender == null) {
    		return EnumGender.UNDEFINED;
    	}
		return gender;
	}

	public void setGender(EnumGender gender) {
		this.gender = gender;
	}

	public DateTime getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(DateTime birthdate) {
		this.birthdate = birthdate;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPostalCode() {
		return postalCode;
	}

	@JsonSetter("zip")
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
    
	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public void setUsername(String value) {
		this.username = value;
	}

	public String getUsername() {
		return this.username;
	}
	
	public void setPassword(String value) {
		this.password = value;
	}

	public String getPassword() {
		return this.password;
	}
	
    public String getFirstname() {
        return firstname;
    }
 
    public void setFirstname(String firstName) {
        this.firstname = firstName;
    }
 
    public String getLastname() {
        return lastname;
    }
 
    public void setLastname(String lastName) {
        this.lastname = lastName;
    }
}