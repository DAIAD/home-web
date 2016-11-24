package eu.daiad.web.model.message;

import org.joda.time.DateTime;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.DynamicRecommendation.Parameters;

public class Alert extends Message 
{
    public interface Parameters extends Message.Parameters
    {
        public EnumAlertType getType();
    }
    
    public abstract static class AbstractParameters extends Message.AbstractParameters implements Parameters
    {
        protected AbstractParameters(DateTime refDate, EnumDeviceType deviceType)
        {
            super(refDate, deviceType);
        }

        @Override
        public EnumAlertType getType()
        {
            return EnumAlertType.UNDEFINED;
        }
    }
    
    private final int id;

	private EnumAlertType alertType;

	private int priority;

	private String title;

	private String description;

	private String imageLink;

	private Long createdOn;
	
	private Long acknowledgedOn;

	public Alert(EnumAlertType alertType, int id) 
	{
		this.alertType = alertType;
		this.id = id;
	}

	@Override
	public EnumMessageType getType() {
		return EnumMessageType.ALERT;
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

	public EnumAlertType getAlertType() {
		return alertType;
	}
	
	public Long getAcknowledgedOn() {
	    return acknowledgedOn;
	}

	public void setAcknowledgedOn(Long acknowledgedOn) {
	    this.acknowledgedOn = acknowledgedOn;
	}
}
