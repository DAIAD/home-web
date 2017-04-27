package eu.daiad.web.model.export;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class DataExportFileResponse extends RestResponse {

    private Integer index;

    private Integer size;

    private int total;

    private List<ExportFile> files;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
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
