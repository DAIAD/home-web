package eu.daiad.web.model.loader;

import eu.daiad.web.connector.SftpProperties;

public class DataTransferConfiguration {

	private SftpProperties sftpProperties;

	private String remoteFolder;

	private String localFolder;

	private String timezone;

	private String filterRegEx;

	public SftpProperties getSftpProperties() {
		return sftpProperties;
	}

	public void setSftpProperties(SftpProperties sftpProperties) {
		this.sftpProperties = sftpProperties;
	}

	public String getRemoteFolder() {
		return remoteFolder;
	}

	public void setRemoteFolder(String remoteFolder) {
		this.remoteFolder = remoteFolder;
	}

	public String getLocalFolder() {
		return localFolder;
	}

	public void setLocalFolder(String localFolder) {
		this.localFolder = localFolder;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getFilterRegEx() {
		return filterRegEx;
	}

	public void setFilterRegEx(String filterRegEx) {
		this.filterRegEx = filterRegEx;
	}

}
