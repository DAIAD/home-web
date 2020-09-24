package eu.daiad.utility.service.savings;

import java.io.IOException;

import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.loader.FileProcessingStatus;

/**
 * Provides methods for importing savings potential data files to HBase/PostegresqL.
 */
public interface ISavingsPotentialDataLoaderService {

	/**
	 * Loads savings potential data from files to HBase/PostgreSQL.
	 *
	 * @param scenarioId scenario id.
	 * @param jobId job id.
	 * @param savingsFilename the savings potential data file name.
	 * @param waterIqFilename the Water IQ data file name.
	 * @param timezone the time stamp time zone.
	 * @param hdfsPath HDFS path if the file is located on HDFS.
	 * @return statistics about the process execution.
	 *
	 * @throws ApplicationException if the file or the time zone is not found.
	 */
	FileProcessingStatus parseSavingsPotential(long scenarioId, long jobId, String savingsFilename, String waterIqFilename, String timezone, String hdfsPath) throws ApplicationException, IOException;

    /**
     * Loads Water IQ data from files to HBase/PostgreSQL.
     *
     * @param utilityId utility id.
     * @param jobId job id.
     * @param savingsFilename the savings potential data file name.
     * @param waterIqFilename the Water IQ data file name.
     * @param timezone the time stamp time zone.
     * @param hdfsPath HDFS path if the file is located on HDFS.
     * @return statistics about the process execution.
     *
     * @throws ApplicationException if the file or the time zone is not found.
     */
    FileProcessingStatus parseWaterIq(int utilityId, long jobId, String savingsFilename, String waterIqFilename, String timezone, String hdfsPath) throws ApplicationException, IOException;


}
