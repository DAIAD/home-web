package eu.daiad.web.service.etl;

import eu.daiad.web.model.utility.UtilityInfo;

/**
 * Represents a query for selecting utility data to export.
 */
public class UtilityDataExportQuery extends DataExportQuery {

    /**
     * Utility information
     */
    private UtilityInfo utility;

    /**
     * Data source.
     */
    private EnumDataSource source;

    /**
     * Working directory for storing intermediate files.
     */
    private String workingDirectory;

    /**
     * Output directory.
     */
    private String targetDirectory;

    /**
     * Output filename.
     */
    private String filename;

    /**
     * Exports only the data for the registered users
     */
    private boolean exportUserDataOnly;

    /**
     * Date format.
     */
    private String dateFormat;

    /**
     * A short description of the exported data
     */
    private String description;
    
    public UtilityDataExportQuery(UtilityInfo utility, String targetDirectory) {
        this.utility = utility;
        this.targetDirectory = targetDirectory;
        this.dateFormat = "yyyy-MM-dd HH:mm:ss";
    }

    public UtilityInfo getUtility() {
        return utility;
    }

    public EnumDataSource getSource() {
        return source;
    }

    public void setSource(EnumDataSource source) {
        this.source = source;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public boolean isExportUserDataOnly() {
        return exportUserDataOnly;
    }

    public void setExportUserDataOnly(boolean exportUserDataOnly) {
        this.exportUserDataOnly = exportUserDataOnly;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
