package eu.daiad.common.domain.application;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "account_role")
@Table(schema = "public", name = "account_role")
public class AccountRoleEntity {


	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_role_id_seq", name = "account_role_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_role_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;
	
	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity owner;
	
	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "role_id", nullable = false)
	private RoleEntity role;	
	
	@Column(name = "date_assigned")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime assignedOn = new DateTime();
	
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_by", nullable = true)
	private AccountEntity assignedBy;

	public AccountEntity getOwner() {
		return owner;
	}

	public void setOwner(AccountEntity owner) {
		this.owner = owner;
	}

	public RoleEntity getRole() {
		return role;
	}

	public void setRole(RoleEntity role) {
		this.role = role;
	}

	public DateTime getAssignedOn() {
		return assignedOn;
	}

	public void setAssignedOn(DateTime assignedOn) {
		this.assignedOn = assignedOn;
	}

	public AccountEntity getAssignedBy() {
		return assignedBy;
	}

	public void setAssignedBy(AccountEntity assignedBy) {
		this.assignedBy = assignedBy;
	}

	public int getId() {
		return id;
	}
}
