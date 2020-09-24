package eu.daiad.common.model.export;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.common.model.AuthenticatedRequest;

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

    /**
     * Enumeration of supported data export request types.
     */
    public enum EnumDataExportRequestType {
        /**
         * Export type is missing or is invalid.
         */
        UNDEFINED(0),
        /**
         * Export all data for a single user.
         */
        USER(1);

        private final int value;

        private EnumDataExportRequestType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static EnumDataExportRequestType fromString(String value) {
            for (EnumDataExportRequestType item : EnumDataExportRequestType.values()) {
                if (item.name().equalsIgnoreCase(value)) {
                    return item;
                }
            }
            return EnumDataExportRequestType.UNDEFINED;
        }

        public static class Deserializer extends JsonDeserializer<EnumDataExportRequestType> {

            @Override
            public EnumDataExportRequestType deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
                return EnumDataExportRequestType.fromString(parser.getValueAsString());
            }
        }

    }
}
