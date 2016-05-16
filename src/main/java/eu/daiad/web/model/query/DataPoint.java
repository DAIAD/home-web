package eu.daiad.web.model.query;

public class DataPoint {

	public enum EnumDataPointType {
		UNDEFINED, METER, AMPHIRO, RANKING;
	}

	protected EnumDataPointType type;

	private Long timestamp = null;

	public DataPoint() {
		this.type = EnumDataPointType.UNDEFINED;
	}

	public DataPoint(long timestamp) {
		this.timestamp = timestamp;
		this.type = EnumDataPointType.UNDEFINED;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public EnumDataPointType getType() {
		return this.type;
	}

}
