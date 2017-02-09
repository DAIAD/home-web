package eu.daiad.web.service.message;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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
    private static final Set<EnumDeviceType> deviceTypes = 
        EnumSet.of(EnumDeviceType.METER, EnumDeviceType.AMPHIRO);
    
    protected DateTime refDate;
    
    protected Configuration config;
    
    protected UtilityInfo utility;
    
    protected ConsumptionStats stats;
    
    @Override
    public void setup(Configuration config, IGeneratorContext generatorContext)
    {
        Assert.state(config != null);
        this.config = config;
   
        this.refDate = generatorContext.getRefDate();
        this.stats = generatorContext.getStats();
    }
    
    @Override
    public void teardown()
    {
        // no-op
    }
    
    @Override
    public Set<EnumDeviceType> getSupportedDevices()
    {
        return Collections.unmodifiableSet(deviceTypes);
    }
}
