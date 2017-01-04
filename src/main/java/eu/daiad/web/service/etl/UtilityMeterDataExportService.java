package eu.daiad.web.service.etl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.web.hbase.HBaseConnectionManager;
import eu.daiad.web.model.TemporalConstants;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.meter.WaterMeterDataPoint;
import eu.daiad.web.model.meter.WaterMeterDataSeries;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IWaterMeterMeasurementRepository;
import eu.daiad.web.service.etl.UtilityDataExportQuery.EnumExportMode;

/**
 * Service that provides methods for exporting smart water meter data for a
 * utility.
 */
@Service
public class UtilityMeterDataExportService extends AbstractUtilityDataExportService {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(UtilityMeterDataExportService.class);

    /**
     * HBASE table that indexes meters by serial number and time stamp.
     */
    private final String meterTableMeasurementByMeter = "daiad:meter-measurements-by-user";

    /**
     * Default column family name used by all HBASE tables.
     */
    private final String columnFamilyName = "cf";

    /**
     * HBASE connection managed by the Spring framework.
     */
    @Autowired
    private HBaseConnectionManager connection;

    /**
     * Repository for accessing smart water meter readings.
     */
    @Autowired
    private IWaterMeterMeasurementRepository waterMeterMeasurementRepository;

    /**
     * Exports data for a single utility to a file. Any exported data file is replaced.
     *
     * @param query the query that selects the data to export.
     * @return the result of the export operation.
     * @throws ApplicationException if the query execution or file creation fails.
     */
    public ExportResult export(UtilityDataExportQuery query) throws ApplicationException {
        try {
            ExportResult exportResult = new ExportResult();

            long totalUsers = 0;
            long totalRows = 0;

            // Initialize directories
            if(StringUtils.isBlank(query.getWorkingDirectory())) {
               query.setWorkingDirectory(workingDirectory);
            }
            ensureDirectory(query.getWorkingDirectory());
            ensureDirectory(query.getTargetDirectory());

            // Set default time zone for the utility if not values is specified
            if(StringUtils.isBlank(query.getTimezone())) {
                query.setTimezone(query.getUtility().getTimezone());
            }

            // Set time zone
            ensureTimezone(query.getTimezone());

            // Set default file name
            if (StringUtils.isBlank(query.getFilename())) {
                query.setFilename(query.getUtility().getName());
            }

            // Create new file names
            String dataFilename = createTemporaryFilename(query.getWorkingDirectory());

            switch (query.getMode()) {
                case METER_UTILITY:
                    totalRows = exportAllMeterData(dataFilename,
                                                   query.getTimezone(),
                                                   query.getStartTimstamp(),
                                                   query.getEndTimestamp(),
                                                   query.getDateFormat());
                    break;
                default:
                    // User rows
                    List<List<String>> userRows = new ArrayList<List<String>>();

                    // Export data for every trial user
                    List<UUID> userKeys = userRepository.getUserKeysForUtility(query.getUtility().getKey());

                    for (UUID userKey : userKeys) {
                        long totalUserRows = 0;

                        // Get user
                        AuthenticatedUser user = userRepository.getUserByKey(userKey);

                        // Get meter
                        DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery(EnumDeviceType.METER);

                        for (Device device : deviceRepository.getUserDevices(userKey, deviceQuery)) {
                            // Get data
                            WaterMeterDevice meterDevice = (WaterMeterDevice) device;

                            WaterMeterMeasurementQuery meterQuery = new WaterMeterMeasurementQuery();
                            meterQuery.setDeviceKey(new UUID[] { meterDevice.getKey() });
                            meterQuery.setUserKey(userKey);
                            meterQuery.setGranularity(TemporalConstants.NONE);

                            WaterMeterMeasurementQueryResult result = waterMeterMeasurementRepository.searchMeasurements(
                                new String[] { meterDevice.getSerial() },
                                DateTimeZone.forID(query.getTimezone()),
                                meterQuery);

                            // Export data
                            for (WaterMeterDataSeries series : result.getSeries()) {
                                totalUserRows = exportUtilityUserMeterData(dataFilename, query.getTimezone(), series);
                            }

                            // Export user only if at least one measurement is found
                            if (totalUserRows > 0) {
                                List<String> row = new ArrayList<String>();

                                row.add(user.getKey().toString());
                                row.add(user.getUsername());
                                row.add(meterDevice.getSerial());

                                userRows.add(row);

                                totalUsers++;
                                totalRows += totalUserRows;
                            }
                        }
                    }

                    // Export user and phases only if all trial users have been requested
                    if ((query.getMode() == EnumExportMode.ALL_TRIAL) && (totalUsers > 0)) {
                        String userFilename = createTemporaryFilename(query.getWorkingDirectory());

                        CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

                        CSVPrinter userPrinter = new CSVPrinter(
                                        new BufferedWriter(
                                            new OutputStreamWriter(
                                                new FileOutputStream(userFilename, true),
                                                Charset.forName("UTF-8").newEncoder())), format);

                        List<String> row = new ArrayList<String>();

                        row.add("user key");
                        row.add("user name");
                        row.add("meter id");

                        userPrinter.printRecord(row);

                        for (List<String> r : userRows) {
                            userPrinter.printRecord(r);
                        }

                        userPrinter.flush();
                        userPrinter.close();

                        exportResult.getFiles().add(new FileLabelPair(new File(userFilename), "user.csv", totalUsers));

                        exportPhaseTimestamps(query, exportResult);
                    }
                    break;
            }

            exportResult.getFiles().add(new FileLabelPair( new File(dataFilename), "data.csv", totalRows));

            exportResult.increment(totalRows + totalUsers);

            return exportResult;
        } catch (Exception ex) {
            throw wrapApplicationException(ex);
        }
    }

