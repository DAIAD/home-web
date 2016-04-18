package eu.daiad.web.service;

import org.joda.time.DateTimeZone;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.loader.ImportWaterMeterFileConfiguration;

public interface IFileDataLoaderService {

	public abstract void importWaterMeter(ImportWaterMeterFileConfiguration configuration) throws ApplicationException;

	public abstract void importRandomAmphiroSessions(String filename, String timezone) throws ApplicationException;

	public abstract void importRandomAmphiroSessions(String filename, DateTimeZone timezone)
					throws ApplicationException;

}
