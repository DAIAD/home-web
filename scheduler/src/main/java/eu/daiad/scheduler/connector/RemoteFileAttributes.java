package eu.daiad.scheduler.connector;

import org.joda.time.DateTime;

public class RemoteFileAttributes {

	private String filename;

	private long size;

	private DateTime modifiedOn;

	private String source;
	
	private String remoteFolder;
	
	public RemoteFileAttributes(String source, String remoteFolder, String filename, long size, long mTime) {
		this.filename = filename;
		this.size = size;
		this.modifiedOn = new DateTime(mTime * 1000L);
		this.source = source;
		this.remoteFolder = remoteFolder;
	}

	public String getFilename() {
		return filename;
	}

	public long getSize() {
		return size;
	}

	public DateTime getModifiedOn() {
		return modifiedOn;
	}

	public String getSource() {
		return source;
	}

	public String getRemoteFolder() {
		return remoteFolder;
	}

}
