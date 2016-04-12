package eu.daiad.web.model.export;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;

public class ExportUserDataRequest extends AuthenticatedRequest {

	@JsonDeserialize(using = EnumExportDataSource.Deserializer.class)
	private EnumExportDataSource type;

	private UUID userKey;

	private UUID[] deviceKeys;

	private Long startDateTime;

	private Long endDateTime;

	private String timezone;

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

	public EnumExportDataSource getType() {
		return type;
	}

}
