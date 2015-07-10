package eu.daiad.web.model;

import java.util.ArrayList;

import org.joda.time.DateTime;

public class SessionData {

	private ArrayList<KeyValuePair> properties;
	
    private long showerId;

	private float temperature;
    
    private float volume;

    private float flow;
    
    private float energy;

    private int duration;
       
    private DateTime timestamp;
    
    public long getShowerId() {
		return showerId;
	}

	public void setShowerId(long showerId) {
		this.showerId = showerId;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	public float getFlow() {
		return flow;
	}

	public void setFlow(float flow) {
		this.flow = flow;
	}

	public float getEnergy() {
		return energy;
	}

	public void setEnergy(float energy) {
		this.energy = energy;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}
   
	public ArrayList<KeyValuePair> getProperties() {
		return this.properties;
	}
	
	public void setProperties(ArrayList<KeyValuePair> value) {
		this.properties = value;
	}
	
	public void addProperty(String key, String value) {
		if(properties == null) {
			properties = new ArrayList<KeyValuePair>();
		}
		properties.add(new KeyValuePair(key, value));
	}
	
	public String getPropertyByKey(String key) {
		for(int i=0;i<this.properties.size();i++) {
			if(this.properties.get(i).getKey().equals(key)) {
				return this.properties.get(i).getValue();
			}
		}
		return null;
	}
}
