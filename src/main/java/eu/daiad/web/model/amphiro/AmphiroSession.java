package eu.daiad.web.model.amphiro;

import java.util.ArrayList;

import eu.daiad.web.model.KeyValuePair;

public class AmphiroSession extends AmphiroAbstractSession {

	private long id;

	private boolean history;

	private AmphiroSessionDeleteAction delete;

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

	public boolean isHistory() {
		return history;
	}

	public void setHistory(boolean history) {
		this.history = history;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
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

	public AmphiroSessionDeleteAction getDelete() {
		return delete;
	}

	public void setDelete(AmphiroSessionDeleteAction delete) {
		this.delete = delete;
	}
}
