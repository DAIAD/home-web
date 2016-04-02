package eu.daiad.web.model.export;

import java.util.ArrayList;

import eu.daiad.web.model.AuthenticatedRequest;

public class ExportDataRequest extends AuthenticatedRequest {

	private EnumExportDataSource type;

	private long from;

	private long to;

	private String username;

	private ArrayList<String> properties;

	private String timezone;

	public EnumExportDataSource getType() {
		return type;
	}

	public void setType(EnumExportDataSource type) {
		this.type = type;
	}

	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public ArrayList<String> getProperties() {
		return properties;
	}

	public void setProperties(ArrayList<String> properties) {
		this.properties = properties;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	@Override
	public String toString() {
		return "ExportData [type=" + type + ", from=" + from + ", to=" + to + ", username=" + username + "]";
	}
}
