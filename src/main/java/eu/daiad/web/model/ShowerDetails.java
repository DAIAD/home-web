package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ShowerDetails extends Shower {

	private ArrayList<DataPoint> points;

	public ShowerDetails(long id) {
		super(id);

		this.points = new ArrayList<DataPoint>();
	}

	@Override
	public void add(DataPoint point) {
		super.add(point);

		this.points.add(point);
	}

	public ArrayList<DataPoint> getMeasurements(){
		Collections.sort(this.points, new Comparator<DataPoint>() {

			public int compare(DataPoint o1, DataPoint o2) {
				if (o1.showerTime <= o2.showerTime) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		
		return this.points;
	}
}
