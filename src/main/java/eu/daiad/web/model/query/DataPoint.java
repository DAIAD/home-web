package eu.daiad.web.model.query;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DataPoint {

	enum EnumDataPointType {
		METER, AMPHIRO;
	}

	@JsonIgnore
	protected EnumDataPointType type;

	private Long timestamp = null;

	private Map<EnumMetric, Double> volume = new HashMap<EnumMetric, Double>();

	public DataPoint() {
		this.type = EnumDataPointType.METER;
	}

	public DataPoint(long timestamp) {
		this.timestamp = timestamp;
		this.type = EnumDataPointType.METER;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Map<EnumMetric, Double> getVolume() {
		return volume;
	}

	public EnumDataPointType getType() {
		return this.type;
	}

}
