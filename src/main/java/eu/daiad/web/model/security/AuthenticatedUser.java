package eu.daiad.web.model.security;

import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import eu.daiad.web.model.EnumGender;
import eu.daiad.web.model.profile.EnumMobileMode;
import eu.daiad.web.model.profile.EnumUtilityMode;
import eu.daiad.web.model.profile.EnumWebMode;

public class AuthenticatedUser extends User {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private UUID key;

    private int id;

    private UUID utilityKey;

    private int utilityId;

    private DateTime createdOn;

    private String firstname;

    private String lastname;

    private EnumGender gender;

    private DateTime birthdate;

    private String locale;

    private String country;

    private String postalCode;

    private String timezone;

    private EnumWebMode webMode = EnumWebMode.INACTIVE;

    private EnumMobileMode mobileMode = EnumMobileMode.INACTIVE;

    private EnumUtilityMode utilityMode = EnumUtilityMode.INACTIVE;

    private boolean allowPasswordReset = false;

    public AuthenticatedUser(int id, UUID key, String username, String password, int utilityId, UUID utilityKey,
                    boolean isLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, true, true, true, !isLocked, authorities);

        this.id = id;
        this.utilityId = utilityId;
        this.utilityKey = utilityKey;

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

    public String getFullname() {
        if ((!StringUtils.isBlank(firstname)) && (!StringUtils.isBlank(lastname))) {
            return String.format("%s , %s", lastname, firstname);
        } else if (!StringUtils.isBlank(lastname)) {
            return lastname;
        } else if (!StringUtils.isBlank(firstname)) {
            return firstname;
        }

        return null;
    }

    public UUID getKey() {
        return this.key;
    }

    public EnumGender getGender() {
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

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public boolean hasRole(String role) {
        return this.getAuthorities().contains(new SimpleGrantedAuthority(role));
    }

    public boolean hasRole(EnumRole role) {
        return this.getAuthorities().contains(new SimpleGrantedAuthority(role.toString()));
    }

    public EnumWebMode getWebMode() {
        return webMode;
    }

    public void setWebMode(EnumWebMode webMode) {
        this.webMode = webMode;
    }

    public EnumMobileMode getMobileMode() {
        return mobileMode;
    }

    public void setMobileMode(EnumMobileMode mobileMode) {
        this.mobileMode = mobileMode;
    }

    public EnumUtilityMode getUtilityMode() {
        return utilityMode;
    }

    public void setUtilityMode(EnumUtilityMode utilityMode) {
        this.utilityMode = utilityMode;
    }

    public int getUtilityId() {
        return utilityId;
    }

    public int getId() {
        return id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public UUID getUtilityKey() {
        return utilityKey;
    }

    public boolean isAllowPasswordReset() {
        return allowPasswordReset;
    }

    public void setAllowPasswordReset(boolean allowPasswordReset) {
        this.allowPasswordReset = allowPasswordReset;
    }
}