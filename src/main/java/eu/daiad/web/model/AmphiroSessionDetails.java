package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AmphiroSessionDetails extends AmphiroSession {

	private ArrayList<AmphiroMeasurement> measurements;

	public AmphiroSessionDetails() {
		super();

		this.measurements = new ArrayList<AmphiroMeasurement>();
	}

	public void add(AmphiroMeasurement measurement) {
		this.measurements.add(measurement);
	}

	public ArrayList<AmphiroMeasurement> getMeasurements() {
		Collections.sort(this.measurements,
				new Comparator<AmphiroMeasurement>() {

					public int compare(AmphiroMeasurement o1,
							AmphiroMeasurement o2) {
						if (o1.getIndex() <= o2.getIndex()) {
							return -1;
						} else {
							return 1;
						}
					}
				});

		return this.measurements;
	}
}
