package eu.daiad.common.model.utility;

import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

import eu.daiad.common.domain.application.UtilityEntity;

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

    public boolean isMessageGenerationEnabled() {
        return messageGenerationEnabled;
    }

    public Geometry getCenter() {
        return center;
    }

}