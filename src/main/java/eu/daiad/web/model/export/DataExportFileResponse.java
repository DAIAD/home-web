package eu.daiad.web.model.export;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class DataExportFileResponse extends RestResponse {

    private int index;

    private int size;

    private int total;

    private List<ExportFile> files;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<ExportFile> getFiles() {
        return files;
    }

    public void setFiles(List<ExportFile> files) {
        this.files = files;
    }

}
