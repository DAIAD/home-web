package eu.daiad.web.model.loader;

public class FileProcessingStatus {

    private String filename;

    private long totalRows = 0;

    private long processedRows = 0;

    private long skippedRows = 0;

    private long ignoredRows = 0;

    private Long minTimestamp;

    private Long maxTimestamp;

    private long negativeDifference = 0;

    public long getNegativeDifference() {
        return negativeDifference;
    }

    public long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(long totalRows) {
        this.totalRows = totalRows;
    }

    public long getProcessedRows() {
        return processedRows;
    }

    public void setProcessedRows(long processedRows) {
        this.processedRows = processedRows;
    }

    public long getSkippedRows() {
        return skippedRows;
    }

    public void setSkippedRows(long skippedRows) {
        this.skippedRows = skippedRows;
    }

    public long getIgnoredRows() {
        return ignoredRows;
    }

    public void setIgnoredRows(long ignoredRows) {
        this.ignoredRows = ignoredRows;
    }

    public void processRow() {
        processedRows++;
    }

    public void skipRow() {
        skippedRows++;
    }

    public void ignoreRow() {
        ignoredRows++;
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
        negativeDifference++;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

}
