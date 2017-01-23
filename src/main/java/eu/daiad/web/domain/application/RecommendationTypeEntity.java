package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import eu.daiad.web.model.message.EnumRecommendationType;

@Entity(name = "recommendation_type")
@Table(schema = "public", name = "recommendation_type")
public class RecommendationTypeEntity 
{
	@Id()
	private int value;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "name", unique = true, nullable = false)
	private EnumRecommendationType type;

	@Basic()
	private int priority;

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public EnumRecommendationType getType() {
		return type;
	}
	
	public EnumRecommendationType asEnum() {
        return type;
    }
	
	public int getValue() {
	    return value;
	}
	
	public RecommendationTypeEntity()
	{
	}
	
	public RecommendationTypeEntity(EnumRecommendationType type)
	{
	    this.type = type;
	    this.value = type.getValue();
	    this.priority = type.getPriority();
	}
}
