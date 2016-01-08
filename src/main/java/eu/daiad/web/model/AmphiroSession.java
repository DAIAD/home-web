package eu.daiad.web.model;

import java.util.ArrayList;

import org.joda.time.DateTime;

public class AmphiroSession {

	private long id;

	private long timestamp;

	private int duration;

	private boolean history;

	private float temperature;

	private float volume;

	private float energy;

	private float flow;

	private ArrayList<KeyValuePair> properties;

	public AmphiroSession() {
		this.properties = new ArrayList<KeyValuePair>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public DateTime getDate() {
		return new DateTime(this.timestamp);
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isHistory() {
		return history;
	}

	public void setHistory(boolean history) {
		this.history = history;
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

	public float getEnergy() {
		return energy;
	}

	public void setEnergy(float energy) {
		this.energy = energy;
	}

	public float getFlow() {
		return flow;
	}

	public void setFlow(float flow) {
		this.flow = flow;
	}

	public ArrayList<KeyValuePair> getProperties() {
		return properties;
	}

	public void setProperties(ArrayList<KeyValuePair> properties) {
		if (properties == null) {
			this.properties = new ArrayList<KeyValuePair>();
		} else {
			this.properties = properties;
		}
	}

	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new ArrayList<KeyValuePair>();
		}
		properties.add(new KeyValuePair(key, value));
	}

	public String getPropertyByKey(String key) {
		for (int i = 0, count = this.properties.size(); i < count; i++) {
			if (this.properties.get(i).getKey().equals(key)) {
				return this.properties.get(i).getValue();
			}
		}
		return null;
	}
}
