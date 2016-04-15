package eu.daiad.web.model.utility;

import eu.daiad.web.domain.application.Utility;

public class UtilityInfo {
	
	private int id;
	private String name;
	private String country;
	private String timezone;
	private String locale;
	private String city;
	
	public UtilityInfo (Utility utility) {
		this.id = utility.getId();
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
	
}