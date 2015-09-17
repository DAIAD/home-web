package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

public class DataSeries {

	private UUID deviceKey;
	
	private ArrayList<DataPoint> points;
	
	public DataSeries() {
		this.points = new ArrayList<DataPoint>();
	}
	
	public void setDeviceKey(UUID value) {
		this.deviceKey = value;
	}
	
	public UUID getDeviceKey() {
		return this.deviceKey;
	}
	
	public void setPoints(ArrayList<DataPoint> value) {
		this.points = value;
	}
	
	public ArrayList<DataPoint> getPoints() {
		return this.points;
	}
	
	
}