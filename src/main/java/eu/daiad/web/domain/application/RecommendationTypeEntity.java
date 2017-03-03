package eu.daiad.web.domain.application;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import eu.daiad.web.model.message.EnumRecommendationType;
import eu.daiad.web.model.message.RecommendationCode;

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

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "type")
    private List<RecommendationCodeEntity> codes = new ArrayList<>();
	
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
	    this.priority = type.getPriority().intValue();
	    for (RecommendationCode code: type.getCodes()) 
            this.codes.add(new RecommendationCodeEntity(code, this));
	}
}
