package eu.daiad.web.service;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.loader.DataTransferConfiguration;
import eu.daiad.web.model.loader.EnumUploadFileType;
import eu.daiad.web.model.loader.FileProcessingStatus;

public interface IWaterMeterDataLoaderService {

	/**
	 * Downloads one or more files with smart water meter readings from a remote SFTP server, parses the files
	 * and inserts data in HBASE based on the given configuration.
	 * 
	 * @param config the configuration
	 */
	public abstract void load(DataTransferConfiguration config);

	/**
	 * Loads smart water meter readings data from a file into HBASE.
	 * 
	 * @param filename the file name.
	 * @param timezone the time stamp time zone.
	 * @param type of data being uploaded
	 * @return statistics about the process execution.
	 * 
	 * @throws ApplicationException if the file or the time zone is not found.
	 */
	public abstract FileProcessingStatus parse(String filename, String timezone, EnumUploadFileType type) throws ApplicationException;

}
