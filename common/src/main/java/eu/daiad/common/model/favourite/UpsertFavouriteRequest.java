package eu.daiad.common.model.favourite;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class UpsertFavouriteRequest {

	@JsonDeserialize(using = EnumFavouriteType.Deserializer.class)
	private EnumFavouriteType type;

	private UUID key;

	private String label;

	public EnumFavouriteType getType() {
		return type;
	}

	public void setType(EnumFavouriteType type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public UUID getKey() {
		return key;
	}

	public void setKey(UUID key) {
		this.key = key;
	}
}
