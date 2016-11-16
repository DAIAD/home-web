package eu.daiad.web.model.export;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;

/**
 * Represents a request for exporting data.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = UserDataExportRequest.class, name = "USER") })
public abstract class DataExportRequest extends AuthenticatedRequest {

    /**
     * Export type.
     */
    @JsonDeserialize(using = EnumDataExportRequestType.Deserializer.class)
    private EnumDataExportRequestType type;

    /**
     * Exported data reference time zone.
     * 
     * If no time zone is specified, the system will assign the time zone of the
     * user whose data is being exported. If the user time zone is not set, the
     * time zone of the user's utility will be used.
     * 
     * If the time zone can not be resolved, an exception will be thrown.
     */
    private String timezone;

    /**
     * Optional time interval start time stamp.
     */
    private Long startDateTime;

    /**
     * Optional time interval end time stamp.
     */
    private Long endDateTime;

    public Long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Long getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Long endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public abstract EnumDataExportRequestType getType();
}
