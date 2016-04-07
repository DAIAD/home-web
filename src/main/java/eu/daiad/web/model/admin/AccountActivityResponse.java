package eu.daiad.web.model.admin;

import java.util.ArrayList;
import java.util.UUID;

import eu.daiad.web.model.RestResponse;

public class AccountActivityResponse extends RestResponse {

	public static class AccountActivity {

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

		private Long numberOfDevices;

		private Long leastDeviceRegistration;

		private Long lastDataUploadSuccess;

		private Long lastDataUploadFailure;

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

		public Long getNumberOfDevices() {
			return numberOfDevices;
		}

		public void setNumberOfDevices(Long numberOfDevices) {
			this.numberOfDevices = numberOfDevices;
		}

		public Long getLeastDeviceRegistration() {
			return leastDeviceRegistration;
		}

		public void setLeastDeviceRegistration(Long leastDeviceRegistration) {
			this.leastDeviceRegistration = leastDeviceRegistration;
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

	}

	private ArrayList<AccountActivity> accounts = new ArrayList<AccountActivity>();

	public ArrayList<AccountActivity> getAccounts() {
		return accounts;
	}

}
