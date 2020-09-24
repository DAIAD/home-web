package eu.daiad.common.domain.application;

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

@Entity(name = "account_tip")
@Table(schema = "public", name = "account_tip")
public class AccountTipEntity
{
	@Id()
	@Column(name = "id")
	@SequenceGenerator(
	    sequenceName = "account_tip_id_seq",
	    name = "account_tip_id_seq",
	    allocationSize = 1,
	    initialValue = 1
	)
	@GeneratedValue(generator = "account_tip_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne()
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	@ManyToOne()
	@JoinColumn(name = "tip_id", nullable = false)
	private TipEntity tip;

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@Column(name = "acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime acknowledgedOn;

	@Column(name = "receive_acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime receiveAcknowledgedOn;

	public AccountTipEntity() {}

	public AccountTipEntity(AccountEntity account, TipEntity tip)
    {
       this.account = account;
       this.tip = tip;
    }

    public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public TipEntity getTip() {
		return tip;
	}

	public void setTip(TipEntity tip) {
		this.tip = tip;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public DateTime getAcknowledgedOn() {
		return acknowledgedOn;
	}

	public void setAcknowledgedOn(DateTime acknowledgedOn) {
		this.acknowledgedOn = acknowledgedOn;
	}

	public int getId() {
		return id;
	}

	public DateTime getReceiveAcknowledgedOn() {
		return receiveAcknowledgedOn;
	}

	public void setReceiveAcknowledgedOn(DateTime receiveAcknowledgedOn) {
		this.receiveAcknowledgedOn = receiveAcknowledgedOn;
	}

}
