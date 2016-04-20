package eu.daiad.web.service;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.loader.DataTransferConfiguration;
import eu.daiad.web.model.loader.FileProcessingStatus;

public interface IWaterMeterDataLoaderService {

	public abstract void load(DataTransferConfiguration config);

	public abstract FileProcessingStatus parse(String filename, String timezone) throws ApplicationException;

	public abstract void cancel();

	public abstract boolean isCancelled();

}
