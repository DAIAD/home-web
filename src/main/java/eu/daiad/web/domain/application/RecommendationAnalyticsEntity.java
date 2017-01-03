package eu.daiad.web.domain.application;

import eu.daiad.web.model.message.EnumRecommendationType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity(name = "recommendation_analytics")
public class RecommendationAnalyticsEntity 
{
    @Id()
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
	private EnumRecommendationType type;
    
    @Column(name = "count")
	private long count;

    public long getCount() {
        return count;
    }
    
    public EnumRecommendationType getType() {
        return type;
    }
}
