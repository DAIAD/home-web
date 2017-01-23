package eu.daiad.web.domain.application;

import java.util.HashMap;
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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "account_alert")
@Table(schema = "public", name = "account_alert")
public class AccountAlertEntity
{
	@Id()
	@Column(name = "id")
	@SequenceGenerator(
	    sequenceName = "account_alert_id_seq",
	    name = "account_alert_id_seq",
	    allocationSize = 1,
	    initialValue = 1)
	@GeneratedValue(generator = "account_alert_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_id", nullable = false)
	@NotNull
	private AccountEntity account;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "account_alert_id")
	private Set<AccountAlertParameterEntity> parameters = new HashSet<>();

	@ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "alert_template", nullable = false)
	@NotNull
    private AlertTemplateEntity alertTemplate;

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@Column(name = "acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime acknowledgedOn;

	@Column(name = "receive_acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime receiveAcknowledgedOn;

	public AccountAlertEntity() {}

	public AccountAlertEntity(
	    AccountEntity account, AlertTemplateEntity templateEntity, Map<String, Object> parameters)
	{
	    this.account = account;
	    this.alertTemplate = templateEntity;

	    if (parameters != null) {
	        for (Map.Entry<String, Object> e: parameters.entrySet()) {
	            String key = e.getKey();
	            String value = e.getValue().toString();
	            this.parameters.add(new AccountAlertParameterEntity(this, key, value));
	        }
	    }
	}

	public AccountAlertEntity(AccountEntity account, AlertTemplateEntity templateEntity)
	{
	    this(account, templateEntity, null);
	}

	public AccountEntity getAccount()
	{
		return account;
	}

	public void setAccount(AccountEntity account)
	{
		this.account = account;
	}

	public AlertTemplateEntity getTemplate()
	{
		return alertTemplate;
	}

	public DateTime getCreatedOn()
	{
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn)
	{
		this.createdOn = createdOn;
	}

	public DateTime getAcknowledgedOn()
	{
		return acknowledgedOn;
	}

	public void setAcknowledgedOn(DateTime acknowledgedOn) {
		this.acknowledgedOn = acknowledgedOn;
	}

	public int getId() {
		return id;
	}

	public Set<AccountAlertParameterEntity> getParameters()
	{
	    return parameters;
	}

	public Map<String, Object> getParametersAsMap()
	{
	    Map<String, Object> p = new HashMap<>();
	    for (AccountAlertParameterEntity pe: parameters)
	        p.put(pe.getKey(), pe.getValue());
	    return p;
	}

	public DateTime getReceiveAcknowledgedOn()
	{
		return receiveAcknowledgedOn;
	}

	public void setReceiveAcknowledgedOn(DateTime receiveAcknowledgedOn)
	{
		this.receiveAcknowledgedOn = receiveAcknowledgedOn;
	}

}
