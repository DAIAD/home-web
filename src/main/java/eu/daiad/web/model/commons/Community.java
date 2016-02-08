package eu.daiad.web.model.commons;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Geometry;

public class Community {

	private int id;

	@NotNull
	@NotEmpty
	@Pattern(regexp = "en|el|es|de")
	private String locale;

	@NotEmpty
	@Size(max = 100)
	private String name;

	@NotEmpty
	private String description;

	private int size = 0;

	private byte image[];

	private DateTime createdOn = new DateTime();

	private Geometry geometry;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

}
