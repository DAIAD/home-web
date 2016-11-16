package eu.daiad.web.model.loader;

public class FileProcessingStatus {

    private int totalRows = 0;

    private int processedRows = 0;

    private int skippedRows = 0;

    private Long minTimestamp;

    private Long maxTimestamp;

    private int negativeDifference = 0;

    public int getNegativeDifference() {
        return negativeDifference;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getProcessedRows() {
        return processedRows;
    }

    public void setProcessedRows(int processedRows) {
        this.processedRows = processedRows;
    }

    public int getSkippedRows() {
        return skippedRows;
    }

    public void setSkippedRows(int skippedRows) {
        this.skippedRows = skippedRows;
    }

    public void processRow() {
        this.processedRows++;
    }

    public void skipRow() {
        this.skippedRows++;
    }

    public Long getMinTimestamp() {
        return minTimestamp;
    }

    public void setMinTimestamp(Long minTimestamp) {
        this.minTimestamp = minTimestamp;
    }

    public Long getMaxTimestamp() {
        return maxTimestamp;
    }

    public void setMaxTimestamp(Long maxTimestamp) {
        this.maxTimestamp = maxTimestamp;
    }

    public void increaseNegativeDifference() {
        this.negativeDifference++;
    }
}
