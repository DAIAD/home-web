package eu.daiad.common.model.meter;

public class WaterMeterDataRow {

	public String serial;

	public long timestamp;

	public float volume;

	public Float difference;

	public static WaterMeterDataRow of(String serial, long timestamp, float volume) {
		final WaterMeterDataRow r = new WaterMeterDataRow();

		r.difference = null;
		r.serial = serial;
		r.timestamp = timestamp;
		r.volume = volume;

		return r;
	}

}
