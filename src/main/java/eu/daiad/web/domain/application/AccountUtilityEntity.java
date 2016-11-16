package eu.daiad.web.domain.application;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "account_utility")
@Table(schema = "public", name = "account_utility")
public class AccountUtilityEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_utility_id_seq", name = "account_utility_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_utility_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity owner;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "utility_id", nullable = false)
	private UtilityEntity utility;

	@Column(name = "date_assigned")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime assignedOn = new DateTime();

	public AccountEntity getOwner() {
		return owner;
	}

	public void setOwner(AccountEntity owner) {
		this.owner = owner;
	}

	public UtilityEntity getUtility() {
		return utility;
	}

	public void setUtility(UtilityEntity utility) {
		this.utility = utility;
	}

	public DateTime getAssignedOn() {
		return assignedOn;
	}

	public void setAssignedOn(DateTime assignedOn) {
		this.assignedOn = assignedOn;
	}

	public int getId() {
		return id;
	}
}
