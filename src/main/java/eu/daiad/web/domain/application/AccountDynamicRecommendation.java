package eu.daiad.web.domain.application;

import java.util.HashSet;
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

@Entity(name = "account_dynamic_recommendation")
@Table(schema = "public", name = "account_dynamic_recommendation")
public class AccountDynamicRecommendation {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_dynamic_recommendation_id_seq", name = "account_dynamic_recommendation_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_dynamic_recommendation_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "dynamic_recommendation_id", nullable = false)
	private DynamicRecommendation recommendation;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "account_dynamic_recommendation_id")
	private Set<AccountDynamicRecommendationProperty> properties = new HashSet<AccountDynamicRecommendationProperty>();

	@Column(name = "created_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@Column(name = "acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime acknowledgedOn;

	@Column(name = "receive_acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime receiveAcknowledgedOn;

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public DynamicRecommendation getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(DynamicRecommendation recommendation) {
		this.recommendation = recommendation;
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

	public Set<AccountDynamicRecommendationProperty> getProperties() {
		return properties;
	}

	public DateTime getReceiveAcknowledgedOn() {
		return receiveAcknowledgedOn;
	}

	public void setReceiveAcknowledgedOn(DateTime receiveAcknowledgedOn) {
		this.receiveAcknowledgedOn = receiveAcknowledgedOn;
	}

}
