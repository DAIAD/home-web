package eu.daiad.web.model.profile;

import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.EnumGender;
import eu.daiad.web.model.device.DeviceRegistration;
import eu.daiad.web.model.utility.UtilityInfo;

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

    private String address;

    private DateTime birthdate;

    @JsonDeserialize(using = EnumGender.Deserializer.class)
    private EnumGender gender;

    @JsonProperty("zip")
    private String postalCode;

    private int mode = 0;

    private String configuration;

    private Integer dailyMeterBudget;

    private Integer dailyAmphiroBudget;

    private ArrayList<DeviceRegistration> devices;

    private UtilityInfo utility;

    private Household household;

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

    public Integer getDailyMeterBudget() {
        return dailyMeterBudget;
    }

    public void setDailyMeterBudget(Integer dailyMeterBudget) {
        this.dailyMeterBudget = dailyMeterBudget;
    }

    public Integer getDailyAmphiroBudget() {
        return dailyAmphiroBudget;
    }

    public void setDailyAmphiroBudget(Integer dailyAmphiroBudget) {
        this.dailyAmphiroBudget = dailyAmphiroBudget;
    }

    public UtilityInfo getUtility() {
        return utility;
    }

    public void setUtility(UtilityInfo utility) {
        this.utility = utility;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

}
