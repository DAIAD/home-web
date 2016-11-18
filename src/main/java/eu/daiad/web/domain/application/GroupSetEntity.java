package eu.daiad.web.domain.application;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import eu.daiad.web.model.group.EnumGroupType;

@Entity(name = "group_set")
@Table(schema = "public", name = "group_set")
public class GroupSetEntity extends GroupEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", nullable = false)
	private AccountEntity owner;

	@Override
	public EnumGroupType getType() {
		return EnumGroupType.SET;
	}

	public AccountEntity getOwner() {
		return owner;
	}

	public void setOwner(AccountEntity owner) {
		this.owner = owner;
	}

}
