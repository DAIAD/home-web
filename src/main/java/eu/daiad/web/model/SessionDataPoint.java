package eu.daiad.web.model;

public class SessionDataPoint extends AmphiroMeasurement {

	public long showerId;

	public int showerTime;
	
	public long getShowerId() {
		return showerId;
	}

	public void setShowerId(long showerId) {
		this.showerId = showerId;
	}

	public int getShowerTime() {
		return showerTime;
	}

	public void setShowerTime(int showerTime) {
		this.showerTime = showerTime;
	}
}
