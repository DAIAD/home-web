package eu.daiad.web.service.message;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.springframework.util.Assert;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.utility.UtilityInfo;

public abstract class AbstractMessageResolver <P extends Message.Parameters>
    implements IMessageResolver<P>
{
    protected DateTime refDate;
    
    protected Configuration config;
    
    protected ConsumptionStats stats;
    
    @Override
    public void setup(Configuration config, UtilityInfo utility, ConsumptionStats stats)
    {
        Assert.state(config != null);
        this.config = config;
   
        LocalDateTime refDate = config.getRefDate();
        Assert.state(refDate != null, "Expected a reference date");
        DateTimeZone tz = DateTimeZone.forID(utility.getTimezone());
        this.refDate = refDate.toDateTime(tz);
        
        Assert.state(stats != null);
        this.stats = stats;
    }
    
    @Override
    public void teardown()
    {
        // no-op
    }
    
    @Override
    public boolean supports(EnumDeviceType deviceType)
    {
        return true;
    }
}
