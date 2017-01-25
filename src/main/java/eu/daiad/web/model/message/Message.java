package eu.daiad.web.model.message;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import eu.daiad.web.model.DateFormatter;
import eu.daiad.web.model.device.EnumDeviceType;

public abstract class Message
{
    public interface Parameters
    {
        public EnumDeviceType getDeviceType();

        public DateTime getRefDate();

        public Map<String, Object> getParameters();
    }

    public abstract static class AbstractParameters implements Parameters
    {
        protected final DateTime refDate;

        protected final EnumDeviceType deviceType;

        protected AbstractParameters(DateTime refDate, EnumDeviceType deviceType)
        {
            this.refDate = refDate;
            this.deviceType = deviceType;
        }

        @Override
        public DateTime getRefDate()
        {
            return refDate;
        }

        @Override
        public EnumDeviceType getDeviceType()
        {
            return deviceType;
        }

        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> p = new HashMap<>();
            p.put("ref_date", new DateFormatter(refDate));
            p.put("device_type", deviceType);
            return p;
        }
    }

	public abstract EnumMessageType getType();

	private final int id;

	protected String title;

	protected Long acknowledgedOn;

	protected Long createdOn;

	protected Message(int id)
	{
	    this.id = id;
	}

	public int getId()
	{
	    return id;
	}

	public String getTitle()
	{
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Long getCreatedOn()
    {
        return createdOn;
    }

    public void setCreatedOn(DateTime created)
    {
        this.createdOn = created.getMillis();
    }

    public Long getAcknowledgedOn()
    {
        return acknowledgedOn;
    }

    public void setAcknowledgedOn(DateTime acknowledged)
    {
        this.acknowledgedOn = acknowledged.getMillis();
    }

    /**
     * A message type returns whatever it considers to be the body of a message.
     */
    public abstract String getBody();
}
