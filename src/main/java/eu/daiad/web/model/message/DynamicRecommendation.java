package eu.daiad.web.model.message;

import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.DateFormatter;
import eu.daiad.web.model.device.EnumDeviceType;

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
