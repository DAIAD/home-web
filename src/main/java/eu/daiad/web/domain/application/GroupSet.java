package eu.daiad.web.domain.application;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.daiad.web.model.commons.EnumGroupType;

@Entity(name = "group_set")
@Table(schema = "public", name = "group_set")
public class GroupSet extends Group {

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", nullable = false)
	private Account owner;

	@Override
	public EnumGroupType getType() {
		return EnumGroupType.SET;
	}

	public Account getOwner() {
		return owner;
	}

	public void setOwner(Account owner) {
		this.owner = owner;
	}

}
