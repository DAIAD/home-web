package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.util.Base64;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.model.EnumGender;

@Entity(name = "account")
@Table(schema = "public", name = "account")
public class AccountEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "account_id_seq", name = "account_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "account_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

    @Version()
    @Column(name = "row_version")
    private long rowVersion;

    @ManyToOne()
    @JoinColumn(name = "utility_id", nullable = false)
    private UtilityEntity utility;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Set<AccountRoleEntity> roles = new HashSet<>();

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Set<AccountUtilityEntity> utilities = new HashSet<>();

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Set<DeviceEntity> devices = new HashSet<>();

    @OneToOne(mappedBy = "account")
    @JoinColumn(name = "account_id")
    private AccountWhiteListEntity whiteListEntry;

    @OneToOne(mappedBy = "account")
    private AccountProfileEntity profile;

    @OneToOne(mappedBy = "account")
    private HouseholdEntity household;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Set<WaterIqHistoryEntity> waterIqHistory = new HashSet<>();

    @Basic(fetch = FetchType.LAZY)
    @Type(type = "org.hibernate.type.BinaryType")
    private byte photo[];

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Column(name = "last_login_success")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastLoginSuccessOn;

    @Column(name = "last_login_failure")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lastLoginFailureOn;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Basic()
    private String firstname;

    @Basic()
    private String lastname;

    @Basic()
    private String email;

    @Column(name = "change_password_on_login")
    private boolean changePasswordOnNextLogin;

    @Column(name = "allow_password_reset")
    private boolean allowPasswordReset;

    @Basic()
    private boolean locked;

    @Basic()
    private String username;

    @Basic()
    private String password;

    @Column()
    @Type(type = "pg-uuid")
    private UUID key = UUID.randomUUID();

    @Basic()
    private String timezone;

    @Basic()
    private String country;

    @Basic()
    private String city;

    @Basic()
    private String address;

    @Column(name = "postal_code")
    private String postalCode;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime birthdate;

    @Enumerated(EnumType.STRING)
    private EnumGender gender;

    @Column(name = "locale", columnDefinition = "bpchar", length = 2)
    private String locale;

    @Column(name = "location")
    private Geometry location;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UtilityEntity getUtility() {
        return utility;
    }

    public UUID getUtilityKey() {
        return utility.getKey();
    }
    
    public void setUtility(UtilityEntity utility) {
        this.utility = utility;
    }

    public Set<DeviceEntity> getDevices() {
        return devices;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String photoToBase64() {
        if (photo != null) {
            return new String(Base64.encodeBase64(photo));
        }
        return "";
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public DateTime getLastLoginSuccessOn() {
        return lastLoginSuccessOn;
    }

    public void setLastLoginSuccessOn(DateTime lastLoginSuccessOn) {
        this.lastLoginSuccessOn = lastLoginSuccessOn;
    }

    public DateTime getLastLoginFailureOn() {
        return lastLoginFailureOn;
    }

    public void setLastLoginFailureOn(DateTime lastLoginFailureOn) {
        this.lastLoginFailureOn = lastLoginFailureOn;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isChangePasswordOnNextLogin() {
        return changePasswordOnNextLogin;
    }

    public void setChangePasswordOnNextLogin(boolean changePasswordOnNextLogin) {
        this.changePasswordOnNextLogin = changePasswordOnNextLogin;
    }

    public boolean isAllowPasswordReset() {
        return allowPasswordReset;
    }

    public void setAllowPasswordReset(boolean allowPasswordReset) {
        this.allowPasswordReset = allowPasswordReset;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UUID getKey() {
        return key;
    }

    public Set<AccountRoleEntity> getRoles() {
        return roles;
    }

    public Set<AccountUtilityEntity> getUtilities() {
        return utilities;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public AccountProfileEntity getProfile() {
        return profile;
    }

    public void setProfile(AccountProfileEntity profile) {
        this.profile = profile;
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

    public long getRowVersion() {
        return rowVersion;
    }

    public Geometry getLocation() {
        return location;
    }

    public void setLocation(Geometry location) {
        this.location = location;
    }

    public HouseholdEntity getHousehold() {
        return household;
    }

    public void setHousehold(HouseholdEntity household) {
        this.household = household;
    }

    public Set<WaterIqHistoryEntity> getWaterIqHistory() {
        return waterIqHistory;
    }

    public String getFullname() {
        String fullname = (StringUtils.isBlank(firstname) ? "" : firstname) + " "
                        + (StringUtils.isBlank(lastname) ? "" : lastname);

        if (StringUtils.isBlank(fullname)) {
            return null;
        }

        return fullname.trim();
    }

}
