package eu.daiad.web.domain;

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

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.web.model.EnumGender;

@Entity(name = "account_white_list")
@Table(schema = "public", name = "account_white_list")
public class AccountWhiteListEntry {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_white_list_id_seq", name = "account_white_list_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_white_list_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "utility_id", nullable = false)
	private Utility utility;

	@OneToOne()
	@JoinColumn(name = "account_id", nullable = true)
	private Account account;

	@Basic()
	private String username;

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "registered_on")
	private DateTime registeredOn;

	@Column(name = "locale", columnDefinition = "bpchar", length = 2)
	private String locale;

	@Basic()
	private String firstname;

	@Basic()
	private String lastname;

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

	@Column(name = "default_mobile_mode")
	private int defaultMobileMode;

	@Column(name = "default_web_mode")
	private int defaultWebMode;

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
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

	public Utility getUtility() {
		return utility;
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

}