    /**
     * Exports data the smart water meter of a single user of a utility.
     *
     * @param filename the name of the file where the exported data is saved.
     * @param timezone the time zone for formatting the dates.
     * @param series the exported data.
     * @return the number of rows written.
     * @throws IOException in case an I/O exception occurs.
     */
    private long exportUtilityUserMeterData(String filename, String timezone, WaterMeterDataSeries series) throws IOException {
        long counter = 0;

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").withZone(DateTimeZone.forID(timezone));

        CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

        CSVPrinter printer = new CSVPrinter(
                                new BufferedWriter(
                                    new OutputStreamWriter(
                                        new FileOutputStream(filename, true),
                                        Charset.forName("UTF-8").newEncoder())), format);

        series.sort();

        for (WaterMeterDataPoint point : series.getValues()) {
            ArrayList<String> row = new ArrayList<String>();

            row.add(series.getSerial());
            row.add(point.getUtcDate().toString(formatter));
            row.add(Float.toString(point.getVolume()));
            row.add(Float.toString(point.getDifference()));

            printer.printRecord(row);

            counter++;
        }

        printer.flush();
        printer.close();

        return counter;
    }


    /**
     * Exports data the smart water meter of a single user of a utility.
     *
     * @param filename the name of the file where the exported data is saved.
     * @param timezone the time zone for formatting the dates.
     * @param startDateTime the time interval date time.
     * @param endDateTime the time interval date time.
     * @param dateFormat date format pattern.
     * @return the number of rows written.
     * @throws IOException in case an I/O exception occurs.
     */
    private long exportAllMeterData(String filename, String timezone, Long startTimestamp, Long endTimestamp, String dateFormat) throws IOException {
        long counter = 0;

        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withZone(DateTimeZone.forID(timezone));

        CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

        CSVPrinter printer = new CSVPrinter(
                                new BufferedWriter(
                                    new OutputStreamWriter(
                                        new FileOutputStream(filename, true),
                                        Charset.forName("UTF-8").newEncoder())), format);


        // Execute full table scan on HBASE table
        Table table = null;
        ResultScanner scanner = null;

        if (startTimestamp == null) {
            startTimestamp = new DateTime(0L, DateTimeZone.UTC).getMillis();
        }
        if (endTimestamp == null) {
            endTimestamp = new DateTime(DateTimeZone.UTC).getMillis();
        }

        try {
            table = connection.getTable(meterTableMeasurementByMeter);
            byte[] columnFamily = Bytes.toBytes(columnFamilyName);

            Scan scan = new Scan();
            scan.addFamily(columnFamily);

            scanner = table.getScanner(scan);

            for (Result r = scanner.next(); r != null; r = scanner.next()) {
                NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 16, 24));

                Float volume = null, difference = null;
                long timestamp = 0;
                String serial = "";

                for (Entry<byte[], byte[]> entry : map.entrySet()) {
                    short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));
                    timestamp = ((Long.MAX_VALUE / 1000L) - (timeBucket + (long) offset)) * 1000L;

                    if ((startTimestamp <= timestamp) && (timestamp <= endTimestamp)) {
                        int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
                        byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);

                        String columnQualifier = Bytes.toString(slice);
                        if (columnQualifier.equals("v")) {
                            volume = Bytes.toFloat(entry.getValue());
                        }
                        if (columnQualifier.equals("d")) {
                            difference = Bytes.toFloat(entry.getValue());
                        }
                        if (columnQualifier.equals("s")) {
                            serial = new String(entry.getValue(), StandardCharsets.UTF_8);
                        }
                    }

                    if ((volume != null) && (difference != null) && (!StringUtils.isBlank(serial))) {
                        printer.printRecord(createMeterRow(
                                        serial,
                                        new DateTime(timestamp).toString(formatter),
                                        volume,
                                        difference
                                    ));

                        volume = null;
                        difference = null;
                        counter++;
                        if(counter % 1000 == 0) {
                            printer.flush();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        } finally {
            try {
                if (scanner != null) {
                    scanner.close();
                    scanner = null;
                }
                if (table != null) {
                    table.close();
                    table = null;
                }
            } catch (Exception ex) {
                logger.error("Failed to release resources.", ex);
            }
        }

        printer.flush();
        printer.close();

        return counter;
    }

    /**
     * Creates a list of String arguments.
     *
     * @param serial the meter serial.
     * @param date the date formatted at a specific time zone.
     * @param volume the current volume.
     * @param difference the difference from the most recent reading.
     * @return a list of String arguments.
     */
    private List<String> createMeterRow(String serial, String date, float volume, float difference) {
        List<String> row = new ArrayList<String>();

        row.add(serial);
        row.add(date);
        row.add(Float.toString(volume));
        row.add(Float.toString(difference));

        return row;
    }
}
