package eu.daiad.web.service;

import org.joda.time.DateTimeZone;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.loader.ImportWaterMeterFileConfiguration;

public interface IFileDataLoaderService {

	/**
	 * Assigns smart water meters to user accounts based on the given configuration.
	 *
	 * @param configuration the configuration
	 * @throws ApplicationException if input file is not found or a user is not found.
	 */
	public abstract void importWaterMeter(ImportWaterMeterFileConfiguration configuration) throws ApplicationException;

	/**
	 * Inserts random showers to all devices of users for a specific utility. The data generation is driven
	 * by an external file that contains real showers.
	 *
	 * @param utilityId the utility id.
	 * @param filename the file name with showers.
	 * @param timezone the utility time zone.
	 * @throws ApplicationException if the file or the utility is not found.
	 */
	public abstract void importRandomAmphiroSessions(int utilityId, String filename, String timezone)
					throws ApplicationException;

	/**
	 * Inserts random showers to all devices of users for a specific utility. The data generation is driven
	 * by an external file that contains real showers.
	 *
	 * @param utilityId the utility id.
	 * @param filename the file name with showers.
	 * @param timezone the utility time zone.
	 * @throws ApplicationException if the file or the utility is not found.
	 */
	public abstract void importRandomAmphiroSessions(int utilityId, String filename, DateTimeZone timezone)
					throws ApplicationException;

}
