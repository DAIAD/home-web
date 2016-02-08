package eu.daiad.web.domain;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "account_white_list")
@Table(schema = "public", name = "account_white_list")
public class AccountWhiteListEntry {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_white_list_id_seq", name = "account_white_list_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_white_list_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "utility_id", nullable = false)
	private Utility utility;

	@OneToOne()
	@JoinColumn(name = "account_id", nullable = true)
	private Account account;

	@Basic()
	private String username;

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "registered_on")
	private DateTime registeredOn;

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public String getUsername() {
		return username;
	}

	public DateTime getRegisteredOn() {
		return registeredOn;
	}

	public void setRegisteredOn(DateTime registeredOn) {
		this.registeredOn = registeredOn;
	}

	public int getId() {
		return id;
	}

	public Utility getUtility() {
		return utility;
	}

}
