package eu.daiad.utility.service.etl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.common.hbase.EnumHBaseTable;
import eu.daiad.common.hbase.HBaseConnectionManager;
import eu.daiad.common.model.TemporalConstants;
import eu.daiad.common.model.device.Device;
import eu.daiad.common.model.device.DeviceRegistrationQuery;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.device.WaterMeterDevice;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.meter.WaterMeterDataPoint;
import eu.daiad.common.model.meter.WaterMeterDataSeries;
import eu.daiad.common.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.common.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.repository.application.IMeterDataRepository;
import eu.daiad.utility.service.etl.UtilityDataExportQuery.EnumExportMode;

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
    private IMeterDataRepository waterMeterMeasurementRepository;

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
                    // Export daily data
                    String dailyDataFilename = createTemporaryFilename(query.getWorkingDirectory());

                    totalRows = exportAllMeterDailyData(dailyDataFilename,
                                                        query.getTimezone(),
                                                        query.getStartTimestamp(),
                                                        query.getEndTimestamp(),
                                                        query.getDateFormat());

                    exportResult.getFiles().add(new FileLabelPair( new File(dailyDataFilename), "daily-data.csv", totalRows));

                    // Export all data
                    totalRows += exportAllMeterData(dataFilename,
                                                    query.getTimezone(),
                                                    query.getStartTimestamp(),
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

                            if ((!query.getSerials().isEmpty()) && (!query.getSerials().contains(meterDevice.getSerial()))) {
                                continue;
                            }

                            WaterMeterMeasurementQuery meterQuery = new WaterMeterMeasurementQuery();
                            meterQuery.setDeviceKey(new UUID[] { meterDevice.getKey() });
                            meterQuery.setUserKey(userKey);
                            meterQuery.setGranularity(TemporalConstants.NONE);
                            if (query.getStartTimestamp() != null) {
                                meterQuery.setStartDate(query.getStartTimestamp());
                            }
                            if (query.getEndTimestamp() != null) {
                                meterQuery.setEndDate(query.getEndTimestamp());
                            }

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
     * Exports data for all smart water meters of a utility.
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
            table = connection.getTable(EnumHBaseTable.SWM_USER.getValue());
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
     * Exports daily data for all smart water meters of a utility.
     *
     * @param filename the name of the file where the exported data is saved.
     * @param timezone the time zone for formatting the dates.
     * @param startDateTime the time interval date time.
     * @param endDateTime the time interval date time.
     * @param dateFormat date format pattern.
     * @return the number of rows written.
     * @throws IOException in case an I/O exception occurs.
     */
    private long exportAllMeterDailyData(String filename, String timezone, Long startTimestamp, Long endTimestamp, String dateFormat) throws IOException {
        long counter = 0;

        DateTimeZone dtz = DateTimeZone.forID(timezone);

        CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

        CSVPrinter printer = new CSVPrinter(
                                new BufferedWriter(
                                    new OutputStreamWriter(
                                        new FileOutputStream(filename, true),
                                        Charset.forName("UTF-8").newEncoder())), format);
        MeterRowCollection.printHeader(printer);

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
            table = connection.getTable(EnumHBaseTable.SWM_USER.getValue());
            byte[] columnFamily = Bytes.toBytes(columnFamilyName);

            Scan scan = new Scan();
            scan.addFamily(columnFamily);

            scanner = table.getScanner(scan);

            MeterRowCollection rows = null;
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
                        if(rows == null) {
                            rows = new MeterRowCollection(serial, dtz);
                            rows.serial = serial;
                        } else if (!rows.serial.equals(serial)) {
                            // Export rows for the current serial
                            rows.printSerial(printer, dateFormat);

                            rows = new MeterRowCollection(serial, dtz);
                            rows.serial = serial;
                        }
                        rows.add(new DateTime(timestamp, dtz), volume);

                        volume = null;
                        difference = null;
                        counter++;
                    }
                }
            }
            // Print last serial
            if (rows != null) {
                rows.printSerial(printer, dateFormat);
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

    private static class Interval {

        public DateTime start;

        public DateTime end;

        public MeterRow beforeStart;

        public MeterRow afterStart;

        public MeterRow beforeEnd;

        public MeterRow afterEnd;

        public short hours = 0;

        public boolean isValid() {
            return ((beforeStart != null) && (afterStart != null) && (beforeEnd != null) && (afterEnd != null));
        }

        public Interval next() {
            Interval interval = new Interval();
            interval.start = end;
            interval.end = end.plusDays(1);

            interval.beforeStart = beforeEnd;
            interval.afterStart = afterEnd;

            if(isValid()) {
                interval.hours = 1;
            }

            return interval;
        }

        public Float getVolume() {
            if(!isValid()) {
                return null;
            }

            return afterEnd.getVolume() - afterStart.getVolume();
        }
    }

    private static class MeterRowCollection {

        private String serial;

        private DateTimeZone timezone;

        private List<MeterRow> items = new ArrayList<MeterRow>();

        public MeterRowCollection(String serial, DateTimeZone timezone) {
            this.serial = serial;
            this.timezone = timezone;
        }

        public void add(DateTime date, float volume) {
            items.add(new MeterRow(date, volume));
        }

        public static void printHeader(CSVPrinter printer) throws IOException {
            List<String> row = new ArrayList<String>();

            row.add("Serial");
            row.add("Date");
            row.add("First record");
            row.add("Last record");
            row.add("# of records in day");
            row.add("Volume");

            printer.printRecord(row);
        }

        public void printSerial(CSVPrinter printer, String dateFormat) throws IOException {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withZone(timezone);

            List<Interval> intervals = extractIntervals(3, 3);
            if(!intervals.isEmpty()) {
                for(Interval interval : intervals) {
                    List<String> row = new ArrayList<String>();

                    row.add(serial);
                    row.add(interval.start.toString(formatter));
                    row.add(interval.afterStart.date.toString(formatter));
                    row.add(interval.afterEnd.date.toString(formatter));
                    row.add(Short.toString(interval.hours));
                    row.add(Float.toString(interval.getVolume()));

                    printer.printRecord(row);
                }
            }
        }

        private List<Interval> extractIntervals(int hour, int offset) {
            List<Interval> intervals = new ArrayList<Interval>();

            if (items.isEmpty()) {
                return intervals;
            }

            Collections.sort(items, new Comparator<MeterRow>() {
                @Override
                public int compare(MeterRow o1, MeterRow o2) {
                    if (o1.getDate().getMillis() <= o2.getDate().getMillis()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            // Initialize first interval
            Interval current = new Interval();
            current.start = items.get(0).getDate().hourOfDay().setCopy(hour);
            current.start = current.start.minuteOfHour().setCopy(0);
            current.start = current.start.secondOfMinute().setCopy(0);
            current.end = current.start.plusDays(1);

            int index = 0, count = items.size();
            while(index < count) {
                MeterRow row = items.get(index);

                if (row.isBefore(current.start)) {
                    current.beforeStart = row;
                } else if (row.isAfter(current.start)) {
                    if(current.afterStart == null) {
                        current.afterStart = row;
                    }
                } else {
                    current.beforeStart = current.afterStart = row;
                }

                // Check that the right interval instant is not set before the left interval instant
                if ((current.beforeStart == null) && (current.afterStart != null)) {
                    current = current.next();
                    continue;
                }

                if (row.isBefore(current.end)) {
                    current.beforeEnd = row;
                } else if (row.isAfter(current.end)) {
                    if(current.afterEnd == null) {
                        current.afterEnd = row;
                    }
                } else {
                    current.beforeEnd = current.afterEnd = row;
                }

                // Check that the right interval instant is not set before the left interval instant
                if ((current.beforeEnd == null) && (current.afterEnd != null)) {
                    current = current.next();
                    continue;
                }

                // Only count hours in the interval
                if ((current.afterStart != null) && (current.afterEnd == null)) {
                    current.hours++;
                }
                if(current.isValid()) {
                    // Enforce offset
                    Period p1 = new Period(current.beforeStart.getDate(), current.start);
                    Period p2 = new Period(current.start, current.afterStart.getDate());
                    Period p3 = new Period(current.beforeEnd.getDate(), current.end);
                    Period p4 = new Period(current.end, current.afterEnd.getDate());

                    // Ignore intervals without accurate start and end instants.
                    if ((p1.getHours() < offset) &&
                        (p2.getHours() < offset) &&
                        (p3.getHours() < offset) &&
                        (p4.getHours() < offset)) {
                        intervals.add(current);
                    }

                    current = current.next();
                }

                index++;
            }

            return intervals;
        }
    }

    private static class MeterRow {

        private DateTime date;

        private float volume;

        public MeterRow(DateTime date, float volume) {
            this.date = date;
            this.volume = volume;
        }

        public boolean isAfter(DateTime instant) {
            return date.isAfter(instant);
        }

        public boolean isBefore(DateTime instant) {
            return date.isBefore(instant);
        }

        public DateTime getDate() {
            return date;
        }

        public float getVolume() {
            return volume;
        }

    }
}
