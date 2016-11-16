package eu.daiad.web.model.export;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an export file created by {@link eu.daiad.web.service.etl.IDataExportService}
 */
public class ExportFile {

    /**
     * Unique export key.
     */
    private UUID key;

    /**
     * Utility id from which data was exported.
     */
    @JsonIgnore
    private int utilityId;

    /**
     * Utility name from which data was exported.
     */
    @JsonProperty("utility")
    private String utilityName;

    /**
     * Path where the exported file is stored.
     */
    @JsonIgnore
    private String path;

    /**
     * File name.
     */
    private String filename;

    /**
     * File size.
     */
    private long size;

    /**
     * A user friendly description of the contents of the file.
     */
    private String description;

    /**
     * Total number of rows exported.
     *
     * During a data export operation more than one files may be created and
     * added in an archive. The total rows counts the rows in all files.
     */
    long totalRows = 0;

    /**
     * Date and time the file has been created.
     */
    private DateTime createdOn;

    /**
     * Date and time the export operation has started.
     */
    private DateTime startedOn;

    /**
     * Date and time the export operation has completed.
     */
    private DateTime completedOn;

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    public int getUtilityId() {
        return utilityId;
    }

    public void setUtilityId(int utilityId) {
        this.utilityId = utilityId;
    }

    public String getUtilityName() {
        return utilityName;
    }

    public void setUtilityName(String utilityName) {
        this.utilityName = utilityName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(long totalRows) {
        this.totalRows = totalRows;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public DateTime getStartedOn() {
        return startedOn;
    }

    public void setStartedOn(DateTime startedOn) {
        this.startedOn = startedOn;
    }

    public DateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(DateTime completedOn) {
        this.completedOn = completedOn;
    }

}
