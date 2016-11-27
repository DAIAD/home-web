package eu.daiad.web.model.message;

import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.DateFormatter;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert.AbstractParameters;
import eu.daiad.web.model.message.Alert.CommonParameters;

public class DynamicRecommendation extends Message 
{
    public interface Parameters extends Message.Parameters
    {
        public EnumDynamicRecommendationType getType();
    }
    
    public abstract static class AbstractParameters extends Message.AbstractParameters implements Parameters
    {
        protected AbstractParameters(DateTime refDate, EnumDeviceType deviceType)
        {
            super(refDate, deviceType);
        }

        @Override
        public EnumDynamicRecommendationType getType()
        {
            return EnumDynamicRecommendationType.UNDEFINED;
        }
    }
    
    public static class CommonParameters extends AbstractParameters
    {
        final EnumDynamicRecommendationType recommendationType;
        
        // Provide some common parameters
        
        Integer integer1;
        
        Integer integer2;
        
        Double currency1;
        
        Double currency2;
        
        public CommonParameters(
            DateTime refDate, EnumDeviceType deviceType, EnumDynamicRecommendationType recommendationType)
        {
            super(refDate, deviceType);
            this.recommendationType = recommendationType;   
        }

        public Integer getInteger1()
        {
            return integer1;
        }

        public CommonParameters setInteger1(Integer integer1)
        {
            this.integer1 = integer1;
            return this;
        }

        public Integer getInteger2()
        {
            return integer2;
        }

        public CommonParameters setInteger2(Integer integer2)
        {
            this.integer2 = integer2;
            return this;
        }

        public Double getCurrency1()
        {
            return currency1;
        }

        public CommonParameters setCurrency1(Double currency1)
        {
            this.currency1 = currency1;
            return this;
        }

        public Double getCurrency2()
        {
            return currency2;
        }

        public CommonParameters setCurrency2(Double currency2)
        {
            this.currency2 = currency2;
            return this;
        }  
        
        @Override
        public Map<String, Object> getPairs()
        {
            Map<String, Object> pairs = super.getPairs();
            
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
        public EnumDynamicRecommendationType getType()
        {
            return recommendationType;
        }
    }
    
    private final int id;

	private EnumDynamicRecommendationType recommendationType;

	private int priority;

	private String title;

	private String description;

	private String imageLink;

	private Long createdOn;

    private Long acknowledgedOn;
	
	public DynamicRecommendation(EnumDynamicRecommendationType recommendationType, int id) 
	{
		this.recommendationType = recommendationType;
		this.id = id;
	}

	@Override
	public EnumMessageType getType() {
		return EnumMessageType.RECOMMENDATION_DYNAMIC;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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

	public EnumDynamicRecommendationType getRecommendationType() {
		return recommendationType;
	}
	
    public Long getAcknowledgedOn() {
        return acknowledgedOn;
    }

    public void setAcknowledgedOn(Long acknowledgedOn) {
        this.acknowledgedOn = acknowledgedOn;
    }
}
