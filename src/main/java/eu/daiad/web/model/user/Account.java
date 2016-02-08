package eu.daiad.web.model.user;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.EnumGender;
import eu.daiad.web.util.GenderDeserializer;

public class Account {

	@NotEmpty
	@Size(max = 100)
	@Email
	private String username;

	@NotEmpty
	@Size(min = 8)
	private String password;

	@NotEmpty
	@Size(max = 40)
	private String firstname;

	@NotEmpty
	@Size(max = 70)
	private String lastname;

	@JsonDeserialize(using = GenderDeserializer.class)
	@NotNull
	private EnumGender gender;

	@NotNull
	@Past
	private DateTime birthdate;

	@NotNull
	@Size(max = 50)
	private String country;

	@NotNull
	@Size(max = 50)
	private String timezone;

	@NotNull
	@Size(max = 10)
	private String postalCode;

	@NotNull
	@NotEmpty
	@Pattern(regexp = "en|el|es|de")
	private String locale;

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

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

}
