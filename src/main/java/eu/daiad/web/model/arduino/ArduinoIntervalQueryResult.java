package eu.daiad.web.model.arduino;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class ArduinoIntervalQueryResult extends RestResponse {

	private ArrayList<ArduinoMeasurement> measurements = new ArrayList<ArduinoMeasurement>();

	public ArduinoIntervalQueryResult() {
		super();
	}

	public ArduinoIntervalQueryResult(String code, String description) {
		super(code, description);
	}

	public ArrayList<ArduinoMeasurement> getMeasurements() {
		return measurements;
	}

	public void add(long timestamp, long volume) {
		ArduinoMeasurement m = new ArduinoMeasurement();
		m.setTimestamp(timestamp);
		m.setVolume(volume);

		this.measurements.add(m);
	}
}
