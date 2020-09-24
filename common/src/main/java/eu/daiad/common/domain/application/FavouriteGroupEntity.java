package eu.daiad.common.domain.application;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.daiad.common.model.favourite.EnumFavouriteType;

@Entity(name = "favourite_group")
@Table(schema = "public", name = "favourite_group")
public class FavouriteGroupEntity extends FavouriteEntity {

	@ManyToOne()
	@JoinColumn(name = "group_id", nullable = false)
	private GroupEntity group;

	public GroupEntity getGroup() {
		return group;
	}

	public void setGroup(GroupEntity group) {
		this.group = group;
	}

	@Override
	public EnumFavouriteType getType() {
		return EnumFavouriteType.GROUP;
	}

}
