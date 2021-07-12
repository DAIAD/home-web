package eu.daiad.common.model.loader;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class FileProcessingStatus {

	@Getter
	@Setter
	private String filename;

	@Getter
	@Setter
	private long totalRows = 0;

	@Getter
	private long processedRows = 0;

	@Getter
	private long skippedRows = 0;

	@Getter
	private long ignoredRows = 0;

	@Getter
	@Setter
	private Long minTimestamp;

	@Getter
	@Setter
	private Long maxTimestamp;

	@Getter
	private long negativeDifference = 0;

	public void process() {
		processedRows++;
	}

	public void skip() {
		skippedRows++;
	}

	public void ignore() {
		ignoredRows++;
	}

	public void negativeDiff() {
		negativeDifference++;
	}

}
