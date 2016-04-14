package eu.daiad.web.model.loader;

public class FileProcessingStatus {

	private int totalRows = 0;

	private int processedRows = 0;

	private int skippedRows = 0;

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

	@Override
	public String toString() {
		return "FileProcessingStatus [totalRows=" + totalRows + ", processedRows=" + processedRows + ", skippedRows="
						+ skippedRows + "]";
	}

}
