package eu.daiad.web.service;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.loader.ImportWaterMeterFileConfiguration;

public interface IFileDataLoaderService {

	public abstract void importWaterMeter(ImportWaterMeterFileConfiguration configuration) throws ApplicationException;

	public abstract void importRandomAmphiroSessions(String filename) throws ApplicationException;

}
