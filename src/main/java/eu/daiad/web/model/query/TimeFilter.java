package eu.daiad.web.model.query;

public class TimeFilter {

	private EnumTimeInterval interval = EnumTimeInterval.NONE;

	private EnumTemporalFilterType type = EnumTemporalFilterType.ABSOLUTE;

	private long start;

	private Long end;

	private Integer duration;

	public EnumTimeInterval getInterval() {
		return interval;
	}

	public void setInterval(EnumTimeInterval interval) {
		this.interval = interval;
	}

	public EnumTemporalFilterType getType() {
		return type;
	}

	public void setType(EnumTemporalFilterType type) {
		this.type = type;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(Long end) {
		this.end = end;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

}
