package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import eu.daiad.web.model.EnumGender;

@Entity(name = "account_white_list")
@Table(schema = "public", name = "account_white_list")
public class AccountWhiteListEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_white_list_id_seq", name = "account_white_list_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_white_list_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@Version()
	@Column(name = "row_version")
	private long rowVersion;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "utility_id", nullable = false)
	private UtilityEntity utility;

	@OneToOne()
	@JoinColumn(name = "account_id", nullable = true)
	private AccountEntity account;

	@Basic()
	private String username;

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "registered_on")
	private DateTime registeredOn;

	@Column(name = "locale", columnDefinition = "bpchar", length = 2)
	private String locale;

	@Column(name = "meter_serial")
	private String meterSerial;

	@Column(name = "meter_location")
	private Geometry meterLocation;

	@Basic()
	private String firstname;

	@Basic()
	private String lastname;

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

	@Column(name = "default_mobile_mode")
	private int defaultMobileMode;

	@Column(name = "default_web_mode")
	private int defaultWebMode;

	@Column(name = "location")
	private Geometry location;

	public AccountWhiteListEntity() {

	}

	public AccountWhiteListEntity(String username) {
		this();
		this.username = username;
	}

	public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public String getUsername() {
		return username;
	}

	public DateTime getRegisteredOn() {
		return registeredOn;
	}

	public void setRegisteredOn(DateTime registeredOn) {
		this.registeredOn = registeredOn;
	}

	public int getId() {
		return id;
	}

	public UtilityEntity getUtility() {
		return utility;
	}

	public void setUtility(UtilityEntity utility) {
		this.utility = utility;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
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

	public int getDefaultMobileMode() {
		return defaultMobileMode;
	}

	public void setDefaultMobileMode(int defaultMobileMode) {
		this.defaultMobileMode = defaultMobileMode;
	}

	public int getDefaultWebMode() {
		return defaultWebMode;
	}

	public void setDefaultWebMode(int defaultWebMode) {
		this.defaultWebMode = defaultWebMode;
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

	public String getMeterSerial() {
		return meterSerial;
	}

	public void setMeterSerial(String meterSerial) {
		this.meterSerial = meterSerial;
	}

	public Geometry getMeterLocation() {
		return meterLocation;
	}

	public void setMeterLocation(Geometry meterLocation) {
		this.meterLocation = meterLocation;
	}

	public Geometry getLocation() {
		return location;
	}

	public void setLocation(Geometry location) {
		this.location = location;
	}

}
