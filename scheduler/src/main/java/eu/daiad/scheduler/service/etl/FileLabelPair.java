package eu.daiad.scheduler.service.etl;

import java.io.File;

/**
 * Helper class for managing files with user assigned labels.
 */
public class FileLabelPair {

    private File file;

    private String label;

    private long rows;

    public FileLabelPair(File file, String label, long rows) {
        this.file = file;
        this.label = label;
        this.rows = rows;
    }

    public File getFile() {
        return file;
    }

    public String getLabel() {
        return label;
    }

    public long getRows() {
        return rows;
    }

    public boolean isEmpty() {
        return (rows == 0);
    }

}
