package eu.daiad.web.domain.application;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 *
 * @author nkarag
 */
@Entity(name = "account_dynamic_recommendation")
@Table(schema = "public", name = "account_dynamic_recommendation")
public class AccountDynamicRecommendation {
    
	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_dynamic_recommendation_id_seq", name = "account_dynamic_recommendation_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_dynamic_recommendation_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;       

	@Column(name = "account_id", nullable = false)        
	private int accountId;  

        //@ManyToOne()
	//@JoinColumn(name = "dynamic_recommendation_id", nullable = true)        
	@Column(name = "dynamic_recommendation_id", nullable = false)
	private int dynamicRecommendationId;  
        
	@Column(name = "created_on")        
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;    

	@Column(name = "acknowledged_on")        
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime acknowledgedOn;
        
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}  
        
	public int getDynamicRecommendationId() {
		return dynamicRecommendationId;
	}

	public void setDynamicRecommendationId(int dynamicRecommendationId) {
		this.dynamicRecommendationId = dynamicRecommendationId;
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
}
