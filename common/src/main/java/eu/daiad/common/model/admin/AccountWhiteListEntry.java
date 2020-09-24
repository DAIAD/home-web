package eu.daiad.common.model.admin;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import eu.daiad.common.model.EnumGender;

public class AccountWhiteListEntry {

	private int id;

	private int utilityId;

	private Integer accountId;

	private String username;

	private DateTime registeredOn;

	private String locale;

	private String meterSerial;

	private Geometry meterLocation;

	private String firstname;

	private String lastname;

	private String timezone;

	private String country;

	private String city;

	private String address;

	private String postalCode;

	private DateTime birthdate;

	private EnumGender gender;

	private int defaultMobileMode;

	private int defaultWebMode;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUtilityId() {
		return utilityId;
	}

	public void setUtilityId(int utilityId) {
		this.utilityId = utilityId;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public DateTime getRegisteredOn() {
		return registeredOn;
	}

	public void setRegisteredOn(DateTime registeredOn) {
		this.registeredOn = registeredOn;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getMeterSerial() {
		return meterSerial;
	}

	public void setMeterSerial(String meterSerial) {
		this.meterSerial = meterSerial;
	}

	public Geometry getMeterLocation() {
		return meterLocation;
	}

	public void setMeterLocation(Geometry meterLocation) {
		this.meterLocation = meterLocation;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public DateTime getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(DateTime birthdate) {
		this.birthdate = birthdate;
	}

	public EnumGender getGender() {
		return gender;
	}

	public void setGender(EnumGender gender) {
		this.gender = gender;
	}

	public int getDefaultMobileMode() {
		return defaultMobileMode;
	}

	public void setDefaultMobileMode(int defaultMobileMode) {
		this.defaultMobileMode = defaultMobileMode;
	}

	public int getDefaultWebMode() {
		return defaultWebMode;
	}

	public void setDefaultWebMode(int defaultWebMode) {
		this.defaultWebMode = defaultWebMode;
	}

}
