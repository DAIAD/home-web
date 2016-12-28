package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import eu.daiad.web.model.message.EnumRecommendationTemplate;

@Entity(name = "account_recommendation")
@Table(schema = "public", name = "account_recommendation")
public class AccountRecommendationEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(
	    sequenceName = "account_recommendation_id_seq",
	    name = "account_recommendation_id_seq",
	    allocationSize = 1,
	    initialValue = 1)
	@GeneratedValue(generator = "account_recommendation_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	@Enumerated(EnumType.STRING)
	@Column(name = "template_name")
	private EnumRecommendationTemplate template;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "account_recommendation_id")
	private Set<AccountRecommendationParameterEntity> properties = 
	    new HashSet<AccountRecommendationParameterEntity>();

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@Column(name = "acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime acknowledgedOn;

	@Column(name = "receive_acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime receiveAcknowledgedOn;

	public AccountRecommendationEntity()
	{
	    // no-op
	}
	
	public AccountRecommendationEntity(
        AccountEntity account, EnumRecommendationTemplate template, Map<String, Object> p)
    {
	    this.account = account;
        this.template = template;
        
        if (p != null) {
            for (Map.Entry<String, Object> e: p.entrySet()) {
                String key = e.getKey();
                String value = e.getValue().toString();
                this.properties.add(
                    new AccountRecommendationParameterEntity(this, key, value)); 
            }
        }
    }

	public AccountRecommendationEntity(
	    AccountEntity account, EnumRecommendationTemplate template) 
	{
	    this(account, template, null);
	}
	
    public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public EnumRecommendationTemplate getTemplate() {
		return template;
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

	public Set<AccountRecommendationParameterEntity> getProperties() {
		return properties;
	}
    	
	public DateTime getReceiveAcknowledgedOn() {
		return receiveAcknowledgedOn;
	}

	public void setReceiveAcknowledgedOn(DateTime receiveAcknowledgedOn) {
		this.receiveAcknowledgedOn = receiveAcknowledgedOn;
	}

}
