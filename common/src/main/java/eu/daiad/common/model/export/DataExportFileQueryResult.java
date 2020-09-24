package eu.daiad.common.model.export;

import java.util.List;

public class DataExportFileQueryResult {

    private int total;

    private List<ExportFile> files;

    public DataExportFileQueryResult(int total, List<ExportFile> files) {
        this.total = total;
        this.files = files;
    }

    public int getTotal() {
        return total;
    }

    public List<ExportFile> getFiles() {
        return files;
    }

}
