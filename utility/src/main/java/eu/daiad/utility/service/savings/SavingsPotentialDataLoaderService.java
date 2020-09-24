package eu.daiad.utility.service.savings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.loader.FileProcessingStatus;
import eu.daiad.common.model.query.savings.SavingsCluster;
import eu.daiad.common.model.query.savings.SavingsClusterCollection;
import eu.daiad.common.model.query.savings.SavingsClusterMonth;
import eu.daiad.common.repository.application.ISavingsPotentialRepository;
import eu.daiad.common.service.BaseService;

/**
 * Service that provides methods for importing smart water meter readings to HBASE.
 */
@Service
public class SavingsPotentialDataLoaderService extends BaseService implements ISavingsPotentialDataLoaderService {

    /**
     * Repository for accessing savings potential and water IQ data.
     */
    @Autowired
    ISavingsPotentialRepository savingsPotentialRepository;

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
    @Override
    public FileProcessingStatus parseSavingsPotential(long scenarioId, long jobId, String savingsFilename, String waterIqFilename, String timezone, String hdfsPath) throws ApplicationException, IOException {
        // Initialize status
        FileProcessingStatus status = new FileProcessingStatus();

        try {
            SavingsClusterCollection clusters = parse(jobId, savingsFilename, waterIqFilename, timezone, hdfsPath, status);

            // Store clusters and consumer data
            storeSavings(scenarioId, jobId, clusters);
        } catch (ApplicationException appEx) {
            throw appEx;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return status;
    }

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
    @Override
    public FileProcessingStatus parseWaterIq(int utilityId, long jobId, String savingsFilename, String waterIqFilename, String timezone, String hdfsPath) throws ApplicationException, IOException {
        // Initialize status
        FileProcessingStatus status = new FileProcessingStatus();

        try {
            SavingsClusterCollection clusters = parse(jobId, savingsFilename, waterIqFilename, timezone, hdfsPath, status);

            // Store clusters and consumer data
            storeWaterIq(utilityId, jobId, clusters);
        } catch (ApplicationException appEx) {
            throw appEx;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return status;
    }

    private SavingsClusterCollection parse(long jobId, String savingsFilename, String waterIqFilename, String timezone, String hdfsPath, FileProcessingStatus status) throws ApplicationException, IOException {
        ensureFilename(savingsFilename, hdfsPath);
        ensureFilename(waterIqFilename, hdfsPath);

        // Set time zone
        Set<String> zones = DateTimeZone.getAvailableIDs();
        if ((StringUtils.isBlank(timezone)) || (!zones.contains(timezone))) {
            throw createApplicationException(SharedErrorCode.TIMEZONE_NOT_FOUND).set("timezone", timezone);
        }

        SavingsClusterCollection clusters = new SavingsClusterCollection();

        // Savings Potential line: Cluster2;1;40217.619;16.8917%
        String filename = savingsFilename;
        String line = "";
        int index = 0;

        if (StringUtils.isBlank(hdfsPath)) {
            // Parse local file
            try (Scanner scan = new Scanner(new File(filename))) {
                while (scan.hasNextLine()) {
                    line = scan.nextLine();
                    index++;

                    parseSavingsPotentialLine(clusters, index, line, filename, status);
                }

                status.setTotalRows(index);
            }
        } else {
            // Parse HDFS file
            FileSystem hdfsFileSystem = getHdfsFilesystem(hdfsPath);

            try (BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(hdfsFileSystem.open(new org.apache.hadoop.fs.Path(filename))))) {
                line = reader.readLine();
                while (line != null) {
                    index++;
                    parseSavingsPotentialLine(clusters, index, line, filename, status);

                    line = reader.readLine();
                }
            }
        }
        // WaterIq line: 1;Cluster5;D13IA514382;F;7860.08
        filename = waterIqFilename;
        line = "";
        index = 0;

        if (StringUtils.isBlank(hdfsPath)) {
            // Parse local file
            try (Scanner scan = new Scanner(new File(filename))) {
                while (scan.hasNextLine()) {
                    line = scan.nextLine();
                    index++;

                    parseWaterIqLine(clusters, index, line, filename, status);
                }

                status.setTotalRows(index);
            }
        } else {
            // Parse HDFS file
            FileSystem hdfsFileSystem = getHdfsFilesystem(hdfsPath);

            try (BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(hdfsFileSystem.open(new org.apache.hadoop.fs.Path(filename))))) {
                line = reader.readLine();
                while (line != null) {
                    index++;
                    parseWaterIqLine(clusters, index, line, filename, status);

                    line = reader.readLine();
                }
            }
        }

        return clusters;
    }

