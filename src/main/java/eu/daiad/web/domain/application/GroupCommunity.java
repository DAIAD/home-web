package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import eu.daiad.web.model.group.EnumGroupType;

@Entity(name = "group_community")
@Table(schema = "public", name = "group_community")
public class GroupCommunity extends GroupEntity {

	@Basic()
	private String description;

	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.BinaryType")
	private byte image[];

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	@Override
	public EnumGroupType getType() {
		return EnumGroupType.COMMONS;
	}

}
