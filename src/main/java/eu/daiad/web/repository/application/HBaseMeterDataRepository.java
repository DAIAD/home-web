package eu.daiad.web.repository.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Repository;

import eu.daiad.web.hbase.EnumHBaseColumnFamily;
import eu.daiad.web.hbase.EnumHBaseTable;
import eu.daiad.web.model.TemporalConstants;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DataErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.meter.MeterDataStoreStats;
import eu.daiad.web.model.meter.WaterMeterDataPoint;
import eu.daiad.web.model.meter.WaterMeterDataSeries;
import eu.daiad.web.model.meter.WaterMeterMeasurement;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatus;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.ExpandedPopulationFilter;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MeterUserDataPoint;
import eu.daiad.web.model.query.RankingDataPoint;
import eu.daiad.web.model.query.UserDataPoint;

@Repository()
public class HBaseMeterDataRepository extends AbstractHBaseMeterDataRepository implements IMeterDataRepository {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(HBaseMeterDataRepository.class);

    public HBaseMeterDataRepository() {
        interval = EnumTimeInterval.HOUR;
    }

    /**
     * Stores a collection of smart water meter readings to HBase.
     *
     * @param serial the smart water meter unique serial number.
     * @param data a collection of {@link WaterMeterMeasurement}.
     * @return statistics for the insert operations.
     */
    @Override
    public MeterDataStoreStats store(String serial, WaterMeterMeasurementCollection data) {
        MeterDataStoreStats stats = new MeterDataStoreStats();

        try {
            if ((data == null) || (data.getMeasurements() == null) || (data.getMeasurements().size() == 0)) {
                return stats;
            }

            // Sort measurements
            Collections.sort(data.getMeasurements(), new Comparator<WaterMeterMeasurement>() {
                @Override
                public int compare(WaterMeterMeasurement o1, WaterMeterMeasurement o2) {
                    if (o1.getTimestamp() <= o2.getTimestamp()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            // Always sync difference for the first and last measurements from HBase
            WaterMeterMeasurement first = data.getMeasurements().get(0);
            WaterMeterStatusQueryResult statusBefore = getStatusBefore(new String[] { serial }, first.getTimestamp() - 1);

            if ((statusBefore != null) && (!statusBefore.getDevices().isEmpty())) {
                float diff = first.getVolume() - statusBefore.getDevices().get(0).getVolume();
                if (diff != first.getDifference()) {
                    first.setDifference(diff);
                    stats.update();
                }
            }

            WaterMeterMeasurement last = data.getMeasurements().get(data.getMeasurements().size() - 1);
            WaterMeterStatusQueryResult statusAfter = getStatusAfter(new String[] { serial }, last.getTimestamp() + 1);

            if ((statusAfter != null) && (!statusAfter.getDevices().isEmpty())) {
                // Re-insert the next data point
                WaterMeterMeasurement after = new WaterMeterMeasurement();
                after.setVolume(statusAfter.getDevices().get(0).getVolume());
                after.setDifference(statusAfter.getDevices().get(0).getVolume()  - last.getVolume());
                after.setTimestamp(statusAfter.getDevices().get(0).getTimestamp());

                data.getMeasurements().add(after);
                stats.create();
            }

            // Update all intermediate readings
            for (int i = 1, count = data.getMeasurements().size(); i < count; i++) {
                float diff = data.getMeasurements().get(i).getVolume() - data.getMeasurements().get(i - 1).getVolume();
                if (diff != data.getMeasurements().get(i).getDifference()) {
                    data.getMeasurements().get(i).setDifference(diff);
                    stats.update();
                }
            }

            storeDataByMeter(serial, data);
            storeDataByTime(serial, data);
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return stats;
    }

    /**
     * Stores smart water meter data indexed by serial number.
     *
     * @param serial the smart water meter data unique serial number.
     * @param data a collection of {@link WaterMeterMeasurement}.
     */
    private void storeDataByMeter(String serial, WaterMeterMeasurementCollection data) {
        Table table = null;
        try {
            table = connection.getTable(EnumHBaseTable.SWM_USER.getValue());

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            byte[] meterSerial = serial.getBytes("UTF-8");
            byte[] meterSerialHash = md.digest(meterSerial);

            for (int i = 0; i < data.getMeasurements().size(); i++) {
                WaterMeterMeasurement m = data.getMeasurements().get(i);

                if (m.getVolume() < 0) {
                    continue;
                }

                RowKeyQualifierPrefix key = createMeterRowKeyQualifierPrefix(meterSerialHash, m.getTimestamp());

                Put p = new Put(key.rowKey);

                byte[] column = concatenate(key.qualifierPrefix, appendLength(Bytes.toBytes("v")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

                column = concatenate(key.qualifierPrefix, appendLength(Bytes.toBytes("d")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getDifference()));

                column = concatenate(key.qualifierPrefix, appendLength(Bytes.toBytes("s")));
                p.addColumn(columnFamily, column, serial.getBytes(StandardCharsets.UTF_8));

                table.put(p);
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        } finally {
            try {
                if (table != null) {
                    table.close();
                    table = null;
                }
            } catch (Exception ex) {
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Stores smart water meter data partitioned by time.
     *
     * @param serial the smart water meter data unique serial number.
     * @param data a collection of {@link WaterMeterMeasurement}.
     */
    private void storeDataByTime(String serial, WaterMeterMeasurementCollection data) {
        Table table = null;

        try {
            table = connection.getTable(EnumHBaseTable.SWM_TIME.getValue());

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            byte[] meterSerial = serial.getBytes("UTF-8");
            byte[] meterSerialHash = md.digest(meterSerial);

            for (int i = 0; i < data.getMeasurements().size(); i++) {
                WaterMeterMeasurement m = data.getMeasurements().get(i);

                if (m.getVolume() < 0) {
                    continue;
                }

                RowKeyQualifierPrefix key = createPartitionedRowKeyQualifierPrefix(meterSerialHash, m.getTimestamp());

                Put p = new Put(key.rowKey);

                byte[] column = concatenate(key.qualifierPrefix, appendLength(Bytes.toBytes("v")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

                column = concatenate(key.qualifierPrefix, appendLength(Bytes.toBytes("d")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getDifference()));

                column = concatenate(key.qualifierPrefix, appendLength(Bytes.toBytes("s")));
                p.addColumn(columnFamily, column, serial.getBytes(StandardCharsets.UTF_8));

                table.put(p);
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        } finally {
            try {
                if (table != null) {
                    table.close();
                    table = null;
                }
            } catch (Exception ex) {
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Returns the current status for a set of smart water meters.
     *
     * @param serials the unique smart water meter serial numbers to search.
     * @return a collection of {@link WaterMeterStatus}.
     */
    @Override
    public WaterMeterStatusQueryResult getStatus(String serials[]) {
        return getStatusBefore(serials, new DateTime(DateTimeZone.UTC).getMillis());
    }

    /**
     * Returns the most recent status for a set of smart water meters before the specified timestamp.
     *
     * @param serials the unique smart water meter serial numbers to search.
     * @param maxDateTime time interval upper limit.
     * @return a collection of {@link WaterMeterStatus}.
     */
    @Override
    public WaterMeterStatusQueryResult getStatusBefore(String serials[], long maxDateTime) {
        return this.getStatus(serials, maxDateTime, true);
    }

    /**
     * Returns the most recent status for a set of smart water meters after the specified timestamp.
     *
     * @param serials the unique smart water meter serial numbers to search.
     * @param minDateTime time interval upper limit.
     * @return a collection of {@link WaterMeterStatus}.
     */
    @Override
    public WaterMeterStatusQueryResult getStatusAfter(String serials[], long minDateTime) {
        return getStatus(serials, minDateTime, false);
    }

    /**
     * Returns the most recent status for a set of smart water meters
     * before/after the specified timestamp.
     *
     * @param serials the unique smart water meter serial numbers to search.
     * @param timeThreshold time interval upper limit.
     * @param descending true if true, the most recent status before the timestamp is returned; Otherwise, the most recent one after is returned.
     * @return a collection of {@link WaterMeterStatus}.
     */
    private  WaterMeterStatusQueryResult getStatus(String serials[], long timeThreshold, boolean descending) {
        WaterMeterStatusQueryResult data = new WaterMeterStatusQueryResult();

        Table table = null;
        ResultScanner scanner = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(EnumHBaseTable.SWM_USER.getValue());
            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            for (int deviceIndex = 0; deviceIndex < serials.length; deviceIndex++) {
                byte[] meterSerial = serials[deviceIndex].getBytes("UTF-8");
                byte[] meterSerialHash = md.digest(meterSerial);

                Scan scan = new Scan();
                scan.addFamily(columnFamily);
                scan.setStartRow(createMeterRowKey(meterSerialHash, timeThreshold));

                if(descending) {
                    scan.setStopRow(calculateTheClosestNextRowKeyForPrefix(meterSerialHash));
                } else {
                    scan.setReversed(true);
                }
                scan.setCaching(2);
                scanner = table.getScanner(scan);

                int valueCount = 0;
                boolean stopScanner = false;

                WaterMeterStatus status = new WaterMeterStatus(serials[deviceIndex]);
                WaterMeterDataPoint value1 = new WaterMeterDataPoint();
                WaterMeterDataPoint value2 = new WaterMeterDataPoint();

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    byte[] currentSerialHash = Arrays.copyOfRange(r.getRow(), 0, 16);
                    if (!Arrays.equals(currentSerialHash, meterSerialHash)) {
                        break;
                    }

                    long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 16, 24));

                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);
                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));
                        long timestamp = ((Long.MAX_VALUE / 1000) - (timeBucket + (long) offset)) * 1000L;

                        if(descending) {
                            if (timestamp <= timeThreshold) {
                                int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
                                byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);
                                String columnQualifier = Bytes.toString(slice);

                                if (columnQualifier.equals("v")) {
                                    valueCount++;
                                    if (value2.getTimestamp() < timestamp) {
                                        value1.setTimestamp(value2.getTimestamp());
                                        value1.setVolume(value2.getVolume());

                                        value2.setTimestamp(timestamp);
                                        value2.setVolume(Bytes.toFloat(entry.getValue()));
                                    } else if (value1.getTimestamp() < timestamp) {
                                        value1.setTimestamp(timestamp);
                                        value1.setVolume(Bytes.toFloat(entry.getValue()));
                                    }
                                }
                            }
                            if (valueCount == 2) {
                                stopScanner = true;
                                break;
                            }
                        } else {
                            if (timestamp >= timeThreshold) {
                                int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
                                byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);
                                String columnQualifier = Bytes.toString(slice);

                                if (columnQualifier.equals("v")) {
                                    valueCount++;
                                    stopScanner = true;
                                    value2.setTimestamp(timestamp);
                                    value2.setVolume(Bytes.toFloat(entry.getValue()));
                                }
                            }
                        }
                    }
                    if (stopScanner) {
                        break;
                    }
                }
                switch (valueCount) {
                    case 0:
                        // No value found
                        break;
                    case 1:
                        status.setTimestamp(value2.getTimestamp());
                        status.setVolume(value2.getVolume());
                        status.setVariation(0);

                        data.getDevices().add(status);
                    default:
                        status.setTimestamp(value2.getTimestamp());
                        status.setVolume(value2.getVolume());
                        status.setVariation(value2.getVolume() - value1.getVolume());

                        data.getDevices().add(status);
                }
            }

            return data;
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Searches for smart water meter readings.
     *
     * @param serials the unique smart water meter serial numbers to search.
     * @param timezone the time zone of the results.
     * @param query the query for filtering the results.
     * @return a collection of {@link WaterMeterDataSeries}.
     */
    @Override
    public WaterMeterMeasurementQueryResult searchMeasurements(String serials[], DateTimeZone timezone, WaterMeterMeasurementQuery query) {
        Table table = null;
        ResultScanner scanner = null;

        DateTime startDate = null;
        if (query.getStartDate() != null) {
            startDate = new DateTime(query.getStartDate(), DateTimeZone.UTC);
        } else {
            startDate = new DateTime(0L, DateTimeZone.UTC);
        }
        DateTime endDate = null;
        if (query.getEndDate() != null) {
            endDate = new DateTime(query.getEndDate(), DateTimeZone.UTC);
        } else {
            endDate = new DateTime(DateTimeZone.UTC);
        }

        switch (query.getGranularity()) {
            case TemporalConstants.NONE:
                // Retrieve values at the highest granularity, that is at the
                // measurement level
                break;
            case TemporalConstants.HOUR:
                startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(),
                                startDate.getHourOfDay(), 0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), endDate
                                .getHourOfDay(), 59, 59, DateTimeZone.UTC);
                break;
            case TemporalConstants.DAY:
                startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), startDate.getDayOfMonth(), 0,
                                0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23, 59,
                                59, DateTimeZone.UTC);
                break;
            case TemporalConstants.WEEK:
                DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
                DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
                startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0, 0,
                                DateTimeZone.UTC);
                endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59, 59,
                                DateTimeZone.UTC);
                break;
            case TemporalConstants.MONTH:
                startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth()
                                .getMaximumValue(), 23, 59, 59, DateTimeZone.UTC);
                break;
            case TemporalConstants.YEAR:
                startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC);
                endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59, DateTimeZone.UTC);
                break;
            default:
                throw createApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED).set("level",
                                query.getGranularity());
        }

