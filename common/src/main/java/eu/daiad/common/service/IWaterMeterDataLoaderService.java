package eu.daiad.common.service;

import java.io.IOException;
import java.util.List;

import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.loader.EnumUploadFileType;
import eu.daiad.common.model.loader.FileProcessingStatus;
import eu.daiad.common.model.meter.WaterMeterDataRow;

/**
 * Provides methods for importing smart water meter readings to HBASE.
 */
public interface IWaterMeterDataLoaderService {

	/**
	 * Loads smart water meter readings data from a file into HBASE.
	 *
	 * @param filename the file name.
	 * @param timezone the time stamp time zone.
	 * @param type of data being uploaded
	 * @param hdfsPath HDFS path if the file is located on HDFS.
	 * @return statistics about the process execution.
	 *
	 * @throws ApplicationException if the file or the time zone is not found.
	 */
	FileProcessingStatus parse(String filename, String timezone, EnumUploadFileType type, String hdfsPath) throws ApplicationException, IOException;

    /**
     * Validates, processes and imports a list of meter readings to HBASE.
     *
     * @param rows the data to import.
     * @param status statistics about the process execution. 
     * @param ignoreNegativeDiff ignore negative difference values
     */
	void importMeterDataToHBase(List<WaterMeterDataRow> rows, FileProcessingStatus status, boolean ignoreNegativeDiff);

}
