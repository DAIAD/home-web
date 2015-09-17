package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

public class Profile {

	private UUID key;

	private String firstname;

	private String lastname;

	private String timezone;

	private String country;

	private String postalCode;

	private ArrayList<DeviceRegistration> devices;

	public Profile() {
		this.devices = new ArrayList<DeviceRegistration>();
	}

	public UUID getKey() {
		return key;
	}

	public void setKey(UUID key) {
		this.key = key;
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

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public ArrayList<DeviceRegistration> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<DeviceRegistration> devices) {
		this.devices = devices;
		if (this.devices == null) {
			this.devices = new ArrayList<DeviceRegistration>();
		}
	}
}
