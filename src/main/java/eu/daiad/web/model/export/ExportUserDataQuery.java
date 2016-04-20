package eu.daiad.web.model.export;

import java.util.ArrayList;
import java.util.UUID;

public class ExportUserDataQuery {

	private UUID userKey;

	private String username;

	private ArrayList<UUID> amphiroKeys = new ArrayList<UUID>();

	private ArrayList<String> amphiroNames = new ArrayList<String>();

	private ArrayList<UUID> meterKeys = new ArrayList<UUID>();

	private ArrayList<String> meterNames = new ArrayList<String>();

	private Long startDateTime;

	private Long endDateTime;

	private String timezone;

	public UUID getUserKey() {
		return userKey;
	}

	public void setUserKey(UUID userKey) {
		this.userKey = userKey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public ArrayList<UUID> getAmphiroKeys() {
		return amphiroKeys;
	}

	public ArrayList<String> getAmphiroNames() {
		return amphiroNames;
	}

	public ArrayList<UUID> getMeterKeys() {
		return meterKeys;
	}

	public ArrayList<String> getMeterNames() {
		return meterNames;
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

}
