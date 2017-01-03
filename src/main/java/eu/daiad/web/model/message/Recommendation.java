package eu.daiad.web.model.message;

import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.device.EnumDeviceType;

public class Recommendation extends Message 
{
    public interface ParameterizedTemplate extends Message.Parameters
    {
        public EnumRecommendationTemplate getTemplate();
    }
    
    public abstract static class AbstractParameterizedTemplate extends Message.AbstractParameters 
        implements ParameterizedTemplate
    {
        protected AbstractParameterizedTemplate(DateTime refDate, EnumDeviceType deviceType)
        {
            super(refDate, deviceType);
        }
    }
    
    public static class SimpleParameterizedTemplate extends AbstractParameterizedTemplate
    {
        final EnumRecommendationTemplate recommendationTemplate;
        
        // Provide some common parameters
        
        Integer integer1;
        
        Integer integer2;
        
        Double currency1;
        
        Double currency2;
        
        public SimpleParameterizedTemplate(
            DateTime refDate, EnumDeviceType deviceType, EnumRecommendationTemplate recommendationTemplate)
        {
            super(refDate, deviceType);
            this.recommendationTemplate = recommendationTemplate;   
        }

        public Integer getInteger1()
        {
            return integer1;
        }

        public SimpleParameterizedTemplate setInteger1(Integer integer1)
        {
            this.integer1 = integer1;
            return this;
        }

        public Integer getInteger2()
        {
            return integer2;
        }

        public SimpleParameterizedTemplate setInteger2(Integer integer2)
        {
            this.integer2 = integer2;
            return this;
        }

        public Double getCurrency1()
        {
            return currency1;
        }

        public SimpleParameterizedTemplate setCurrency1(Double currency1)
        {
            this.currency1 = currency1;
            return this;
        }

        public Double getCurrency2()
        {
            return currency2;
        }

        public SimpleParameterizedTemplate setCurrency2(Double currency2)
        {
            this.currency2 = currency2;
            return this;
        }  
        
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> pairs = super.getParameters();
            
            if (integer1 != null)
                pairs.put("integer1", integer1);
            if (integer2 != null)
                pairs.put("integer2", integer2);
            
            if (currency1 != null)
                pairs.put("currency1", currency1);
            if (currency2 != null)
                pairs.put("currency2", currency2);
            
            return pairs;
        }
        
        @Override
        public EnumRecommendationTemplate getTemplate()
        {
            return recommendationTemplate;
        }
    }

    private final int id;
    
    private final EnumRecommendationType recommendationType;
    
	private final EnumRecommendationTemplate recommendationTemplate;

	private int priority;

	private String title;

	private String description;

	private String imageLink;

	private Long createdOn;

    private Long acknowledgedOn;
	
	public Recommendation(int id, EnumRecommendationTemplate template) 
	{
		this.id = id;
	    this.recommendationTemplate = template;
		this.recommendationType = template.getType();
		this.priority = recommendationType.getPriority();
	}
	
	public Recommendation(int id, EnumRecommendationType type) 
	{
	    this.id = id;
	    this.recommendationTemplate = null;
	    this.recommendationType = type;
	    this.priority = recommendationType.getPriority();
	}

	@Override
	public EnumMessageType getType() {
		return EnumMessageType.RECOMMENDATION;
	}

	public int getPriority() {
		return priority;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Long createdOn) {
		this.createdOn = createdOn;
	}

	public String getImageLink() {
		return imageLink;
	}

	public void setImageLink(String imageLink) {
		this.imageLink = imageLink;
	}

	public EnumRecommendationTemplate getRecommendationTemplate() {
		return recommendationTemplate;
	}
	
	public EnumRecommendationType getRecommendationType() {
        return recommendationType;
    }
	
    public Long getAcknowledgedOn() {
        return acknowledgedOn;
    }

    public void setAcknowledgedOn(Long acknowledgedOn) {
        this.acknowledgedOn = acknowledgedOn;
    }
}
