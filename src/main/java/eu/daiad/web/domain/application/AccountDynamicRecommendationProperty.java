package eu.daiad.web.domain.application;

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

/**
 *
 * @author nkarag
 */
@Entity(name = "account_dynamic_recommendation_property")
@Table(schema = "public", name = "account_dynamic_recommendation_property")
public class AccountDynamicRecommendationProperty {
	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_dynamic_recommendation_property_id_seq", name = "account_dynamic_recommendation_property_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_dynamic_recommendation_property_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;       

        //@ManyToOne()
	//@JoinColumn(name = "account_dynamic_recommendation_id", nullable = true)  
	@Column(name = "account_dynamic_recommendation_id", nullable = false)
	private int accountDynamicRecommendationId;  
        
	@Column(name = "\"key\"") //http://stackoverflow.com/questions/2224503/creating-field-with-reserved-word-name-with-jpa
	private String key;         

	@Column(name = "\"value\"")
	private String value;
        
	public int getId() {
		return id;
	}

//	public void setId(int id) {
//		this.id = id;
//	}

	public int getAccountDynamicRecommendationId() {
		return accountDynamicRecommendationId;
	}

	public void setAccountDymanicRecommendationId(int accountDynamicRecommendationId) {
		this.accountDynamicRecommendationId = accountDynamicRecommendationId;
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
}
