package eu.daiad.web.model.user;

import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.model.EnumGender;

public class UserInfo {

    private UUID id;

    private String firstName;

    private String lastName;

    private String fullname;

    private String email;

    private EnumGender gender;

    private long registrationDateMils;

    private String country;

    private String city;

    private String address;

    private String postalCode;

    private Geometry location;

    private DeviceMeterInfo meter;

    private boolean favorite;

    public UserInfo(eu.daiad.web.domain.application.Account account) {
        this.id = account.getKey();
        this.firstName = account.getFirstname();
        this.lastName = account.getLastname();
        this.email = account.getUsername();
        this.gender = account.getGender();
        this.registrationDateMils = account.getCreatedOn().getMillis();
        this.country = account.getCountry();
        this.city = account.getCity();
        this.address = account.getAddress();
        this.postalCode = account.getPostalCode();
        this.fullname = account.getFullname();
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public EnumGender getGender() {
        return gender;
    }

    public long getRegistrationDateMils() {
        return registrationDateMils;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public Geometry getLocation() {
        return location;
    }

    public void setLocation(Geometry location) {
        this.location = location;
    }

    public DeviceMeterInfo getMeter() {
        return meter;
    }

    public void setMeter(DeviceMeterInfo meter) {
        this.meter = meter;
    }

    public String getFullname() {
        return fullname;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

}