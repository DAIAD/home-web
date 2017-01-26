package eu.daiad.web.model.message;

import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.NumberFormatter;
import eu.daiad.web.model.device.EnumDeviceType;

public class Alert extends Message
{
    public interface ParameterizedTemplate extends Message.Parameters
    {
        public EnumAlertTemplate getTemplate();
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
        final EnumAlertTemplate alertTemplate;

        // Provide some common parameters

        Integer integer1;

        Integer integer2;

        Double currency1;

        Double currency2;

        public SimpleParameterizedTemplate(
            DateTime refDate, EnumDeviceType deviceType, EnumAlertTemplate template)
        {
            super(refDate, deviceType);
            this.alertTemplate = template;
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
                pairs.put("currency1", new NumberFormatter(currency1, ".#"));
            if (currency2 != null)
                pairs.put("currency2", new NumberFormatter(currency2, ".#"));

            return pairs;
        }

        @Override
        public EnumAlertTemplate getTemplate()
        {
            return alertTemplate;
        }
    }

    private final int id;

	private final EnumAlertType alertType;

    private final EnumAlertTemplate alertTemplate;

	private int priority;

	private String title;

	private String description;

	private String link;

	private Long createdOn;

	private Long acknowledgedOn;

	public Alert(int id, EnumAlertTemplate template)
    {
        this.id = id;
        this.alertTemplate = template;
        this.alertType = template.getType();
        this.priority = alertType.getPriority();
    }

    public Alert(int id, EnumAlertType type)
    {
        this.id = id;
        this.alertTemplate = null;
        this.alertType = type;
        this.priority = alertType.getPriority();
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

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
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
