package eu.daiad.common.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.common.model.error.ActionErrorCode;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.ImportErrorCode;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.loader.EnumUploadFileType;
import eu.daiad.common.model.loader.FileProcessingStatus;
import eu.daiad.common.model.meter.WaterMeterDataRow;
import eu.daiad.common.model.meter.WaterMeterForecast;
import eu.daiad.common.model.meter.WaterMeterForecastCollection;
import eu.daiad.common.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.common.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.common.repository.application.IMeterDataRepository;
import eu.daiad.common.repository.application.IMeterForecastingDataRepository;

/**
 * Service that provides methods for importing smart water meter readings to HBASE.
 */
@Service
@Transactional
public class WaterMeterDataLoaderService extends BaseService implements IWaterMeterDataLoaderService {

    /**
     * Maximum number of rows to parse.
     */
    private static final int CHUNK_SIZE = 100000;

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(WaterMeterDataLoaderService.class);
	
    /**
     * Date format pattern.
     */
    private static final String dateFormatPattern = "dd/MM/yyyy HH:mm:ss";

    /**
     * Entity manager for persisting upload meta data.
     */
    @PersistenceContext
    EntityManager entityManager;

    /**
     * Repository for storing smart water meter readings to HBASE.
     */
    @Autowired
    IMeterDataRepository waterMeterMeasurementRepository;

    /**
     * Repository for storing forecasting data for water consumption.
     */
    @Autowired
    IMeterForecastingDataRepository waterMeterForecastRepository;

    /**
     * Loads smart water meter readings data from a file into HBASE.
     *
     * @param filename the file name.
     * @param timezone the time stamp time zone.
     * @param type of data being uploaded.
     * @param hdfsPath HDFS path if the file is located on HDFS.
     * @return statistics about the process execution.
     * @throws IOException in case an I/O exception occurs.
     *
     * @throws ApplicationException if the file or the time zone is not found.
     */
    @Override
    public FileProcessingStatus parse(String filename, String timezone, EnumUploadFileType type, String hdfsPath) throws ApplicationException, IOException {
        switch (type) {
            case METER_DATA:
                // HDFS is not supported
                if (!StringUtils.isBlank(hdfsPath)) {
                    throw createApplicationException(SharedErrorCode.FILESYSTEM_NOT_SUPPORTED).set("filesystem", "hdfs");
                }
                FileProcessingStatus status = parseMeterData(filename, timezone);

                String newFilename = renameFile(filename, timezone, status.getMinTimestamp(), status.getMaxTimestamp());
                if (!StringUtils.isBlank(newFilename)) {
                    status.setFilename(FilenameUtils.getName(newFilename));
                }

                return status;
            case METER_DATA_FORECAST:
                return parseMeterForecastData(filename, timezone, hdfsPath);
            default:
                throw createApplicationException(ActionErrorCode.FILE_TYPE_NOT_SUPPORTED).set("type", type);
        }
    }

    /**
     * Renames a file based on the time interval its data refer to.
     *
     * @param filename the initial filename.
     * @param timezone the time zone.
     * @param minTimestamp the minimum timestamp.
     * @param maxTimestamp the maximum timestamp.
     * @return the new filename.
     * @throws IOException in case an I/O exception occurs.
     */
    private String renameFile(String filename, String timezone, Long minTimestamp, Long maxTimestamp) throws IOException {
        if ((minTimestamp == null) || (maxTimestamp == null)) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.forID(timezone));

        String newFilename = String.format("%s_%s_%s",
                                           new DateTime(minTimestamp, DateTimeZone.UTC).toString(formatter),
                                           new DateTime(maxTimestamp, DateTimeZone.UTC).toString(formatter),
                                           FilenameUtils.getName(filename));

        newFilename = FilenameUtils.concat(FilenameUtils.getFullPath(filename), newFilename);

        FileUtils.moveFile(new File(filename), new File(newFilename));

