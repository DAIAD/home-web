package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

public class DataSeries {

	private UUID deviceId;
	
	private ArrayList<DataPoint> points;
	
	public void setDeviceId(UUID value) {
		this.deviceId = value;
	}
	
	public UUID getDeviceId() {
		return this.deviceId;
	}
	
	public void setPoints(ArrayList<DataPoint> value) {
		this.points = value;
	}
	
	public ArrayList<DataPoint> getPoints() {
		return this.points;
	}
	
	
}
