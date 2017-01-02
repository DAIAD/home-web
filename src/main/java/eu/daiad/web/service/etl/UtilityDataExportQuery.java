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
     * Selects exported data. When exporting amphiro data, this parameter is ignored.
     */
    private EnumExportMode mode;

    /**
     * Date format.
     */
    private String dateFormat;

    /**
     * True if the output file must be compressed. If more than one files are
     * generated, the output is always compressed. When exporting amphiro data,
     * this parameter is ignored.
     */
    private boolean comporessed;

    /**
     * A short description of the exported data
     */
    private String description;

    public UtilityDataExportQuery(UtilityInfo utility, String targetDirectory) {
        this.utility = utility;
        this.targetDirectory = targetDirectory;
        dateFormat = "yyyy-MM-dd HH:mm:ss";
        comporessed = true;
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

    public EnumExportMode getMode() {
        return mode;
    }

    public void setMode(EnumExportMode mode) {
        this.mode = mode;
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

    public boolean isComporessed() {
        return comporessed;
    }

    public void setComporessed(boolean comporessed) {
        this.comporessed = comporessed;
    }

    public enum EnumExportMode {
        METER_UTILITY,
        METER_TRIAL,
        ALL_TRIAL;
    }

}
