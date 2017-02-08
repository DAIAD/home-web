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
    
    protected UtilityInfo utility;
    
    protected ConsumptionStats stats;
    
    @Override
    public void setup(Configuration config, IGeneratorContext context)
    {
        Assert.state(config != null);
        this.config = config;
   
        this.refDate = context.getRefDate();
        this.stats = context.getStats();
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