        DateTime queryEndDate = endDate;

        DateTime maxDate = new DateTime(DateTimeZone.UTC);
        if (maxDate.getMillis() < endDate.getMillis()) {
            endDate = maxDate;
        }

        WaterMeterMeasurementQueryResult data = new WaterMeterMeasurementQueryResult();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(EnumHBaseTable.SWM_USER.getValue());
            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            for (int deviceIndex = 0; deviceIndex < serials.length; deviceIndex++) {
                byte[] meterSerial = serials[deviceIndex].getBytes("UTF-8");
                byte[] meterSerialHash = md.digest(meterSerial);

                Scan scan = new Scan();
                scan.addFamily(columnFamily);

                scan.setStartRow(createMeterRowKey(meterSerialHash, endDate.getMillis()));

                scan.setStopRow(calculateTheClosestNextRowKeyForPrefix(createMeterRowKey(meterSerialHash, startDate.getMillis())));

                scanner = table.getScanner(scan);

                WaterMeterDataSeries series = new WaterMeterDataSeries(
                    query.getDeviceKey()[deviceIndex],
                    serials[deviceIndex],
                    startDate.getMillis(),
                    queryEndDate.getMillis(),
                    query.getGranularity());

                data.getSeries().add(series);

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 16, 24));

                    Float volume = null, difference = null;

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));
                        long timestamp = ((Long.MAX_VALUE / 1000L) - (timeBucket + (long) offset)) * 1000L;

                        if ((startDate.getMillis() <= timestamp) && (timestamp <= endDate.getMillis())) {
                            int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
                            byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);

                            String columnQualifier = Bytes.toString(slice);
                            if (columnQualifier.equals("v")) {
                                volume = Bytes.toFloat(entry.getValue());
                            }
                            if (columnQualifier.equals("d")) {
                                difference = Bytes.toFloat(entry.getValue());
                            }

                            if ((volume != null) && (difference != null)) {
                                series.add(timestamp, volume, difference, timezone);

                                volume = null;
                                difference = null;
                            }
                        }
                    }
                }

                series.sort();
            }

            return data;
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }
    }

    /**
     * Executes a query for smart water meter data.
     *
     * @param query the query for filtering data.
     * @return a collection of {@link GroupDataSeries}.
     * @throws ApplicationException if an error occurs or query validation fails.
     */
    @Override
    public ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException {
        Table table = null;
        ResultScanner scanner = null;

        ArrayList<GroupDataSeries> result = new ArrayList<GroupDataSeries>();
        for (ExpandedPopulationFilter filter : query.getGroups()) {
            result.add(new GroupDataSeries(filter.getLabel(), filter.getSize(), filter.getAreaId()));
        }
        try {
            table = connection.getTable(EnumHBaseTable.SWM_TIME.getValue());
            byte[] columnFamily = Bytes.toBytes(EnumHBaseColumnFamily.DEFAULT.getValue());

            DateTime startDate = new DateTime(query.getStartDateTime(), query.getTimezone());
            DateTime endDate = new DateTime(query.getEndDateTime(), query.getTimezone());

            switch (query.getGranularity()) {
                case HOUR:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
                                    startDate.getDayOfMonth(), startDate.getHourOfDay(), 0, 0, query.getTimezone());
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(),
                                    endDate.getHourOfDay(), 59, 59, query.getTimezone());
                    break;
                case DAY:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
                                    startDate.getDayOfMonth(), 0, 0, 0, query.getTimezone());
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23,
                                    59, 59, query.getTimezone());
                    break;
                case WEEK:
                    DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
                    DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
                    startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0,
                                    0, query.getTimezone());
                    endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59,
                                    59, query.getTimezone());
                    break;
                case MONTH:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0,
                                    query.getTimezone());
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth()
                                    .getMaximumValue(), 23, 59, 59, query.getTimezone());
                    break;
                case YEAR:
                    startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0, query.getTimezone());
                    endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59, query.getTimezone());
                    break;
                case ALL:
                    // Ignore
                    break;
                default:
                    throw createApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED).set("level", query.getGranularity());
            }

            for (short p = 0; p < timePartitions; p++) {
                Scan scan = new Scan();
                scan.setCaching(scanCacheSize);
                scan.addFamily(columnFamily);

                byte[] rowKey = createPartitionedRowKey(p, endDate.getMillis());
                scan.setStartRow(rowKey);

                rowKey = createPartitionedRowKey(p, startDate.getMillis());
                scan.setStopRow(calculateTheClosestNextRowKeyForPrefix(rowKey));

                scanner = table.getScanner(scan);

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 2, 10));
                    byte[] serialHash = Arrays.copyOfRange(r.getRow(), 10, 26);

                    Float volume = null, difference = null;
                    long lastTimestamp = 0;

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));
                        long timestamp = ((Long.MAX_VALUE / 1000) - (timeBucket + (long) offset)) * 1000L;

                        if ((startDate.getMillis() <= timestamp) && (timestamp <= endDate.getMillis())) {
                            int length = (int) Arrays.copyOfRange(entry.getKey(), 2, 3)[0];
                            byte[] slice = Arrays.copyOfRange(entry.getKey(), 3, 3 + length);

                            String columnQualifier = Bytes.toString(slice);
                            if (columnQualifier.equals("v")) {
                                volume = Bytes.toFloat(entry.getValue());
                            }
                            if (columnQualifier.equals("d")) {
                                difference = Bytes.toFloat(entry.getValue());
                            }

                            if (lastTimestamp == timestamp) {
                                if ((difference != null) && (volume != null)) {
                                    int filterIndex = 0;
                                    for (ExpandedPopulationFilter filter : query.getGroups()) {
                                        GroupDataSeries series = result.get(filterIndex);

                                        int index = inArray(filter.getSerialHashes(), serialHash);
                                        if (index >= 0) {
                                            series.addMeterRankingDataPoint(
                                                query.getGranularity(),
                                                filter.getUserKeys().get(index),
                                                filter.getLabels().get(index),
                                                timestamp,
                                                difference,
                                                volume,
                                                query.getMetrics(),
                                                query.getTimezone());

                                        }

                                        filterIndex++;
                                    }
                                    volume = difference = null;
                                }
                            } else {
                                lastTimestamp = timestamp;
                            }
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
                logger.error(getMessage(SharedErrorCode.RESOURCE_RELEASE_FAILED), ex);
            }
        }

        // Post process results
        int filterIndex = 0;
        for (final ExpandedPopulationFilter filter : query.getGroups()) {
            flatProjectSeries(query, filter, result.get(filterIndex));
            filterIndex++;
        }

        cleanSeries(query, result);

        return result;
    }

    /**
     * Removes any unsupported metrics from a query's result.
     *
     * @param query the query.
     * @param result the query result.
     */
    private void cleanSeries(ExpandedDataQuery query, ArrayList<GroupDataSeries> result) {
        int filterIndex = 0;
        for (final ExpandedPopulationFilter filter : query.getGroups()) {
            GroupDataSeries series = result.get(filterIndex);
            if (filter.getRanking() != null) {
                for (Object p : series.getPoints()) {
                    RankingDataPoint rankingDataPoint = (RankingDataPoint) p;
                    for (UserDataPoint userDataPoint : rankingDataPoint.getUsers()) {
                        MeterUserDataPoint meterUserDataPoint = (MeterUserDataPoint) userDataPoint;

                        meterUserDataPoint.getVolume().remove(EnumMetric.MIN);
                        meterUserDataPoint.getVolume().remove(EnumMetric.MAX);
                        meterUserDataPoint.getVolume().remove(EnumMetric.AVERAGE);
                        meterUserDataPoint.getVolume().remove(EnumMetric.COUNT);
                    }
                }
            }
            filterIndex++;
        }
    }

}
