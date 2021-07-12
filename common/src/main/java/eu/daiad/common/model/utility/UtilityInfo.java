package eu.daiad.common.model.utility;

import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

import eu.daiad.common.domain.application.UtilityEntity;
import lombok.Getter;

@Getter
public class UtilityInfo {

	private int id;
	private UUID key;
	private String name;
	private String country;
	private String timezone;
	private String locale;
	private String city;
	private boolean messageGenerationEnabled;
	private Geometry center;
	private byte[] logo;

	public UtilityInfo(UtilityEntity utility) {

		id = utility.getId();
		key = utility.getKey();
		name = utility.getName();
		country = utility.getCountry();
		timezone = utility.getTimezone();
		locale = utility.getLocale();
		city = utility.getCity();
		messageGenerationEnabled = utility.isMessageGenerationEnabled();
		center = utility.getCenter();
		logo = utility.getLogo();
	}

}