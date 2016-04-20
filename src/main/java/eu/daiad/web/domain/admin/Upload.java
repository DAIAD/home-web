package eu.daiad.web.domain.admin;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "upload")
@Table(schema = "public", name = "upload")
public class Upload {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "upload_id_seq", name = "upload_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "upload_id_seq", strategy = GenerationType.SEQUENCE)
	private long id;

	@Basic
	private String source;

	@Column(name = "remote_folder")
	private String remoteFolder;

	@Column(name = "local_folder")
	private String localFolder;

	@Column(name = "remote_filename")
	private String remoteFilename;

	@Column(name = "local_filename")
	private String localFilename;

	@Column(name = "file_size")
	private long size;

	@Column(name = "row_count")
	long totalRows = 0;

	@Column(name = "row_processed")
	long proccessedRows = 0;

	@Column(name = "row_skipped")
	long skippedRows = 0;

	@Column(name = "date_modified")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime modifiedOn;

	@Column(name = "upload_start_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime uploadStartedOn;

	@Column(name = "upload_end_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime uploadCompletedOn;

	@Column(name = "process_start_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime processingStartedOn;

	@Column(name = "process_end_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime processingCompletedOn;

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

	public String getRemoteFilename() {
		return remoteFilename;
	}

	public void setRemoteFilename(String remoteFilename) {
		this.remoteFilename = remoteFilename;
	}

	public String getLocalFilename() {
		return localFilename;
	}

	public void setLocalFilename(String localFilename) {
		this.localFilename = localFilename;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public DateTime getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(DateTime modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public DateTime getUploadStartedOn() {
		return uploadStartedOn;
	}

	public void setUploadStartedOn(DateTime uploadStartedOn) {
		this.uploadStartedOn = uploadStartedOn;
	}

	public DateTime getUploadCompletedOn() {
		return uploadCompletedOn;
	}

	public void setUploadCompletedOn(DateTime uploadCompletedOn) {
		this.uploadCompletedOn = uploadCompletedOn;
	}

	public DateTime getProcessingStartedOn() {
		return processingStartedOn;
	}

	public void setProcessingStartedOn(DateTime processingStartedOn) {
		this.processingStartedOn = processingStartedOn;
	}

	public DateTime getProcessingCompletedOn() {
		return processingCompletedOn;
	}

	public void setProcessingCompletedOn(DateTime processingCompletedOn) {
		this.processingCompletedOn = processingCompletedOn;
	}

	public long getId() {
		return id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public long getProccessedRows() {
		return proccessedRows;
	}

	public void setProccessedRows(long proccessedRows) {
		this.proccessedRows = proccessedRows;
	}

	public long getSkippedRows() {
		return skippedRows;
	}

	public void setSkippedRows(long skippedRows) {
		this.skippedRows = skippedRows;
	}

	public long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(long totalRows) {
		this.totalRows = totalRows;
	}

}
