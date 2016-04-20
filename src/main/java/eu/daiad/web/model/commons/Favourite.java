package eu.daiad.web.model.commons;

import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

public class Favourite {

	private EnumFavouriteType type;

	private UUID key;

	private UUID referenceKey;

	private String label;

	private long createdOn;

	private Geometry geometry;

	public Favourite(EnumFavouriteType type) {
		this.type = type;
	}

	public UUID getKey() {
		return key;
	}

	public void setKey(UUID key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}

	public UUID getReferenceKey() {
		return referenceKey;
	}

	public void setReferenceKey(UUID referenceKey) {
		this.referenceKey = referenceKey;
	}

	public EnumFavouriteType getType() {
		return type;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

}
