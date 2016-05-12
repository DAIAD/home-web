package eu.daiad.web.model.profile;

import java.util.ArrayList;
import java.util.UUID;

import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.device.DeviceRegistration;

public class Profile {

	private UUID key;

	private UUID version;

	private EnumApplication application;

	private String username;

	private String firstname;

	private String lastname;

	private String email;

	private byte[] photo;

	private String locale;

	private String timezone;

	private String country;

	private int mode = 0;

	private String configuration;

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

	public ArrayList<DeviceRegistration> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<DeviceRegistration> devices) {
		this.devices = devices;
		if (this.devices == null) {
			this.devices = new ArrayList<DeviceRegistration>();
		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public EnumApplication getApplication() {
		return application;
	}

	public void setApplication(EnumApplication application) {
		this.application = application;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public UUID getVersion() {
		return version;
	}

	public void setVersion(UUID version) {
		this.version = version;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

}
