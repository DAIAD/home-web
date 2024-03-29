package eu.daiad.common.model;

public class KeyValuePair {

	private String key;
	
	private String value;
	
	public KeyValuePair() {
	
	}
	
	public KeyValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public void setKey(String value) {
		this.key = value;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "[key=" + key + ", value=" + value + "]";
	}
}
