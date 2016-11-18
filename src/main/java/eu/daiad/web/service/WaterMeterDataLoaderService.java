package eu.daiad.web.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.connector.RemoteFileAttributes;
import eu.daiad.web.connector.SecureFileTransferConnector;
import eu.daiad.web.domain.admin.UploadEntity;
import eu.daiad.web.model.error.ActionErrorCode;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.loader.DataTransferConfiguration;
import eu.daiad.web.model.loader.EnumUploadFileType;
import eu.daiad.web.model.loader.FileProcessingStatus;
import eu.daiad.web.model.meter.WaterMeterForecast;
import eu.daiad.web.model.meter.WaterMeterForecastCollection;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.repository.application.IWaterMeterForecastRepository;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;

/**
 * Service that provides methods for importing smart water meter readings to HBASE.
 */
@Service
@Transactional("managementTransactionManager")
public class WaterMeterDataLoaderService extends BaseService implements IWaterMeterDataLoaderService {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(WaterMeterDataLoaderService.class);

    /**
     * Date format pattern.
     */
    private static final String dateFormatPattern = "dd/MM/yyyy HH:mm:ss";

    /**
     * Secure FTP connection.
     */
    @Autowired
    SecureFileTransferConnector sftConnector;

    /**
     * Entity manager for persisting upload meta data.
     */
    @PersistenceContext(unitName = "management")
    EntityManager entityManager;

    /**
     * Repository for storing smart water meter readings to HBASE.
     */
    @Autowired
    IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

    /**
     * Repository for storing forecasting data for water consumption.
     */
    @Autowired
    IWaterMeterForecastRepository waterMeterForecastRepository;

