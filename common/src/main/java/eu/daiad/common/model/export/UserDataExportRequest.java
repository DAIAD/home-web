package eu.daiad.common.model.export;

import java.util.UUID;

/**
 * Represents a data export request for the data of a specific user.
 */
public class UserDataExportRequest extends DataExportRequest {

    /**
     * Unique key (UUID) of the user whose data is being exported.
     */
    private UUID userKey;

    /**
     * Optionally request data only for specific devices. If no device key is
     * selected, data for all devices is exported.
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

    @Override
    public EnumDataExportRequestType getType() {
        return EnumDataExportRequestType.USER;
    }

}