        return newFilename;
    }

    /**
     * Parses a file with smart water meter data and imports the results to HBASE. The parse supports two formats:
     *
     * Format 1: Every line contains the following 6 tokens
     *
     * 1. Place
     * 2. Company
     * 3. Meter Id
     * 4. Date & Time
     * 5. Volume
     * 6. Difference
     *
     * Format 2:
     *
     * 1. Meter Id
     * 2. Date & Time
     * 3. Volume
     *
     * Both formats expect dates formatted using the pattern dd/MM/yyyy HH:mm:ss.
     *
     * @param filename the filename to parse.
     * @param timezone the time zone the dates refer to.
     * @param type the type of the uploaded file.
     * @return statistics about the process execution.
     * @throws ApplicationException in case validation fails or an I/O exception occurs.
     */
    private FileProcessingStatus parseMeterData(String filename, String timezone) throws ApplicationException {
    	WaterMeterDataRow row;
        String line = "";
        int lineIndex = 0;
        int chunkSize = 0;
        String lastSerial = null;

        // Check if file exists
        File file = new File(filename);
        if (!file.exists()) {
            throw createApplicationException(SharedErrorCode.RESOURCE_DOES_NOT_EXIST).set("resource", filename);
        }

        Scanner scan = null;

        FileProcessingStatus status = new FileProcessingStatus();

        // Set time zone
        Set<String> zones = DateTimeZone.getAvailableIDs();
        if ((StringUtils.isBlank(timezone)) || (!zones.contains(timezone))) {
            throw createApplicationException(SharedErrorCode.TIMEZONE_NOT_FOUND).set("timezone", timezone);
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormatPattern).withZone(DateTimeZone.forID(timezone));

        try {
            List<WaterMeterDataRow> rows = new ArrayList<WaterMeterDataRow>();

            scan = new Scanner(new File(filename));

            while (scan.hasNextLine()) {
                lineIndex++;
                line = scan.nextLine();

                String[] tokens = StringUtils.split(line, ";");

                if(chunkSize > CHUNK_SIZE) {
                    // Decide if all rows for the current serial have been parsed
                    boolean doImport = false;
                    switch (tokens.length) {
                        case 3:
                            doImport = !tokens[0].equals(lastSerial);
                            break;
                        case 6:
                            doImport = !tokens[2].equals(lastSerial);
                            break;
                        default:
                            break;
                    }
                    // Update and import row data
                    if(doImport) {
                        importMeterDataToHBase(rows, status, false);

                        rows = new ArrayList<WaterMeterDataRow>();
                        chunkSize = 0;
                    }
                }

                switch (tokens.length) {
                    case 3:
                        row = new WaterMeterDataRow();
                        row.serial = tokens[0];

                        try {
                            row.timestamp = formatter.parseDateTime(tokens[1]).getMillis();
                        } catch (Exception ex) {
                            logger.error(String.format("Failed to parse timestamp [%s] in line [%d] from file [%s].",
                                                       tokens[1], lineIndex, filename), ex);
                            status.skip();
                            continue;
                        }

                        try {
                            row.volume = Float.parseFloat(tokens[2]);
                        } catch (Exception ex) {
                            logger.error(String.format("Failed to parse volume [%s] in line [%d] from file [%s].",
                                                       tokens[2], lineIndex, filename), ex);
                            status.skip();
                            continue;
                        }

                        rows.add(row);
                        chunkSize++;
                        lastSerial = row.serial;
                        break;
                    case 6:
                        row = new WaterMeterDataRow();
                        row.serial = tokens[2];

                        try {
                            row.timestamp = formatter.parseDateTime(tokens[3]).getMillis();
                        } catch (Exception ex) {
                            logger.error(String.format("Failed to parse timestamp [%s] in line [%d] from file [%s].",
                                                       tokens[3], lineIndex, filename), ex);
                            status.skip();
                            continue;
                        }

                        try {
                            row.volume = Float.parseFloat(tokens[4]);
                        } catch (Exception ex) {
                            logger.error(String.format("Failed to parse volume [%s] in line [%d] from file [%s].",
                                                       tokens[4], lineIndex, filename), ex);
                            status.skip();
                            continue;
                        }

                        try {
                            row.difference = Float.parseFloat(tokens[5]);
                        } catch (Exception ex) {
                            logger.debug(String.format("Failed to parse difference [%s] in line [%d] from file [%s].",
                                                       tokens[5], lineIndex, filename), ex);
                            // Do not skip row. Difference value will be overridden.
                            row.difference = 0f;
                        }

                        rows.add(row);
                        chunkSize++;
                        lastSerial = row.serial;
                        break;
                    default:
                        // Row format is not supported
                        status.skip();
                }
            }

            scan.close();
            scan = null;

            status.setTotalRows(lineIndex);

            // Update and import row data
            importMeterDataToHBase(rows, status, false);

            if ((status.getProcessedRows() > 0) && ((status.getIgnoredRows() / status.getProcessedRows()) > 0.05)) {
                throw createApplicationException(ImportErrorCode.TOO_MANY_ERRORS)
                    .set("errors", status.getIgnoredRows())
                    .set("total", status.getProcessedRows());
            }

        } catch (FileNotFoundException fileEx) {
            logger.error(String.format("File [%s] was not found.", filename), fileEx);
        } finally {
            if (scan != null) {
                scan.close();
            }
        }

        return status;
    }

    @Override
    public void importMeterDataToHBase(List<WaterMeterDataRow> rows, FileProcessingStatus status, boolean ignoreNegativeDiff) {
        if(rows.isEmpty()) {
            return;
        }

        // Sort data rows by date time
        Collections.sort(rows, new Comparator<WaterMeterDataRow>() {

            @Override
            public int compare(WaterMeterDataRow r1, WaterMeterDataRow r2) {
                int result = r1.serial.compareTo(r2.serial);

                if (result == 0) {
                    if (r1.timestamp < r2.timestamp) {
                        return -1;
                    } else if (r1.timestamp > r2.timestamp) {
                        return 1;
                    }
                    return 0;
                } else {
                    return result;
                }
            }
        });

        // Get time stamp range
        for (int i = 0, count = rows.size(); i < count; i++) {
            if ((status.getMinTimestamp() == null) || (status.getMinTimestamp() > rows.get(i).timestamp)) {
                status.setMinTimestamp(rows.get(i).timestamp);
            }
            if ((status.getMaxTimestamp() == null) || (status.getMaxTimestamp() < rows.get(i).timestamp)) {
                status.setMaxTimestamp(rows.get(i).timestamp);
            }
        }

        // Compute any missing values
        for (int i = 0, count = rows.size(); i < count; i++) {
            // Set difference for the first row for every unique serial number
            if ((i == 0) || (!rows.get(i).serial.equals(rows.get(i - 1).serial))) {
                WaterMeterStatusQueryResult meterStatus = waterMeterMeasurementRepository.getStatusBefore(
            		new String[] { rows.get(i).serial },
        			rows.get(i).timestamp - 1
    			);

                if ((meterStatus == null) || (meterStatus.getDevices().isEmpty())) {
                    rows.get(i).difference = 0f;
                } else {
                    rows.get(i).difference = rows.get(i).volume - meterStatus.getDevices().get(0).getVolume();

					if (rows.get(i).difference < 0) {
						status.negativeDiff();
						if (ignoreNegativeDiff) {
							rows.get(i).difference = 0f;
						}
					}
				}
            } else if ((rows.get(i).serial.equals(rows.get(i - 1).serial)) && (rows.get(i).difference == null)) {
                rows.get(i).difference = rows.get(i).volume - rows.get(i - 1).volume;

                if (rows.get(i).difference < 0) {
                    status.negativeDiff();
					if (ignoreNegativeDiff) {
						rows.get(i).difference = 0f;
					}
                }
            }

            // Validate difference
            if ((i != 0) && (rows.get(i).difference != null) && (rows.get(i).serial.equals(rows.get(i - 1).serial))) {
                rows.get(i).difference = rows.get(i).volume - rows.get(i - 1).volume;
                if (rows.get(i).difference < 0) {
                    status.negativeDiff();
					if (ignoreNegativeDiff) {
						rows.get(i).difference = 0f;
					}
                }
            }
        }

        // Import rows to HBASE
        for (WaterMeterDataRow row : rows) {
            insert(status, row);
        }
    }

    /**
     * Imports a single of meter reading to HBASE.
     *
     * @param status statistics about the process execution.
     * @param row the data to import.
     */
    private void insert(FileProcessingStatus status, WaterMeterDataRow row) {
        try {
            WaterMeterMeasurementCollection data = new WaterMeterMeasurementCollection();

            data.add(row.timestamp, row.volume, row.difference);

            waterMeterMeasurementRepository.store(row.serial, data);
        } catch (Exception ex) {
            logger.warn(String.format("Failed to import row %s;%d;%f;%f. Reason:\n", row.serial, row.timestamp, row.volume, row.difference), ex);
            status.ignore();
        }
        status.process();
    }

    /**
     * Parses a file with meter forecasting data and imports its data to HBASE.
     *
     * @param filename the file name.
     * @param timezone the timestamp time zone.
     * @param hdfsPath Optional HDFS path.
     * @return the result of the operation.
     * @throws ApplicationException if an error occurs.
     */
    private FileProcessingStatus parseMeterForecastData(String filename, String timezone, String hdfsPath) throws ApplicationException {
        // Initialize status
        FileProcessingStatus status = new FileProcessingStatus();

        try {
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

            // Set time zone
            Set<String> zones = DateTimeZone.getAvailableIDs();
            if ((StringUtils.isBlank(timezone)) || (!zones.contains(timezone))) {
                throw createApplicationException(SharedErrorCode.TIMEZONE_NOT_FOUND).set("timezone", timezone);
            }

            DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormatPattern).withZone(DateTimeZone.forID(timezone));

            // I14FA065940;25/06/2016 08:00:00;4.26
            String line = "";

            int index = 0;

            if (StringUtils.isBlank(hdfsPath)) {
                // Parse local file
                try (Scanner scan = new Scanner(new File(filename))) {
                    while (scan.hasNextLine()) {
                        line = scan.nextLine();

                        index++;
                        parseForecastingDataLine(index, line, formatter, filename, status);
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
                        parseForecastingDataLine(index, line, formatter, filename, status);

                        line = reader.readLine();
                    }
                }
            }
        } catch (FileNotFoundException fileEx) {
            throw wrapApplicationException(fileEx, SharedErrorCode.RESOURCE_DOES_NOT_EXIST).set("resource", filename);
        } catch (ApplicationException appEx) {
            throw appEx;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return status;
    }

    /**
     * Parses a line with meter forecasting data and imports results to HBase.
     *
     * @param index the current line index.
     * @param line the line data.
     * @param formatter date time formatter for parsing dates.
     * @param filename the input filename.
     * @param status the process status.
     */
    private void parseForecastingDataLine(int index, String line, DateTimeFormatter formatter, String filename, FileProcessingStatus status) {
        if(StringUtils.isBlank(line)) {
            return;
        }
        float difference;

        String[] tokens = StringUtils.split(line, ";");
        if (tokens.length != 3) {
            status.skip();
            return;
        }

        String serial = tokens[0];

        DateTime timestamp;
        try {
            timestamp = formatter.parseDateTime(tokens[1]);
        } catch (Exception ex) {
            logger.error(String.format("Failed to parse timestamp [%s] in line [%d] from file [%s].",
                                        tokens[1], index, filename), ex);
            status.skip();
            return;
        }

        try {
            difference = Float.parseFloat(tokens[2]);
        } catch (Exception ex) {
            logger.error(String.format("Failed to parse difference [%s] in line [%d] from file [%s].",
                                       tokens[2], index, filename), ex);
            status.skip();
            return;
        }

        importForecastingDataToHBase(serial, timestamp, difference);

        status.process();
    }

    /**
     * Imports a row with meter forecasting data to HBase.
     *
     * @param serial meter serial number.
     * @param timestamp date and time of the meter reading.
     * @param difference water consumption since the last reading.
     */
    private void importForecastingDataToHBase(String serial, DateTime timestamp, float difference) {
        WaterMeterForecastCollection data = new WaterMeterForecastCollection();
        ArrayList<WaterMeterForecast> measurements = new ArrayList<WaterMeterForecast>();
        WaterMeterForecast measurement = new WaterMeterForecast();

        measurement.setTimestamp(timestamp.getMillis());
        measurement.setDifference(difference);

        measurements.add(measurement);

        data.setMeasurements(measurements);

        waterMeterForecastRepository.store(serial, data);
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

}
