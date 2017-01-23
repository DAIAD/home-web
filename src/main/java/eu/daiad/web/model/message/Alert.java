package eu.daiad.web.model.message;

import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.device.EnumDeviceType;

public class Alert extends Message 
{
    public interface ParameterizedTemplate extends Message.Parameters
    {
        public EnumAlertType getType();
    }
    
    public abstract static class AbstractParameterizedTemplate extends Message.AbstractParameters 
        implements ParameterizedTemplate
    {
        protected AbstractParameterizedTemplate(DateTime refDate, EnumDeviceType deviceType)
        {
            super(refDate, deviceType);
        }

        @Override
        public EnumAlertType getType()
        {
            return EnumAlertType.UNDEFINED;
        }
    }
    
    public static class SimpleParameterizedTemplate extends AbstractParameterizedTemplate
    {
        final EnumAlertType alertType;
        
        // Provide some common parameters
        
        Integer integer1;
        
        Integer integer2;
        
        Double currency1;
        
        Double currency2;
        
        public SimpleParameterizedTemplate(DateTime refDate, EnumDeviceType deviceType, EnumAlertType alertType)
        {
            super(refDate, deviceType);
            this.alertType = alertType;   
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
        public EnumAlertType getType()
        {
            return alertType;
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
