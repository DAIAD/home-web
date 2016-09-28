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
import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.model.EnumGender;

public class Account {

    public interface AccountDefaultValidation {

    }

    public interface AccountSimpleValidation {

    }

    @NotEmpty(groups = { AccountSimpleValidation.class, AccountDefaultValidation.class })
    @Size(max = 100, groups = { AccountSimpleValidation.class, AccountDefaultValidation.class })
    @Email(groups = { AccountSimpleValidation.class, AccountDefaultValidation.class })
    private String username;

    @NotEmpty(groups = { AccountSimpleValidation.class, AccountDefaultValidation.class })
    @Size(min = 8, groups = { AccountSimpleValidation.class, AccountDefaultValidation.class })
    private String password;

    @NotEmpty(groups = { AccountDefaultValidation.class })
    @Size(max = 40, groups = { AccountDefaultValidation.class })
    private String firstname;

    @NotEmpty(groups = { AccountDefaultValidation.class })
    @Size(max = 70, groups = { AccountDefaultValidation.class })
    private String lastname;

    @JsonDeserialize(using = EnumGender.Deserializer.class)
    @NotNull(groups = { AccountDefaultValidation.class })
    private EnumGender gender;

    @NotNull(groups = { AccountDefaultValidation.class })
    @Past(groups = { AccountDefaultValidation.class })
    private DateTime birthdate;

    @NotNull(groups = { AccountDefaultValidation.class })
    @Size(max = 50, groups = { AccountDefaultValidation.class })
    private String country;

    @Size(max = 60, groups = { AccountDefaultValidation.class })
    private String city;

    @Size(max = 90, groups = { AccountDefaultValidation.class })
    private String address;

    @NotNull(groups = { AccountDefaultValidation.class })
    @Size(max = 50, groups = { AccountDefaultValidation.class })
    private String timezone;

    @NotNull(groups = { AccountDefaultValidation.class })
    @Size(max = 10, groups = { AccountDefaultValidation.class })
    private String postalCode;

    @NotNull(groups = { AccountDefaultValidation.class })
    @NotEmpty(groups = { AccountDefaultValidation.class })
    @Pattern(regexp = "en|el|es|de", groups = { AccountDefaultValidation.class })
    private String locale;

    private Geometry location;

    private byte[] photo;

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

    public Geometry getLocation() {
        return location;
    }

    public void setLocation(Geometry location) {
        this.location = location;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

}
