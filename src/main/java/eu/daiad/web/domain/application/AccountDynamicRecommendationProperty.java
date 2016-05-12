package eu.daiad.web.domain.application;

import javax.persistence.Basic;
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

@Entity(name = "account_dynamic_recommendation_property")
@Table(schema = "public", name = "account_dynamic_recommendation_property")
public class AccountDynamicRecommendationProperty {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_dynamic_recommendation_property_id_seq", name = "account_dynamic_recommendation_property_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_dynamic_recommendation_property_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_dynamic_recommendation_id", nullable = false)
	private AccountDynamicRecommendation recommendation;

	@Basic()
	private String key;

	@Basic()
	private String value;

	public AccountDynamicRecommendation getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(AccountDynamicRecommendation recommendation) {
		this.recommendation = recommendation;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getId() {
		return id;
	}

}