    /**
     * Parses a line with savings potential data. The line has the format {@code Cluster2;1;40217.619;16.8917%}. The fields are:
     *
     * Cluster name
     * Month index
     * Total savings
     * Savings percent
     *
     * @param clusters collection of {@link Cluster.
     * @param index the current line index.
     * @param line the line data.
     * @param filename the input filename.
     * @param status the process status.
     */
    private void parseSavingsPotentialLine(SavingsClusterCollection clusters, int index, String line, String filename, FileProcessingStatus status) {
        if(StringUtils.isBlank(line)) {
            return;
        }

        String[] tokens = StringUtils.split(line, ";");
        if (tokens.length != 4) {
            status.skipRow();
            return;
        }

        SavingsCluster cluster = clusters.getByName(tokens[0]);
        cluster.add(Integer.parseInt(tokens[1]),
                    Double.parseDouble(tokens[2]),
                    Double.parseDouble(tokens[3].substring(0, tokens[3].length() - 1)));

        status.processRow();
    }

    /**
     * Parses a line with Water IQ data. The line has the format {@code 1;Cluster5;D13IA514382;F;7860.08}. The fields are:
     *
     * Month index
     * Cluster name
     * SWM serial number
     * Water IQ
     * Deviation from the average cluster consumption.
     *
     * @param clusters collection of {@link Cluster.
     * @param index the current line index.
     * @param line the line data.
     * @param filename the input filename.
     * @param status the process status.
     */
    private void parseWaterIqLine(SavingsClusterCollection clusters, int index, String line, String filename, FileProcessingStatus status) {
        if(StringUtils.isBlank(line)) {
            return;
        }

        String[] tokens = StringUtils.split(line, ";");
        if (tokens.length != 5) {
            status.skipRow();
            return;
        }

        SavingsCluster cluster = clusters.getByName(tokens[1]);
        SavingsClusterMonth month = cluster.getByIndex(Integer.parseInt(tokens[0]));

        month.add(tokens[2],
                  tokens[3],
                  Double.parseDouble(tokens[4]));

        status.processRow();
    }

    /**
     * Stores savings potential data to data store.
     *
     * @param scenarioId scenario id
     * @param jobId job id
     * @param clusters the clusters.
     */
    private void storeSavings(long scenarioId, long jobId, SavingsClusterCollection clusters) {
        savingsPotentialRepository.storeSavings(scenarioId, jobId, clusters);
    }

    /**
     * Stores Water IQ data to data store.
     *
     * @param utilityId utility id
     * @param jobId job id
     * @param clusters the clusters.
     */
    private void storeWaterIq(int utilityId, long jobId, SavingsClusterCollection clusters) {
        savingsPotentialRepository.storeWaterIq(utilityId, jobId, clusters);
    }

    /**
     * Gets HDFS file system.
     *
     * @param hdfsPath HDFS path.
     * @return a configured file system.
     * @throws IOException if an I/O exception occurs.
     */
    private FileSystem getHdfsFilesystem(String hdfsPath) throws IOException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsPath);

        return FileSystem.get(conf);
    }


    /**
     * Checks if a file exists.
     *
     * @param filename the file name.
     * @param hdfsPath the HDFS path.
     * @throws IOException if an I/O exception occurs.
     * @throws ApplicationException if the file does not exist.
     */
    private void ensureFilename(String filename, String hdfsPath) throws ApplicationException, IOException {
        if (StringUtils.isBlank(hdfsPath)) {
            // Local file
            File file = new File(filename);
            if (!file.exists()) {
                throw createApplicationException(SharedErrorCode.RESOURCE_DOES_NOT_EXIST).set("resource", filename);
            }

        } else {
            FileSystem hdfsFileSystem = getHdfsFilesystem(hdfsPath);

            if (!hdfsFileSystem.exists(new org.apache.hadoop.fs.Path(filename))) {
                throw createApplicationException(SharedErrorCode.RESOURCE_DOES_NOT_EXIST).set("resource", filename);
            }

        }
    }

}
