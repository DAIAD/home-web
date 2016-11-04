package eu.daiad.web.domain.application;

import java.math.BigDecimal;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "trial_account_activity")
@Table(schema = "public", name = "trial_account_activity")
public class AccountActivityEntity {

	@Id()
	@Column(name = "id")
	private Integer id;

	@Column()
	@Type(type = "pg-uuid")
	private UUID key;

	@Column(name = "utility_id")
	private Integer utilityId;

	@Column(name = "utility_name")
	private String utilityName;

	@Column(name = "account_id")
	private Integer accountId;

	@Basic
	private String username;

	@Column(name = "firstname")
	private String firstName;

	@Column(name = "lastname")
	private String lastName;

	@Column(name = "signup_date")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime accountRegisteredOn;

	@Column(name = "last_login_success")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime lastLoginSuccess;

	@Column(name = "last_login_failure")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime lastLoginFailure;

	@Column(name = "amphiro_count")
	private Long numberOfAmphiroDevices;

	@Column(name = "meter_count")
	private Long numberOfMeters;

	@Column(name = "amphiro_last_registration")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime leastAmphiroRegistration;

	@Column(name = "meter_last_registration")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime leastMeterRegistration;

	@Column(name = "device_last_upload_success")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime lastDataUploadSuccess;

	@Column(name = "device_last_upload_failure")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime lastDataUploadFailure;

	@Column(name = "transmission_count")
	private BigDecimal transmissionCount;

	@Column(name = "transmission_interval_sum")
	private BigDecimal transmissionIntervalSum;

	@Column(name = "transmission_interval_max")
	private Integer transmissionIntervalMax;

	public Integer getUtilityId() {
		return utilityId;
	}

	public void setUtilityId(Integer utilityId) {
		this.utilityId = utilityId;
	}

	public String getUtilityName() {
		return utilityName;
	}

	public void setUtilityName(String utilityName) {
		this.utilityName = utilityName;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public DateTime getAccountRegisteredOn() {
		return accountRegisteredOn;
	}

	public void setAccountRegisteredOn(DateTime accountRegisteredOn) {
		this.accountRegisteredOn = accountRegisteredOn;
	}

	public DateTime getLastLoginSuccess() {
		return lastLoginSuccess;
	}

	public void setLastLoginSuccess(DateTime lastLoginSuccess) {
		this.lastLoginSuccess = lastLoginSuccess;
	}

	public DateTime getLastLoginFailure() {
		return lastLoginFailure;
	}

	public void setLastLoginFailure(DateTime lastLoginFailure) {
		this.lastLoginFailure = lastLoginFailure;
	}

	public DateTime getLastDataUploadSuccess() {
		return lastDataUploadSuccess;
	}

	public void setLastDataUploadSuccess(DateTime lastDataUploadSuccess) {
		this.lastDataUploadSuccess = lastDataUploadSuccess;
	}

	public DateTime getLastDataUploadFailure() {
		return lastDataUploadFailure;
	}

	public void setLastDataUploadFailure(DateTime lastDataUploadFailure) {
		this.lastDataUploadFailure = lastDataUploadFailure;
	}

	public Integer getId() {
		return id;
	}

	public UUID getKey() {
		return key;
	}

	public void setKey(UUID key) {
		this.key = key;
	}

	public Long getNumberOfAmphiroDevices() {
		return numberOfAmphiroDevices;
	}

	public void setNumberOfAmphiroDevices(Long numberOfAmphiroDevices) {
		this.numberOfAmphiroDevices = numberOfAmphiroDevices;
	}

	public Long getNumberOfMeters() {
		return numberOfMeters;
	}

	public void setNumberOfMeters(Long numberOfMeters) {
		this.numberOfMeters = numberOfMeters;
	}

	public DateTime getLeastAmphiroRegistration() {
		return leastAmphiroRegistration;
	}

	public void setLeastAmphiroRegistration(DateTime leastAmphiroRegistration) {
		this.leastAmphiroRegistration = leastAmphiroRegistration;
	}

	public DateTime getLeastMeterRegistration() {
		return leastMeterRegistration;
	}

	public void setLeastMeterRegistration(DateTime leastMeterRegistration) {
		this.leastMeterRegistration = leastMeterRegistration;
	}

	public BigDecimal getTransmissionCount() {
		return transmissionCount;
	}

	public void setTransmissionCount(BigDecimal transmissionCount) {
		this.transmissionCount = transmissionCount;
	}

	public BigDecimal getTransmissionIntervalSum() {
		return transmissionIntervalSum;
	}

	public void setTransmissionIntervalSum(BigDecimal transmissionIntervalSum) {
		this.transmissionIntervalSum = transmissionIntervalSum;
	}

	public Integer getTransmissionIntervalMax() {
		return transmissionIntervalMax;
	}

	public void setTransmissionIntervalMax(Integer transmissionIntervalMax) {
		this.transmissionIntervalMax = transmissionIntervalMax;
	}

}
