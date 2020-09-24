package eu.daiad.utility.service.etl;

import java.util.UUID;

/**
 * Represents a query for selecting user data to export.
 */
public class UserDataExportQuery extends DataExportQuery {

    /**
     * Unique key (UUID) of the user whose data is being exported.
     */
	private UUID userKey;

	/**
	 * Array of unique device keys (UUID) of the devices whose data is being exported.
	 */
	private UUID[] deviceKeys;

    public UUID getUserKey() {
        return userKey;
    }

    public void setUserKey(UUID userKey) {
        this.userKey = userKey;
    }

    public UUID[] getDeviceKeys() {
        return deviceKeys;
    }

    public void setDeviceKeys(UUID[] deviceKeys) {
        this.deviceKeys = deviceKeys;
    }

}
