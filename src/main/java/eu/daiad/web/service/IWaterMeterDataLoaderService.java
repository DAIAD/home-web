package eu.daiad.web.service;

import eu.daiad.web.model.loader.DataTransferConfiguration;

public interface IWaterMeterDataLoaderService {

	abstract void load(DataTransferConfiguration config);

	abstract void cancel();

	abstract boolean isCancelled();

}
