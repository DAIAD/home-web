package eu.daiad.common.model.amphiro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AmphiroSessionDetails extends AmphiroSession {

	private List<AmphiroMeasurement> measurements;

	public AmphiroSessionDetails() {
		super();

		measurements = new ArrayList<AmphiroMeasurement>();
	}

	public void add(AmphiroMeasurement measurement) {
		measurements.add(measurement);
	}

	public List<AmphiroMeasurement> getMeasurements() {
		Collections.sort(measurements, new Comparator<AmphiroMeasurement>() {
			@Override
            public int compare(AmphiroMeasurement o1, AmphiroMeasurement o2) {
				if (o1.getIndex() <= o2.getIndex()) {
					return -1;
				} else {
					return 1;
				}
			}
		});

		return measurements;
	}

	public void setMeasurements(List<AmphiroMeasurement> measurements) {
		if (measurements == null) {
			new ArrayList<AmphiroMeasurement>();
		} else {
			this.measurements = measurements;
		}
	}
}
