package eu.daiad.web.service.message;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageResolutionStatus;

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
     * @return a set of {@link EnumDeviceType}.
     */
    public Set<EnumDeviceType> getSupportedDevices();

    /**
     * Examine a particular account and generate messages.
     *
     * @param accountKey
     * @return a list of {@link MessageResolutionStatus}.
     */
    public List<MessageResolutionStatus<P>> resolve(UUID accountKey, EnumDeviceType deviceType);
}

