package eu.daiad.web.domain.application;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "account_alert")
@Table(schema = "public", name = "account_alert")
public class AccountAlertEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_alert_id_seq", name = "account_alert_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_alert_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "alert_id", nullable = false)
	private AlertEntity alert;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "account_alert_id")
	private Set<AccountAlertPropertyEntity> properties = new HashSet<AccountAlertPropertyEntity>();

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@Column(name = "acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime acknowledgedOn;

	@Column(name = "receive_acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime receiveAcknowledgedOn;

	public AccountAlertEntity()
	{
	    // no-op
	}
	
	public AccountAlertEntity(AccountEntity account, AlertEntity alert, Map<String, Object> p)
	{
	    this.account = account;
	    this.alert = alert;
	    
	    if (p != null) {
	        for (Map.Entry<String, Object> e: p.entrySet()) {
	            this.properties.add(
	                new AccountAlertPropertyEntity(this, e.getKey(), e.getValue().toString())); 
	        }
	    }
	}
	
	public AccountAlertEntity(AccountEntity account, AlertEntity alert)
	{
	    this(account, alert, null);
	}
	
	public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public AlertEntity getAlert() {
		return alert;
	}

	public void setAlert(AlertEntity alert) {
		this.alert = alert;
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

	public Set<AccountAlertPropertyEntity> getProperties() {
		return properties;
	}
	
	public DateTime getReceiveAcknowledgedOn() {
		return receiveAcknowledgedOn;
	}

	public void setReceiveAcknowledgedOn(DateTime receiveAcknowledgedOn) {
		this.receiveAcknowledgedOn = receiveAcknowledgedOn;
	}

}
