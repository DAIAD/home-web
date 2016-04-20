package eu.daiad.web.model.admin;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountActivity {

	private UUID key;

	private Integer id;

	private Integer utilityId;

	private String utilityName;

	private Integer accountId;

	private String username;

	private String firstName;

	private String lastName;

	private Long accountRegisteredOn;

	private Long lastLoginSuccess;

	private Long lastLoginFailure;

	private Long numberOfAmphiroDevices;

	private Long numberOfMeters;

	private Long leastAmphiroRegistration;

	private Long leastMeterRegistration;

	private Long lastDataUploadSuccess;

	private Long lastDataUploadFailure;

	private BigDecimal transmissionCount;

	private BigDecimal transmissionIntervalSum;

	private Integer transmissionIntervalMax;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

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

	public Long getAccountRegisteredOn() {
		return accountRegisteredOn;
	}

	public void setAccountRegisteredOn(Long accountRegisteredOn) {
		this.accountRegisteredOn = accountRegisteredOn;
	}

	public Long getLastLoginSuccess() {
		return lastLoginSuccess;
	}

	public void setLastLoginSuccess(Long lastLoginSuccess) {
		this.lastLoginSuccess = lastLoginSuccess;
	}

	public Long getLastLoginFailure() {
		return lastLoginFailure;
	}

	public void setLastLoginFailure(Long lastLoginFailure) {
		this.lastLoginFailure = lastLoginFailure;
	}

	public Long getLastDataUploadSuccess() {
		return lastDataUploadSuccess;
	}

	public void setLastDataUploadSuccess(Long lastDataUploadSuccess) {
		this.lastDataUploadSuccess = lastDataUploadSuccess;
	}

	public Long getLastDataUploadFailure() {
		return lastDataUploadFailure;
	}

	public void setLastDataUploadFailure(Long lastDataUploadFailure) {
		this.lastDataUploadFailure = lastDataUploadFailure;
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

	public Long getLeastAmphiroRegistration() {
		return leastAmphiroRegistration;
	}

	public void setLeastAmphiroRegistration(Long leastAmphiroRegistration) {
		this.leastAmphiroRegistration = leastAmphiroRegistration;
	}

	public Long getLeastMeterRegistration() {
		return leastMeterRegistration;
	}

	public void setLeastMeterRegistration(Long leastMeterRegistration) {
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
