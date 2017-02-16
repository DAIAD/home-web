package eu.daiad.web.service.message;

import java.util.UUID;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.utility.UtilityInfo;

public interface IMessageResolver <P extends Message.Parameters>
{
    /**
     * Setup message resolution context.
     * This will be invoked before any account-specific resolution takes place.
     */
    public void setup(Configuration config, IGeneratorContext generatorContext);

    /**
     * The opposite of setup, provided only for cleanup purposes. This will be invoked 
     * after all account-specific resolution takes place.
     */
    public void teardown();

    /**
     * Provide the set of device types this resolver can support
     * 
     * @return
     */
    public Set<EnumDeviceType> getSupportedDevices();

    /**
     * Examine a particular account and generate messages.
     * 
     * @param accountKey
     * @return
     */
    public List<MessageResolutionStatus<P>> resolve(UUID accountKey, EnumDeviceType deviceType);
}

