package eu.daiad.web.model.amphiro;

public class AmphiroAggregatedSession extends AmphiroAbstractSession {

private int count = 0;
	
	public AmphiroAggregatedSession(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public void addPoint(AmphiroAbstractSession session) {	
		this.volume += session.getVolume();
		this.energy += session.getEnergy();
		
		this.duration = this.duration * this.count + session.getDuration();
		this.flow = this.flow * this.count + session.getFlow();
		this.temperature = this.temperature * this.count + session.getTemperature();
		
		this.count++;
		
		this.duration /= (float) this.count;
		this.flow /= (float) this.count;
		this.temperature /= (float) this.count;
	}

	public int getCount() {
		return count;
	}
	
}
