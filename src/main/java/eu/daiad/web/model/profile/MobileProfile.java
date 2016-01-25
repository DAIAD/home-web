package eu.daiad.web.model.profile;

public class MobileProfile extends Profile {

	private String postalCode;

	private long lastSyncTimestamp;

	private ProfileHousehold household;

	public MobileProfile() {
		super();
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public long getLastSyncTimestamp() {
		return lastSyncTimestamp;
	}

	public void setLastSyncTimestamp(long lastSyncTimestamp) {
		this.lastSyncTimestamp = lastSyncTimestamp;
	}

	public ProfileHousehold getHousehold() {
		return household;
	}

	public void setHousehold(ProfileHousehold household) {
		this.household = household;
	}
}
