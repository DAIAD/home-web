package eu.daiad.web.service.etl;

import java.io.File;

/**
 * Helper class for managing files with user assigned labels.
 */
public class FileLabelPair {

    private File file;

    private String label;

    public FileLabelPair(File file, String label) {
        this.file = file;
        this.label = label;
    }

    public File getFile() {
        return file;
    }

    public String getLabel() {
        return label;
    }
}
