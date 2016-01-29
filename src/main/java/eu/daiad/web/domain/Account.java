package eu.daiad.web.domain;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.net.util.Base64;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.web.model.EnumGender;

@Entity(name = "account")
@Table(schema = "public", name = "account")
public class Account {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_id_seq", name = "account_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "utility_id", nullable = false)
	private Utility utility;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "account_id")
	private Set<AccountRole> roles = new HashSet<AccountRole>();

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private Set<Device> devices = new HashSet<Device>();

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

	@Column(name = "postal_code")
	private String postalCode;

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime birthdate;

	@Enumerated(EnumType.STRING)
	private EnumGender gender;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Utility getUtility() {
		return utility;
	}

	public void setUtility(Utility utility) {
		this.utility = utility;
	}

	public Set<Device> getDevices() {
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

	public Set<AccountRole> getRoles() {
		return roles;
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

}
