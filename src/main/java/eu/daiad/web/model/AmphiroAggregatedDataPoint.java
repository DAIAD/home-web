package eu.daiad.web.model;

public class AmphiroAggregatedDataPoint  extends AmphiroAbstractDataPoint{

	private int count = 0;
	
	public AmphiroAggregatedDataPoint(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void addPoint(AmphiroAbstractDataPoint point) {	
		this.volume += point.getVolume();
		this.energy += point.getEnergy();
		
		this.temperature = this.temperature * this.count + point.getTemperature();
		
		this.count++;
		
		this.temperature /= (float) this.count;
	}

	public int getCount() {
		return count;
	}
	
}
