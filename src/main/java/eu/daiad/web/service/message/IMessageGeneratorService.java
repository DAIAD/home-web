package eu.daiad.web.service.message;

import java.util.UUID;
import java.util.List;

public interface IMessageGeneratorService 
{
	/**
     * Generates messages for all users of a utility.
     * 
     * @param config the configuration options
     * @param utilityKey
     */
    public abstract void executeUtility(Configuration config, UUID utilityKey);

    /**
     * Generates messages for a list of users.
     *
     * @param config the configuration options
     * @param accountKeys
     */
    public abstract void executeAccounts(Configuration config, List<UUID> accountKeys);

}
