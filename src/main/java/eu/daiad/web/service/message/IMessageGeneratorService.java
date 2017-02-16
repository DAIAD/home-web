package eu.daiad.web.service.message;

import java.util.UUID;

import org.joda.time.LocalDateTime;

import java.util.List;

public interface IMessageGeneratorService 
{
	/**
     * Generates messages for all users of a utility.
     * 
     * @param config the configuration options
     * @param utilityKey
     */
    public void executeUtility(LocalDateTime refDate, Configuration config, UUID utilityKey);

    /**
     * Generates messages for a list of users.
     *
     * @param config the configuration options
     * @param accountKeys
     */
    public void executeAccounts(LocalDateTime refDate, Configuration config, List<UUID> accountKeys);

}
