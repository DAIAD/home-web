package eu.daiad.common.model.message;

public class Insight extends Recommendation 
{   
    public Insight(EnumRecommendationTemplate recommendationTemplate, int id)
    {
        super(id, recommendationTemplate);
    }
    
    public Insight(EnumRecommendationType recommendationType, int id)
    {
        super(id, recommendationType);
    }
}