    /**
     * Downloads one or more files with smart water meter readings from a remote SFTP server, parses the files
     * and inserts data in HBASE based on the given configuration.
     *
     * @param config the configuration
     */
    @Override
    public void load(DataTransferConfiguration config) {
        try {
            // Create local folder
            FileUtils.forceMkdir(new File(config.getLocalFolder()));

            // Set time zone
            Set<String> zones = DateTimeZone.getAvailableIDs();
            if (config.getTimezone() == null) {
                config.setTimezone("UTC");
            }
            if (!zones.contains(config.getTimezone())) {
                throw new ExportException(String.format("Time zone [%s] is not supported.", config.getTimezone()));
            }

            // Construct regular expression for filtering file names
            Pattern allowedFilenames = null;
            if (!StringUtils.isBlank(config.getFilterRegEx())) {
                allowedFilenames = Pattern.compile(config.getFilterRegEx());
            }

            // Enumerate files from the remote folder
            ArrayList<RemoteFileAttributes> files = this.sftConnector.ls(config.getSftpProperties(), config
                            .getRemoteFolder());

            String sqlString = "select      u " +
                               "from        upload u " +
                               "where       u.remoteFolder = :remoteFolder and " +
                               "            u.remoteFilename = :remoteFilename and " +
                               "            u.size = :fileSize " +
                               "order by    u.id desc";

            for (RemoteFileAttributes f : files) {
                // Check if a file with the same path, name and size has already been imported
                TypedQuery<UploadEntity> uploadQuery = entityManager.createQuery(sqlString, UploadEntity.class).setFirstResult(0).setMaxResults(1);

                uploadQuery.setParameter("remoteFolder", f.getRemoteFolder());
                uploadQuery.setParameter("remoteFilename", f.getFilename());
                uploadQuery.setParameter("fileSize", f.getSize());

                List<UploadEntity> uploads = uploadQuery.getResultList();

                UploadEntity existingUpload = null;
                if (uploads.size() != 0) {
                    existingUpload = uploads.get(0);
                }

                if ((existingUpload == null) ||
                    ((existingUpload.getSkippedRows() + existingUpload.getProccessedRows()) != existingUpload.getTotalRows())) {
                    // Filter file names based on a regular expression
                    if ((allowedFilenames != null) && (!allowedFilenames.matcher(f.getFilename()).matches())) {
                        continue;
                    }

                    // Create upload record
                    UploadEntity upload = new UploadEntity();

                    upload.setSource(f.getSource());
                    upload.setRemoteFolder(f.getRemoteFolder());
                    upload.setRemoteFilename(f.getFilename());

                    upload.setSize(f.getSize());
                    upload.setModifiedOn(f.getModifiedOn());

                    upload.setLocalFolder(config.getLocalFolder());
                    upload.setLocalFilename(UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(f.getFilename()));

                    String target = FilenameUtils.concat(config.getLocalFolder(), upload.getLocalFilename());

                    // Download file to the local folder
                    upload.setUploadStartedOn(new DateTime());
                    this.sftConnector.get(config.getSftpProperties(), config.getRemoteFolder(), f.getFilename(), target);

                    upload.setUploadCompletedOn(new DateTime());

                    // Process data and import records to HBASE
                    upload.setProcessingStartedOn(new DateTime());

                    FileProcessingStatus status = this.parse(target, config.getTimezone(), EnumUploadFileType.METER_DATA);

                    upload.setTotalRows(status.getTotalRows());
                    upload.setProccessedRows(status.getProcessedRows());
                    upload.setSkippedRows(status.getSkippedRows());
                    upload.setNegativeDifferenceRows(status.getNegativeDifference());

                    upload.setProcessingCompletedOn(new DateTime());

                    this.entityManager.persist(upload);
                    this.entityManager.flush();
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    /**
     * Loads smart water meter readings data from a file into HBASE.
     *
     * @param filename the file name.
     * @param timezone the time stamp time zone.
     * @param type of data being uploaded
     * @return statistics about the process execution.
     * @throws IOException in case an I/O exception occurs.
     *
     * @throws ApplicationException if the file or the time zone is not found.
     */
    @Override
    public FileProcessingStatus parse(String filename, String timezone, EnumUploadFileType type) throws ApplicationException, IOException {
        switch (type) {
            case METER_DATA:
                FileProcessingStatus status = parseMeterData(filename, timezone);

                renameFile(filename, timezone, status.getMinTimestamp(), status.getMaxTimestamp());

                return status;
            case METER_DATA_FORECAST:
                return parseMeterForecastData(filename, timezone);
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
     * @throws IOException in case an I/O exception occurs.
     */
    private void renameFile(String filename, String timezone, Long minTimestamp, Long maxTimestamp) throws IOException {
        if ((minTimestamp == 0) || (maxTimestamp == 0)) {
            return;
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.forID(timezone));

        String newFilename = String.format("%s_%s_%s",
                                           new DateTime(minTimestamp, DateTimeZone.UTC).toString(formatter),
                                           new DateTime(maxTimestamp, DateTimeZone.UTC).toString(formatter),
                                           FilenameUtils.getName(filename));

        newFilename = FilenameUtils.concat(FilenameUtils.getFullPath(filename), newFilename);

        FileUtils.moveFile(new File(filename), new File(newFilename));
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
        MeterDataRow row;
        String line = "";
        int lineIndex = 0;

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
            List<MeterDataRow> rows = new ArrayList<MeterDataRow>();

            // Count rows
            scan = new Scanner(new File(filename));

            while (scan.hasNextLine()) {
                lineIndex++;
                line = scan.nextLine();

                String[] tokens = StringUtils.split(line, ";");

                switch (tokens.length) {
                    case 3:
                        row = new MeterDataRow();
                        row.serial = tokens[0];

                        try {
                            row.timestamp = formatter.parseDateTime(tokens[1]).getMillis();
                        } catch (Exception ex) {
                            logger.error(String.format("Failed to parse timestamp [%s] in line [%d] from file [%s].",
                                                       tokens[1], lineIndex, filename), ex);
                            status.skipRow();
                            continue;
                        }

                        try {
                            row.volume = Float.parseFloat(tokens[2]);
                        } catch (Exception ex) {
                            logger.error(String.format("Failed to parse volume [%s] in line [%d] from file [%s].",
                                                       tokens[2], lineIndex, filename), ex);
                            status.skipRow();
                            continue;
                        }

                        rows.add(row);
                        break;
                    case 6:
                        row = new MeterDataRow();
                        row.serial = tokens[2];

                        try {
                            row.timestamp = formatter.parseDateTime(tokens[3]).getMillis();
                        } catch (Exception ex) {
                            logger.error(String.format("Failed to parse timestamp [%s] in line [%d] from file [%s].",
                                                       tokens[3], lineIndex, filename), ex);
                            status.skipRow();
                            continue;
                        }

                        try {
                            row.volume = Float.parseFloat(tokens[4]);
                        } catch (Exception ex) {
                            logger.error(String.format("Failed to parse volume [%s] in line [%d] from file [%s].",
                                                       tokens[4], lineIndex, filename), ex);
                            status.skipRow();
                            continue;
                        }

                        try {
                            row.difference = Float.parseFloat(tokens[5]);
                        } catch (Exception ex) {
                            logger.error(String.format("Failed to parse difference [%s] in line [%d] from file [%s].",
                                                       tokens[5], lineIndex, filename), ex);
                            status.skipRow();
                            continue;
                        }

                        rows.add(row);
                        break;
                    default:
                        // Row format is not supported
                        status.skipRow();
                }
            }

            scan.close();
            scan = null;

            status.setTotalRows(lineIndex);

            // Update and import row data
            importDataToHBase(status, rows);

        } catch (FileNotFoundException fileEx) {
            logger.error(String.format("File [%s] was not found.", filename), fileEx);
        } finally {
            if (scan != null) {
                scan.close();
            }
        }

        return status;
    }

    /**
     * Validates, processes and imports a list of meter readings to HBASE.
     *
     * @param status statistics about the process execution.
     * @param rows the data to import.
     */
    private void importDataToHBase(FileProcessingStatus status, List<MeterDataRow> rows) {
        if(rows.isEmpty()) {
            return;
        }

        // Sort data rows
        Collections.sort(rows, new Comparator<MeterDataRow>() {

            @Override
            public int compare(MeterDataRow r1, MeterDataRow r2) {
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
        status.setMinTimestamp(rows.get(0).timestamp);
        status.setMaxTimestamp(rows.get(rows.size()-1).timestamp);

        // Compute any missing values
        for (int i = 0, count = rows.size(); i < count; i++) {
            // Set difference for the first row for every unique serial number
            if ((i == 0) || (!rows.get(i).serial.equals(rows.get(i - 1).serial))) {
                if (rows.get(i).difference == null) {
                    WaterMeterStatusQueryResult meterStatus = this.waterMeterMeasurementRepository.getStatus(new String[] { rows.get(i).serial }, rows.get(i).timestamp - 1);

                    if ((meterStatus == null) || (meterStatus.getDevices().size() == 0)) {
                        rows.get(i).difference = 0f;
                    } else {
                        rows.get(i).difference = rows.get(i).volume - meterStatus.getDevices().get(0).getVolume();

                        if (rows.get(i).difference < 0) {
                            status.increaseNegativeDifference();
                        }
                    }
                }
            } else if ((rows.get(i).serial.equals(rows.get(i - 1).serial)) && (rows.get(i).difference == null)) {
                rows.get(i).difference = rows.get(i).volume - rows.get(i - 1).volume;

                if (rows.get(i).difference < 0) {
                    status.increaseNegativeDifference();
                }
            }

            // Validate difference
            if ((i != 0) && (rows.get(i).difference != null) && (rows.get(i).serial.equals(rows.get(i - 1).serial))) {
                float diff = rows.get(i).volume - rows.get(i - 1).volume;

                if (diff != rows.get(i).difference) {
                    rows.get(i).difference = diff;

                    if (diff < 0) {
                        status.increaseNegativeDifference();
                    }
                }
            }
        }

        // Import rows to HBASE
        for (MeterDataRow row : rows) {
            insert(status, row);
        }
    }

    /**
     * Imports a single of meter reading to HBASE.
     *
     * @param status statistics about the process execution.
     * @param row the data to import.
     */
    private void insert(FileProcessingStatus status, MeterDataRow row) {
        WaterMeterMeasurementCollection data = new WaterMeterMeasurementCollection();

        data.add(row.timestamp, row.volume, row.difference);

        this.waterMeterMeasurementRepository.store(row.serial, data);

        status.processRow();
    }

    private FileProcessingStatus parseMeterForecastData(String filename, String timezone)
                    throws ApplicationException {
        File file = new File(filename);
        if (!file.exists()) {
            throw createApplicationException(SharedErrorCode.RESOURCE_DOES_NOT_EXIST).set("resource", filename);
        }

        Scanner scan = null;

        FileProcessingStatus status = new FileProcessingStatus();

        // C11DE516148_2014-06-30-01 1.9244444
        String line = "";

        // Set time zone
        Set<String> zones = DateTimeZone.getAvailableIDs();
        if ((StringUtils.isBlank(timezone)) || (!zones.contains(timezone))) {
            throw createApplicationException(SharedErrorCode.TIMEZONE_NOT_FOUND).set("timezone", timezone);
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd-HH").withZone(DateTimeZone.forID(timezone));

        try {
            // Count rows
            scan = new Scanner(new File(filename));

            int index = 0;
            while (scan.hasNextLine()) {
                index++;
                scan.nextLine();
            }
            scan.close();
            scan = null;

            status.setTotalRows(index);

            // Process rows
            scan = new Scanner(new File(filename));

            index = 0;
            while (scan.hasNextLine()) {
                index++;
                line = scan.nextLine();

                float difference;

                String[] parts = StringUtils.split(line, "_");
                if (parts.length != 2) {
                    status.skipRow();
                    continue;
                }
                String[] values = StringUtils.split(parts[1], " ");
                if (values.length != 2) {
                    status.skipRow();
                    continue;
                }

                String serial = parts[0];

                DateTime timestamp;
                try {
                    timestamp = formatter.parseDateTime(values[0]);
                } catch (Exception ex) {
                    logger.error(String.format("Failed to parse timestamp [%s] in line [%d] from file [%s].",
                                    values[0], index, filename), ex);
                    status.skipRow();
                    continue;
                }

                try {
                    difference = Float.parseFloat(values[1]);
                } catch (Exception ex) {
                    logger.error(String.format("Failed to parse difference [%s] in line [%d] from file [%s].",
                                    values[1], index, filename), ex);
                    status.skipRow();
                    continue;
                }

                WaterMeterForecastCollection data = new WaterMeterForecastCollection();
                ArrayList<WaterMeterForecast> measurements = new ArrayList<WaterMeterForecast>();
                WaterMeterForecast measurement = new WaterMeterForecast();

                measurement.setTimestamp(timestamp.getMillis());
                measurement.setDifference(difference);

                measurements.add(measurement);

                data.setMeasurements(measurements);

                this.waterMeterForecastRepository.store(serial, data);

                status.processRow();
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

    private static class MeterDataRow {

        public String serial;

        public long timestamp;

        public float volume;

        public Float difference;
    }
}
