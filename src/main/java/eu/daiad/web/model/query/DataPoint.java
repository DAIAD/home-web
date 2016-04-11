package eu.daiad.web.model.query;

import java.util.HashMap;
import java.util.Map;

public class DataPoint {

	private Long timestamp = null;

	private Map<String, Double> values = new HashMap<String, Double>();

	public DataPoint() {

	}

	public DataPoint(long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Double> getValues() {
		return values;
	}

}
