package eu.daiad.web.model.utility;

import java.util.UUID;

import eu.daiad.web.domain.application.Utility;

public class UtilityInfo {

	private int id;
	private UUID key;
	private String name;
	private String country;
	private String timezone;
	private String locale;
	private String city;

	public UtilityInfo(Utility utility) {

		this.id = utility.getId();
		this.key = utility.getKey();
		this.name = utility.getName();
		this.country = utility.getCountry();
		this.timezone = utility.getTimezone();
		this.locale = utility.getLocale();
		this.city = utility.getCity();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCountry() {
		return country;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getLocale() {
		return locale;
	}

	public String getCity() {
		return city;
	}

	public UUID getKey() {
		return key;
	}

	public void setKey(UUID key) {
		this.key = key;
	}

}