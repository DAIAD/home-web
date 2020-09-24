package eu.daiad.common.domain.admin;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.common.model.export.EnumDataExportType;

/**
 * Entity of an exported data file.
 */
@Entity(name = "export")
@Table(schema = "public", name = "export")
public class ExportFileEntity {

    /**
     * Unique database key.
     */
    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "export_id_seq", name = "export_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "export_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    /**
     * Unique UUID key.
     */
    @Column()
    @Type(type = "pg-uuid")
    private UUID key = UUID.randomUUID();

    /**
     * Utility id from which data has been exported.
     */
    @Column(name = "utility_id")
    private int utilityId;

    /**
     * Utility name from which data has been exported.
     */
    @Column(name = "utility_name")
    private String utilityName;

    /**
     * Absolute path to the folder where the data file is stored.
     */
    @Basic
    private String path;

    /**
     * Physical file name.
     */
    @Basic
    private String filename;

    /**
     * File size.
     */
    @Column(name = "file_size")
    private long size;

    /**
     * A user friendly description of the file contents.
     */
    @Basic
    private String description;

    /**
     * Total rows exported.
     */
    @Column(name = "row_count")
    long totalRows = 0;

    /**
     * Creation date.
     */
    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn;

    /**
     * Export operation start date and time.
     */
    @Column(name = "started_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime startedOn;

    /**
     * Export operation completion date and time.
     */
    @Column(name = "completed_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime completedOn;

    @Basic()
    private boolean hidden;

    @Basic()
    private boolean pinned;

    @Enumerated(EnumType.STRING)
    private EnumDataExportType type;

    public UUID getKey() {
        return key;
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

    public long getId() {
        return id;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public EnumDataExportType getType() {
        return type;
    }

    public void setType(EnumDataExportType type) {
        this.type = type;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

}
