package eu.daiad.web.model.query;

import java.util.HashMap;
import java.util.Map;

public class DataPoint {

	private long timestamp;

	private int population;

	private String label;

	private Map<String, Number> values = new HashMap<String, Number>();

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getPopulation() {
		return population;
	}

	public void setPopulation(int population) {
		this.population = population;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Map<String, Number> getValues() {
		return values;
	}

}
