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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import eu.daiad.web.hbase.HBaseConnectionManager;
import eu.daiad.web.model.TemporalConstants;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DataErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.meter.WaterMeterDataPoint;
import eu.daiad.web.model.meter.WaterMeterDataSeries;
import eu.daiad.web.model.meter.WaterMeterMeasurement;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.model.meter.WaterMeterMeasurementQuery;
import eu.daiad.web.model.meter.WaterMeterMeasurementQueryResult;
import eu.daiad.web.model.meter.WaterMeterStatus;
import eu.daiad.web.model.meter.WaterMeterStatusQueryResult;
import eu.daiad.web.model.query.DataPoint;
import eu.daiad.web.model.query.EnumMetric;
import eu.daiad.web.model.query.ExpandedDataQuery;
import eu.daiad.web.model.query.ExpandedPopulationFilter;
import eu.daiad.web.model.query.GroupDataSeries;
import eu.daiad.web.model.query.MeterUserDataPoint;
import eu.daiad.web.model.query.RankingDataPoint;
import eu.daiad.web.model.query.UserDataPoint;
import eu.daiad.web.repository.AbstractHBaseRepository;

@Repository()
public class HBaseWaterMeterMeasurementRepository extends AbstractHBaseRepository implements
                IWaterMeterMeasurementRepository {

    private static final Log logger = LogFactory.getLog(HBaseWaterMeterMeasurementRepository.class);

    private final String ERROR_RELEASE_RESOURCES = "Failed to release resources";

    private enum EnumTimeInterval {
        UNDEFINED(0), HOUR(3600), DAY(86400);

        private final int value;

        private EnumTimeInterval(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    private final String meterTableMeasurementByMeter = "daiad:meter-measurements-by-user";

    private final String meterTableMeasurementByTime = "daiad:meter-measurements-by-time";

    private final String columnFamilyName = "cf";

    @Value("${hbase.data.time.partitions}")
    private short timePartitions;

    @Value("${scanner.cache.size}")
    private int scanCacheSize = 1;

    @Autowired
    private HBaseConnectionManager connection;

    @Override
    public void store(String serial, WaterMeterMeasurementCollection data) {
        try {
            if ((data == null) || (data.getMeasurements() == null) || (data.getMeasurements().size() == 0)) {
                return;
            }

            // Sort measurements
            Collections.sort(data.getMeasurements(), new Comparator<WaterMeterMeasurement>() {
                public int compare(WaterMeterMeasurement o1, WaterMeterMeasurement o2) {
                    if (o1.getTimestamp() <= o2.getTimestamp()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            // Get current status if no difference is computed
            WaterMeterMeasurement first = data.getMeasurements().get(0);
            if (first.getDifference() == null) {
                WaterMeterStatusQueryResult status = this.getStatus(new String[] { serial },
                                                                    new DateTime(first.getTimestamp(), DateTimeZone.UTC).getMillis());

                if (status.getDevices().size() == 0) {
                    // This is the first measurement for this water meter
                    first.setDifference(0.0f);
                } else if (first.getTimestamp() == status.getDevices().get(0).getTimestamp()) {
                    first.setDifference(first.getVolume() - status.getDevices().get(0).getVolume() + status.getDevices().get(0).getVariation());
                } else {
                    first.setDifference(first.getVolume() - status.getDevices().get(0).getVolume());
                }
            }
            for (int i = 1, count = data.getMeasurements().size(); i < count; i++) {
                if (data.getMeasurements().get(i).getDifference() == null) {
                    data.getMeasurements().get(i).setDifference(data.getMeasurements().get(i).getVolume() - data.getMeasurements().get(i - 1).getVolume());
                }
            }

            this.storeDataByMeter(serial, data);
            this.storeDataByTime(serial, data);
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @SuppressWarnings("resource")
    private void storeDataByMeter(String serial, WaterMeterMeasurementCollection data) {
        Table table = null;
        try {
            table = connection.getTable(this.meterTableMeasurementByMeter);

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            byte[] meterSerial = serial.getBytes("UTF-8");
            byte[] meterSerialHash = md.digest(meterSerial);

            for (int i = 0; i < data.getMeasurements().size(); i++) {
                WaterMeterMeasurement m = data.getMeasurements().get(i);

                if (m.getVolume() < 0) {
                    continue;
                }

                long timestamp = (Long.MAX_VALUE / 1000) - (m.getTimestamp() / 1000);

                long timeSlice = timestamp % EnumTimeInterval.HOUR.getValue();
                byte[] timeSliceBytes = Bytes.toBytes((short) timeSlice);
                if (timeSliceBytes.length != 2) {
                    throw new RuntimeException("Invalid byte array length!");
                }

                long timeBucket = timestamp - timeSlice;

                byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
                if (timeBucketBytes.length != 8) {
                    throw new RuntimeException("Invalid byte array length!");
                }

                byte[] rowKey = new byte[meterSerialHash.length + timeBucketBytes.length];
                System.arraycopy(meterSerialHash, 0, rowKey, 0, meterSerialHash.length);
                System.arraycopy(timeBucketBytes, 0, rowKey, meterSerialHash.length, timeBucketBytes.length);

                Put p = new Put(rowKey);

                byte[] column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("v")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

                column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("d")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getDifference()));

                column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("s")));
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
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    @SuppressWarnings("resource")
    private void storeDataByTime(String serial, WaterMeterMeasurementCollection data) {
        Table table = null;

        try {
            table = connection.getTable(this.meterTableMeasurementByTime);

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            byte[] meterSerial = serial.getBytes("UTF-8");
            byte[] meterSerialHash = md.digest(meterSerial);

            for (int i = 0; i < data.getMeasurements().size(); i++) {
                WaterMeterMeasurement m = data.getMeasurements().get(i);

                if (m.getVolume() < 0) {
                    continue;
                }

                short partition = (short) (m.getTimestamp() % this.timePartitions);
                byte[] partitionBytes = Bytes.toBytes(partition);

                long timestamp = (Long.MAX_VALUE / 1000) - (m.getTimestamp() / 1000);

                long timeSlice = timestamp % EnumTimeInterval.HOUR.getValue();
                byte[] timeSliceBytes = Bytes.toBytes((short) timeSlice);
                if (timeSliceBytes.length != 2) {
                    throw new RuntimeException("Invalid byte array length!");
                }

                long timeBucket = timestamp - timeSlice;

                byte[] timeBucketBytes = Bytes.toBytes(timeBucket);
                if (timeBucketBytes.length != 8) {
                    throw new RuntimeException("Invalid byte array length!");
                }

                byte[] rowKey = new byte[partitionBytes.length + timeBucketBytes.length + meterSerialHash.length];

                System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
                System.arraycopy(timeBucketBytes, 0, rowKey, partitionBytes.length, timeBucketBytes.length);
                System.arraycopy(meterSerialHash, 0, rowKey, (partitionBytes.length + timeBucketBytes.length),
                                meterSerialHash.length);

                Put p = new Put(rowKey);

                byte[] column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("v")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getVolume()));

                column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("d")));
                p.addColumn(columnFamily, column, Bytes.toBytes(m.getDifference()));

                column = this.concatenate(timeSliceBytes, this.appendLength(Bytes.toBytes("s")));
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
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    private byte[] getDeviceTimeRowKey(byte[] meterSerialHash, long timestamp, EnumTimeInterval interval)
                    throws Exception {

        long intervalInSeconds = EnumTimeInterval.HOUR.getValue();
        switch (interval) {
            case HOUR:
                intervalInSeconds = interval.getValue();
                break;

            default:
                throw new RuntimeException(String.format("Time interval [%s] is not supported.", interval.toString()));
        }

        long timeSlice = timestamp % intervalInSeconds;
        long timeBucket = timestamp - timeSlice;
        byte[] timeBucketBytes = Bytes.toBytes(timeBucket);

        byte[] rowKey = new byte[meterSerialHash.length + 8];
        System.arraycopy(meterSerialHash, 0, rowKey, 0, meterSerialHash.length);
        System.arraycopy(timeBucketBytes, 0, rowKey, meterSerialHash.length, timeBucketBytes.length);

        return rowKey;
    }

    @Override
    public WaterMeterStatusQueryResult getStatus(String serials[]) {
        return this.getStatus(serials, new DateTime(DateTimeZone.UTC).getMillis());
    }

    @Override
    public WaterMeterStatusQueryResult getStatus(String serials[], long maxDateTime) {
        WaterMeterStatusQueryResult data = new WaterMeterStatusQueryResult();

        Table table = null;
        ResultScanner scanner = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            table = connection.getTable(this.meterTableMeasurementByMeter);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            for (int deviceIndex = 0; deviceIndex < serials.length; deviceIndex++) {
                byte[] meterSerial = serials[deviceIndex].getBytes("UTF-8");
                byte[] meterSerialHash = md.digest(meterSerial);

                Scan scan = new Scan();
                scan.addFamily(columnFamily);
                scan.setStartRow(this.getDeviceTimeRowKey(meterSerialHash, (Long.MAX_VALUE / 1000)
                                - (maxDateTime / 1000), EnumTimeInterval.HOUR));
                scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(meterSerialHash));
                scan.setCaching(2);

                scanner = table.getScanner(scan);

                int valueCount = 0;

                WaterMeterStatus status = new WaterMeterStatus(serials[deviceIndex]);
                WaterMeterDataPoint value1 = new WaterMeterDataPoint();
                WaterMeterDataPoint value2 = new WaterMeterDataPoint();

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    if (valueCount == 2) {
                        break;
                    }

                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 16, 24));

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        if (valueCount == 2) {
                            break;
                        }

                        short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));

                        long timestamp = ((Long.MAX_VALUE / 1000) - (timeBucket + (long) offset)) * 1000L;

                        if (timestamp <= maxDateTime) {
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
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

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

            table = connection.getTable(this.meterTableMeasurementByMeter);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            for (int deviceIndex = 0; deviceIndex < serials.length; deviceIndex++) {
                byte[] meterSerial = serials[deviceIndex].getBytes("UTF-8");
                byte[] meterSerialHash = md.digest(meterSerial);

                Scan scan = new Scan();
                scan.addFamily(columnFamily);

                scan.setStartRow(this.getDeviceTimeRowKey(meterSerialHash, (Long.MAX_VALUE / 1000L)
                                - (endDate.getMillis() / 1000L), EnumTimeInterval.HOUR));

                scan.setStopRow(this.calculateTheClosestNextRowKeyForPrefix(this.getDeviceTimeRowKey(meterSerialHash,
                                (Long.MAX_VALUE / 1000L) - (startDate.getMillis() / 1000L), EnumTimeInterval.HOUR)));

                scanner = table.getScanner(scan);

                WaterMeterDataSeries series = new WaterMeterDataSeries(query.getDeviceKey()[deviceIndex],
                                serials[deviceIndex], startDate.getMillis(), queryEndDate.getMillis(), query
                                                .getGranularity());

                data.getSeries().add(series);

                for (Result r = scanner.next(); r != null; r = scanner.next()) {
                    NavigableMap<byte[], byte[]> map = r.getFamilyMap(columnFamily);

                    long timeBucket = Bytes.toLong(Arrays.copyOfRange(r.getRow(), 16, 24));

                    Float volume = null, difference = null;
                    long timestamp = 0;

                    for (Entry<byte[], byte[]> entry : map.entrySet()) {
                        short offset = Bytes.toShort(Arrays.copyOfRange(entry.getKey(), 0, 2));
                        timestamp = ((Long.MAX_VALUE / 1000L) - (timeBucket + (long) offset)) * 1000L;

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
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }
    }

    @Override
    public ArrayList<GroupDataSeries> query(ExpandedDataQuery query) throws ApplicationException {
        Table table = null;
        ResultScanner scanner = null;

        ArrayList<GroupDataSeries> result = new ArrayList<GroupDataSeries>();
        for (ExpandedPopulationFilter filter : query.getGroups()) {
            result.add(new GroupDataSeries(filter.getLabel(), filter.getUsers().size(), filter.getAreaId()));
        }
        try {
            table = connection.getTable(this.meterTableMeasurementByTime);
            byte[] columnFamily = Bytes.toBytes(this.columnFamilyName);

            DateTime startDate = new DateTime(query.getStartDateTime(), DateTimeZone.UTC);
            DateTime endDate = new DateTime(query.getEndDateTime(), DateTimeZone.UTC);

            switch (query.getGranularity()) {
                case HOUR:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
                                    startDate.getDayOfMonth(), startDate.getHourOfDay(), 0, 0, DateTimeZone.UTC);
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(),
                                    endDate.getHourOfDay(), 59, 59, DateTimeZone.UTC);
                    break;
                case DAY:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(),
                                    startDate.getDayOfMonth(), 0, 0, 0, DateTimeZone.UTC);
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.getDayOfMonth(), 23,
                                    59, 59, DateTimeZone.UTC);
                    break;
                case WEEK:
                    DateTime monday = startDate.withDayOfWeek(DateTimeConstants.MONDAY);
                    DateTime sunday = endDate.withDayOfWeek(DateTimeConstants.SUNDAY);
                    startDate = new DateTime(monday.getYear(), monday.getMonthOfYear(), monday.getDayOfMonth(), 0, 0,
                                    0, DateTimeZone.UTC);
                    endDate = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 23, 59,
                                    59, DateTimeZone.UTC);
                    break;
                case MONTH:
                    startDate = new DateTime(startDate.getYear(), startDate.getMonthOfYear(), 1, 0, 0, 0,
                                    DateTimeZone.UTC);
                    endDate = new DateTime(endDate.getYear(), endDate.getMonthOfYear(), endDate.dayOfMonth()
                                    .getMaximumValue(), 23, 59, 59, DateTimeZone.UTC);
                    break;
                case YEAR:
                    startDate = new DateTime(startDate.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC);
                    endDate = new DateTime(endDate.getYear(), 12, 31, 23, 59, 59, DateTimeZone.UTC);
                    break;
                case ALL:
                    // Ignore
                    break;
                default:
                    throw createApplicationException(DataErrorCode.TIME_GRANULARITY_NOT_SUPPORTED).set("level",
                                    query.getGranularity());
            }

            for (short p = 0; p < timePartitions; p++) {
                Scan scan = new Scan();
                scan.setCaching(this.scanCacheSize);
                scan.addFamily(columnFamily);

                byte[] partitionBytes = Bytes.toBytes(p);

                long from = (Long.MAX_VALUE / 1000) - (endDate.getMillis() / 1000);
                from = from - (from % EnumTimeInterval.HOUR.getValue());
                byte[] fromBytes = Bytes.toBytes(from);

                long to = (Long.MAX_VALUE / 1000) - (startDate.getMillis() / 1000);
                to = to - (to % EnumTimeInterval.HOUR.getValue());
                byte[] toBytes = Bytes.toBytes(to);

                // Scanner row key prefix start
                byte[] rowKey = new byte[partitionBytes.length + fromBytes.length];

                System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
                System.arraycopy(fromBytes, 0, rowKey, partitionBytes.length, fromBytes.length);

                scan.setStartRow(rowKey);

                // Scanner row key prefix end
                rowKey = new byte[partitionBytes.length + toBytes.length];

                System.arraycopy(partitionBytes, 0, rowKey, 0, partitionBytes.length);
                System.arraycopy(toBytes, 0, rowKey, partitionBytes.length, toBytes.length);

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

                                        int index = inArray(filter.getSerials(), serialHash);
                                        if (index >= 0) {
                                            series.addMeterRankingDataPoint(query.getGranularity(), filter.getUsers()
                                                            .get(index), filter.getLabels().get(index), timestamp,
                                                            difference, volume, query.getMetrics(), query.getTimezone());

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
                logger.error(ERROR_RELEASE_RESOURCES, ex);
            }
        }

        // Post process results
        int filterIndex = 0;
        for (final ExpandedPopulationFilter filter : query.getGroups()) {
            GroupDataSeries series = result.get(filterIndex);

            if (filter.getRanking() == null) {
                // Aggregate all user data points of a ranking data point to a
                // single meter data point
                ArrayList<DataPoint> points = new ArrayList<DataPoint>();

                for (DataPoint point : series.getPoints()) {
                    points.add(((RankingDataPoint) point).aggregate(query.getMetrics(),
                                    DataPoint.EnumDataPointType.METER));
                }

                series.setPoints(points);
            } else {
                // Truncate (n-k) users and keep top/bottom-k only
                for (DataPoint point : series.getPoints()) {
                    RankingDataPoint ranking = (RankingDataPoint) point;

                    Collections.sort(ranking.getUsers(), new Comparator<UserDataPoint>() {

                        @Override
                        public int compare(UserDataPoint u1, UserDataPoint u2) {
                            MeterUserDataPoint m1 = (MeterUserDataPoint) u1;
                            MeterUserDataPoint m2 = (MeterUserDataPoint) u2;

                            if (m1.getVolume().get(EnumMetric.SUM) < m2.getVolume().get(EnumMetric.SUM)) {
                                return -1;
                            }
                            if (m1.getVolume().get(EnumMetric.SUM) > m2.getVolume().get(EnumMetric.SUM)) {
                                return 1;
                            }
                            return 0;
                        }
                    });

                    int limit = filter.getRanking().getLimit();
                    switch (filter.getRanking().getType()) {
                        case TOP:
                            for (int i = 0, max = ranking.getUsers().size() - limit; i < max; i++) {
                                ranking.getUsers().remove(0);
                            }
                            break;
                        case BOTTOM:
                            for (int i = ranking.getUsers().size() - 1, max = limit - 1; i > max; i--) {
                                ranking.getUsers().remove(i);
                            }
                            break;
                        default:
                            series.getPoints().clear();
                            break;
                    }
                }
            }
            filterIndex++;
        }

        cleanSeries(query, result);

        return result;
    }

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
