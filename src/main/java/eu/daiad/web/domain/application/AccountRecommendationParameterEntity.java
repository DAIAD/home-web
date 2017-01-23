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

@Entity(name = "account_recommendation_parameter")
@Table(schema = "public", name = "account_recommendation_parameter")
public class AccountRecommendationParameterEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(
	    sequenceName = "account_recommendation_parameter_id_seq",
	    name = "account_recommendation_parameter_id_seq",
	    allocationSize = 1,
	    initialValue = 1)
	@GeneratedValue(generator = "account_recommendation_parameter_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_recommendation_id", nullable = false)
	private AccountRecommendationEntity recommendation;

	@Basic()
	private String key;

	@Basic()
	private String value;

	public AccountRecommendationParameterEntity()
	{}
	
	public AccountRecommendationParameterEntity(
	    AccountRecommendationEntity recommendation, String key, String value)
	{
	    this.recommendation = recommendation;
	    this.key = key;
	    this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public int getId() {
		return id;
	}

}
