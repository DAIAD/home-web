package eu.daiad.web.model.message;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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
        @NotNull
        protected DateTime refDate;

        @NotNull
        protected EnumDeviceType deviceType;

        @JsonIgnore
        public void setRefDate(DateTime refDate)
        {
            this.refDate = refDate;
        }

        @JsonProperty("refDate")
        public void setRefDate(long refDate)
        {
            this.refDate = new DateTime(refDate);
        }
        
        public void setDeviceType(EnumDeviceType deviceType)
        {
            this.deviceType = deviceType;
        }

        protected AbstractParameters() {}
        
        protected AbstractParameters(DateTime refDate, EnumDeviceType deviceType)
        {
            this.refDate = refDate;
            this.deviceType = deviceType;
        }

        @JsonIgnore
        @Override
        public DateTime getRefDate()
        {
            return refDate;
        }
        
        @JsonProperty("refDate")
        public long getRefDateAsInstant()
        {
            return refDate.getMillis();
        }
        
        @Override
        public EnumDeviceType getDeviceType()
        {
            return deviceType;
        }

        @JsonIgnore
        @Override
        public Map<String, Object> getParameters()
        {
            Map<String, Object> parameters = new HashMap<>();
            
            parameters.put("ref_date", refDate.toDate());
            
            parameters.put("device_type", deviceType);
            
            return parameters;
        }
    }

	public abstract EnumMessageType getType();

	public static final int INVALID_ID = -1;

	private final int id;

	protected String locale;

	protected String title;

	@JsonIgnore
	protected Long acknowledgedOn;

	@JsonIgnore
	protected Long createdOn;

	protected Message()
	{
	    this.id = INVALID_ID;
	}

	protected Message(int id)
	{
	    this.id = id;
	}

	public int getId()
	{
	    return id;
	}

	public String getLocale()
    {
        return locale;
    }

    public void setLocale(String locale)
    {
        this.locale = locale;
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

    public void setCreatedOn(long created)
    {
        this.createdOn = created;
    }

    @JsonIgnore
    public void setCreatedOn(DateTime created)
    {
        this.createdOn = created.getMillis();
    }

    public Long getAcknowledgedOn()
    {
        return acknowledgedOn;
    }

    public void setAcknowledgedOn(long acknowledged)
    {
        this.acknowledgedOn = acknowledged;
    }

    @JsonIgnore
    public void setAcknowledgedOn(DateTime acknowledged)
    {
        this.acknowledgedOn = acknowledged.getMillis();
    }

    /**
     * A message type returns whatever it considers to be the body of a message.
     */
    public abstract String getBody();
}
