package eu.daiad.common.model.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class MessageAcknowledgement {

	@JsonDeserialize(using = EnumMessageType.Deserializer.class)
	private EnumMessageType type;

	private int id;

	private long timestamp;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public EnumMessageType getType() {
		return type;
	}

	public void setType(EnumMessageType type) {
		this.type = type;
	}

}
