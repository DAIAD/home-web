package eu.daiad.web.model.profile;

import java.util.UUID;

import org.joda.time.DateTime;

/**
 * Represents a profile history record.
 */
public class ProfileHistoryEntry {

    /**
     * History entry unique id.
     */
    private int id;

    /**
     * Profile version.
     */
    private UUID version;

    /**
     * Date and time the profile has been updated.
     */
    private DateTime updatedOn;

    /**
     * Date and time the client has received the new profile.
     */
    private DateTime acknowledgedOn;

    /**
     * Date and time the client has applied the new profile.
     */
    private DateTime enabledOn;

    /**
     * Mobile application mode.
     */
    private EnumMobileMode mobileMode;

    /**
     * Web application mode.
     */
    private EnumWebMode webMode;

    /**
     * Utility application mode.
     */
    private EnumUtilityMode utilityMode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getVersion() {
        return version;
    }

    public void setVersion(UUID version) {
        this.version = version;
    }

    public DateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(DateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public DateTime getAcknowledgedOn() {
        return acknowledgedOn;
    }

    public void setAcknowledgedOn(DateTime acknowledgedOn) {
        this.acknowledgedOn = acknowledgedOn;
    }

    public DateTime getEnabledOn() {
        return enabledOn;
    }

    public void setEnabledOn(DateTime enabledOn) {
        this.enabledOn = enabledOn;
    }

    public EnumMobileMode getMobileMode() {
        return mobileMode;
    }

    public void setMobileMode(EnumMobileMode mobileMode) {
        this.mobileMode = mobileMode;
    }

    public EnumWebMode getWebMode() {
        return webMode;
    }

    public void setWebMode(EnumWebMode webMode) {
        this.webMode = webMode;
    }

    public EnumUtilityMode getUtilityMode() {
        return utilityMode;
    }

    public void setUtilityMode(EnumUtilityMode utilityMode) {
        this.utilityMode = utilityMode;
    }

}
